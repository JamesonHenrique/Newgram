package com.jhcs.newgram.infrastructure.aws;

import com.jhcs.newgram.infrastructure.exception.ArquivoException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Upload, download (streaming) e presigned URLs no S3 (SDK v2).
 *
 * <p>Validacao e por whitelist de content-type + path sanitizado; download
 * nunca materializa o arquivo inteiro em memoria (retorna stream).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm");
    private static final long MAX_BYTES = 32L * 1024 * 1024;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration-minutes:60}")
    private int presignedUrlExpirationMinutes;

    public String uploadFile(MultipartFile file, String path) {
        validate(file);
        String filePath = normalizePath(sanitizeSegment(path) + "/" + generateFileName(file.getOriginalFilename()));
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .contentDisposition("inline")
                    .serverSideEncryption("AES256")
                    .build();
            s3Client.putObject(put, RequestBody.fromInputStream(in, file.getSize()));
            return filePath;
        } catch (IOException e) {
            throw new ArquivoException("Erro ao fazer upload do arquivo", e);
        } catch (S3Exception e) {
            throw new ArquivoException("Serviço de armazenamento indisponível", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArquivoException("Arquivo vazio ou ausente");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ArquivoException("Arquivo excede o limite de 32MB");
        }
        String contentType = Objects.toString(file.getContentType(), "");
        if (!IMAGE_TYPES.contains(contentType) && !VIDEO_TYPES.contains(contentType)) {
            throw new ArquivoException("Tipo de arquivo não suportado: " + contentType);
        }
    }

    /** Download como stream (sem {@code readAllBytes}: nao estoura memoria). */
    public InputStreamResource downloadFile(String filePath) {
        try {
            GetObjectRequest get = GetObjectRequest.builder().bucket(bucketName).key(filePath).build();
            ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(get);
            return new InputStreamResource(stream);
        } catch (S3Exception e) {
            throw new ArquivoException("Arquivo não encontrado no armazenamento", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(filePath).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return;
            }
            throw new ArquivoException("Falha ao remover arquivo do armazenamento", e);
        }
    }

    public String getFileUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        try {
            GetObjectRequest get = GetObjectRequest.builder().bucket(bucketName).key(filePath).build();
            GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                    .getObjectRequest(get)
                    .build();
            return s3Presigner.presignGetObject(presign).url().toString();
        } catch (S3Exception e) {
            throw new ArquivoException("Falha ao gerar URL do arquivo", e);
        }
    }

    private String generateFileName(String originalFileName) {
        String base = Objects.toString(originalFileName, "arquivo").replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
        if (base.isBlank()) {
            base = "arquivo";
        }
        return UUID.randomUUID() + "-" + base;
    }

    private String sanitizeSegment(String path) {
        String clean = Objects.toString(path, "uploads").replace("\\", "/").replaceAll("/+", "/").trim();
        if (clean.contains("..")) {
            throw new ArquivoException("Caminho de upload inválido");
        }
        return clean.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/").replaceAll("/+", "/");
    }
}
