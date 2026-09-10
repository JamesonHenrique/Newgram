package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.infrastructure.aws.S3StorageService;
import com.jhcs.newgram.infrastructure.exception.ArquivoException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Validacao e upload de arquivos (delega o storage ao S3). */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArquivoService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "gif", "webp", "mp4");
    private static final Set<String> CONTENT_TYPES_PERMITIDOS =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "video/mp4");

    @Value("${file.max-size:2097152}")
    private long maxFileSize;

    private final S3StorageService s3StorageService;

    public String saveFile(MultipartFile sourceFile, String nomeUsuario, TipoArquivo tipoArquivo) {
        Objects.requireNonNull(sourceFile, "Arquivo é obrigatório");
        Objects.requireNonNull(nomeUsuario, "Nome do usuário é obrigatório");
        Objects.requireNonNull(tipoArquivo, "Tipo do arquivo é obrigatório");
        validarArquivo(sourceFile);
        final String fileUploadSubPath = "usuarios/" + sanitizarSegmento(nomeUsuario) + "/" + tipoArquivo.getPasta();
        return s3StorageService.uploadFile(sourceFile, fileUploadSubPath);
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoException("Arquivo vazio");
        }
        if (arquivo.getSize() > maxFileSize) {
            throw new ArquivoException(
                    "Tamanho do arquivo excede o limite permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }
        String extensao = getFileExtension(arquivo.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new ArquivoException("Tipo de arquivo não permitido. Extensões aceitas: "
                    + String.join(", ", EXTENSOES_PERMITIDAS));
        }
        String contentType = Objects.toString(arquivo.getContentType(), "").toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new ArquivoException("Tipo de conteúdo não permitido: " + contentType);
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
        return fileName.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String sanitizarSegmento(String segmento) {
        String clean = segmento.replace("\\", "/").replaceAll("/+", "/").trim();
        if (clean.isEmpty() || clean.contains("..")) {
            throw new ArquivoException("Nome de usuário inválido para upload");
        }
        return clean;
    }
}
