package com.jhcs.newgram.core.domain.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Files;

@Slf4j
public class ArquivoUtils {
    private static final String UPLOADS_DIR = "uploads";

    public static byte[] lerArquivoDoLocal(String urlArquivo) {
        if (StringUtils.isBlank(urlArquivo)) {
            return null;
        }
        try {
            Path caminhoArquivo = new File(UPLOADS_DIR, urlArquivo).toPath();
            if (!Files.exists(caminhoArquivo)) {
                log.error("Arquivo não encontrado: {}", urlArquivo);
                return null;
            }
            return Files.readAllBytes(caminhoArquivo);
        } catch (NoSuchFileException e) {
            log.error("Arquivo não encontrado: {}", urlArquivo, e);
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo do local: {}", urlArquivo, e);
        }
        return null;
    }
}