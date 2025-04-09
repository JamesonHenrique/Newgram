package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.enums.TipoArquivo;
import com.jhcs.newgram.infrastructure.exception.ArquivoException;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.io.File.separator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArquivoService {

    @Value("${file.upload-dir}")
    private String fileUploadPath;

    @Value("${file.max-size:5242880}")
    private long maxFileSize;

    private static final List<String> EXTENSOES_PERMITIDAS = Arrays.asList("jpg", "jpeg", "png", "gif");



    public String saveFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String nomeUsuario,
            @Nonnull TipoArquivo tipoArquivo
    ) {
        validarArquivo(sourceFile);
        final String fileUploadSubPath = "usuarios" + separator + nomeUsuario + separator + tipoArquivo.getPasta();
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    public Resource carregarArquivo(String caminhoCompleto) {
        try {
            Path arquivoPath = Paths.get(caminhoCompleto);
            Resource resource = new UrlResource(arquivoPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ArquivoException("Não foi possível ler o arquivo: " + caminhoCompleto);
            }
        } catch (MalformedURLException e) {
            throw new ArquivoException("Erro ao carregar o arquivo: " + e.getMessage());
        }
    }

    public boolean excluirArquivo(String caminhoCompleto) {
        try {
            Path arquivoPath = Paths.get(caminhoCompleto);
            return Files.deleteIfExists(arquivoPath);
        } catch (IOException e) {
            log.error("Erro ao excluir o arquivo: {}", caminhoCompleto, e);
            throw new ArquivoException("Não foi possível excluir o arquivo: " + e.getMessage());
        }
    }

    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    ) {
        final String finalUploadPath = fileUploadPath + "/" + fileUploadSubPath.replace("\\", "/");
        File targetFolder = new File(finalUploadPath);

        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.error("Falha ao criar a pasta de destino: {}", targetFolder);
                throw new ArquivoException("Não foi possível criar o diretório para upload");
            }
        }

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String nomeArquivo = System.currentTimeMillis() + "." + fileExtension;
        String targetFilePath = finalUploadPath + "/" + nomeArquivo;
        Path targetPath = Paths.get(targetFilePath);

        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("Arquivo salvo com sucesso em: {}", targetFilePath);

            return fileUploadSubPath + "/" + nomeArquivo;
        } catch (IOException e) {
            log.error("Erro ao salvar o arquivo", e);
            throw new ArquivoException("Falha ao salvar o arquivo: " + e.getMessage());
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoException("Arquivo vazio");
        }

        if (arquivo.getSize() > maxFileSize) {
            throw new ArquivoException("Tamanho do arquivo excede o limite permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String extensao = getFileExtension(arquivo.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao.toLowerCase())) {
            throw new ArquivoException("Tipo de arquivo não permitido. Extensões aceitas: " + String.join(", ", EXTENSOES_PERMITIDAS));
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