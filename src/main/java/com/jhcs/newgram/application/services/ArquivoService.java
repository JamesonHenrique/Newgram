package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.ArquivoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArquivoService {
    @Value("${file.max-size:5242880}")
    private long maxFileSize;

    private final S3StorageService s3StorageService;

    private static final Map<String, String> EXTENSAO_PARA_CONTENT_TYPE = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "mp4", "video/mp4"
    );

    public String saveFile(
            MultipartFile sourceFile,
            String nomeUsuario,
            TipoArquivo tipoArquivo
    ) {
        Objects.requireNonNull(sourceFile, "Arquivo não pode ser nulo");
        Objects.requireNonNull(nomeUsuario, "Nome de usuário não pode ser nulo");
        Objects.requireNonNull(tipoArquivo, "Tipo de arquivo não pode ser nulo");
        validarArquivo(sourceFile);
        String usuarioSanitizado = sanitizarNomeUsuario(nomeUsuario);
        final String fileUploadSubPath = "usuarios/" + usuarioSanitizado + "/" + tipoArquivo.getPasta();
        return s3StorageService.uploadFile(sourceFile, fileUploadSubPath);
    }

    /** Rejeita path traversal e separadores no segmento do path S3. */
    private String sanitizarNomeUsuario(String nomeUsuario) {
        String sanitizado = nomeUsuario.trim();
        if (sanitizado.isEmpty()
                || sanitizado.contains("..")
                || sanitizado.contains("/")
                || sanitizado.contains("\\")) {
            throw new ArquivoException("Nome de usuário inválido para upload");
        }
        return sanitizado;
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoException("Arquivo vazio");
        }

        if (arquivo.getSize() > maxFileSize) {
            throw new ArquivoException("Tamanho do arquivo excede o limite permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String extensao = getFileExtension(arquivo.getOriginalFilename());
        String contentTypeEsperado = EXTENSAO_PARA_CONTENT_TYPE.get(extensao);
        if (contentTypeEsperado == null) {
            throw new ArquivoException("Tipo de arquivo não permitido. Extensões aceitas: "
                    + String.join(", ", EXTENSAO_PARA_CONTENT_TYPE.keySet()));
        }

        String contentTypeReal = arquivo.getContentType();
        if (contentTypeReal == null || !contentTypeReal.equalsIgnoreCase(contentTypeEsperado)) {
            log.warn("Content-type divergente: filename={} contentType={}", arquivo.getOriginalFilename(), contentTypeReal);
            throw new ArquivoException("Content-type do arquivo não corresponde à extensão ." + extensao);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
