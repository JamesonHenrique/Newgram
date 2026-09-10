package com.jhcs.newgram.infrastructure.aws;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.jhcs.newgram.infrastructure.exception.ArquivoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiration-minutes:60}")
    private int presignedUrlExpirationMinutes;

    public String uploadFile(MultipartFile file, String path) {
        try {
            String fileName = generateFileName(Objects.requireNonNull(file.getOriginalFilename()));
            String filePath = normalizePath(path + "/" + fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(bucketName, filePath, file.getInputStream(), metadata);
            amazonS3.putObject(putRequest);

            log.info("Arquivo salvo no S3: {}", filePath);
            return filePath;
        } catch (IOException e) {
            throw new ArquivoException("Erro ao fazer upload para S3", e);
        }
    }

    public byte[] downloadFile(String filePath) {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, filePath);
            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException e) {
            throw new ArquivoException("Erro ao baixar arquivo do S3: " + e.getMessage());
        }
    }
    public byte[] downloadFileByUrl(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            return downloadFile(filePath);
        } catch (Exception e) {
            throw new ArquivoException("Erro ao baixar arquivo do S3 por URL: " + e.getMessage());
        }
    }

    private String extractFilePathFromUrl(String fileUrl) {
        String baseUrl = "https://" + bucketName + ".s3.amazonaws.com/";
        return fileUrl.replace(baseUrl, "").split("\\?")[0];
    }
    public void deleteFile(String filePath) {
        amazonS3.deleteObject(bucketName, filePath);
        log.info("Arquivo removido do S3: {}", filePath);
    }

    public String getFileUrl(String filePath) {
        return generatePresignedUrl(filePath);
    }

    private String generatePresignedUrl(String filePath) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + TimeUnit.MINUTES.toMillis(presignedUrlExpirationMinutes);
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, filePath)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    private String generateFileName(String originalFileName) {
        String sanitized = originalFileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
        return UUID.randomUUID().toString() + "-" + sanitized;
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/")
                .replaceAll("/+", "/");
    }
}