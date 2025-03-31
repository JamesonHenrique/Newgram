package com.jhcs.newgram.application.services;



import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoUploadResponseDTO;
import com.jhcs.newgram.core.domain.entities.Arquivo;
import com.jhcs.newgram.core.domain.repositories.ArquivoRepository;
import com.jhcs.newgram.infrastructure.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class
ArquivoService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base.url}")
    private String baseUrl;

    @Autowired
    private ArquivoRepository arquivoRepository;

    @Transactional
    public ArquivoUploadResponseDTO uploadArquivo(MultipartFile file, Arquivo.TipoEntidadeRelacionada tipoEntidade, Long entidadeId) {
        validarArquivo(file);

        try {
            String nomeOriginal = file.getOriginalFilename();
            String extensao = obterExtensao(nomeOriginal);
            String nomeArmazenado = UUID.randomUUID().toString() + "." + extensao;

            String contentType = file.getContentType();
            String tipo = determinarTipoArquivo(contentType);

            // Criar diretório se não existir
            Path diretorioPath = Paths.get(uploadDir, tipo);
            if (!Files.exists(diretorioPath)) {
                Files.createDirectories(diretorioPath);
            }

            // Caminho completo do arquivo
            Path caminhoCompleto = diretorioPath.resolve(nomeArmazenado);

            // Salvar arquivo no sistema de arquivos
            Files.copy(file.getInputStream(), caminhoCompleto);

            // Criar entidade Arquivo
            Arquivo arquivo = new Arquivo();
            arquivo.setNomeOriginal(nomeOriginal);
            arquivo.setNomeArmazenado(nomeArmazenado);
            arquivo.setTipo(tipo);
            arquivo.setTamanho(file.getSize());
            arquivo.setDataUpload(new Date());
            arquivo.setCaminho(tipo + "/" + nomeArmazenado);
            arquivo.setContentType(contentType);
            arquivo.setTipoEntidade(tipoEntidade);
            arquivo.setEntidadeId(entidadeId);

            arquivo = arquivoRepository.save(arquivo);

            // Montar resposta
            ArquivoUploadResponseDTO response = new ArquivoUploadResponseDTO();
            response.setId(arquivo.getId());
            response.setUrl(baseUrl + "/arquivos/" + arquivo.getCaminho());
            response.setTipo(arquivo.getTipo());
            response.setContentType(arquivo.getContentType());

            return response;

        } catch (IOException e) {
            throw new BusinessException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ArquivoDTO> buscarArquivosPorEntidade(Arquivo.TipoEntidadeRelacionada tipoEntidade, Long entidadeId) {
        List<Arquivo> arquivos = arquivoRepository.findByTipoEntidadeAndEntidadeId(tipoEntidade, entidadeId);
        return arquivos.stream().map(this::converterParaDTO).collect(Collectors.toList());
    }

    @Transactional
    public void excluirArquivo(Long arquivoId) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new BusinessException("Arquivo não encontrado"));

        // Excluir arquivo físico
        Path path = Paths.get(uploadDir, arquivo.getCaminho());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BusinessException("Erro ao excluir arquivo físico: " + e.getMessage());
        }

        // Excluir registro no banco
        arquivoRepository.delete(arquivo);
    }

    @Transactional
    public void excluirArquivosPorEntidade(Arquivo.TipoEntidadeRelacionada tipoEntidade, Long entidadeId) {
        List<Arquivo> arquivos = arquivoRepository.findByTipoEntidadeAndEntidadeId(tipoEntidade, entidadeId);

        // Excluir arquivos físicos
        for (Arquivo arquivo : arquivos) {
            Path path = Paths.get(uploadDir, arquivo.getCaminho());
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new BusinessException("Erro ao excluir arquivo físico: " + e.getMessage());
            }
        }

        // Excluir registros no banco
        arquivoRepository.deleteByTipoEntidadeAndEntidadeId(tipoEntidade, entidadeId);
    }

    // Métodos auxiliares

    private void validarArquivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Arquivo vazio");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessException("Tipo de arquivo não identificado");
        }

        // Validar tipos permitidos (imagens e vídeos)
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new BusinessException("Tipo de arquivo não permitido. Apenas imagens e vídeos são aceitos.");
        }

        // Validar tamanho máximo (10MB para vídeos, 5MB para imagens)
        long tamanhoMaximoVideo = 10 * 1024 * 1024; // 10MB
        long tamanhoMaximoImagem = 5 * 1024 * 1024; // 5MB

        if (contentType.startsWith("video/") && file.getSize() > tamanhoMaximoVideo) {
            throw new BusinessException("Tamanho do vídeo excede o limite máximo de 10MB");
        }

        if (contentType.startsWith("image/") && file.getSize() > tamanhoMaximoImagem) {
            throw new BusinessException("Tamanho da imagem excede o limite máximo de 5MB");
        }
    }

    private String determinarTipoArquivo(String contentType) {
        if (contentType.startsWith("image/")) {
            return "imagem";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "outro";
        }
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null) {
            return "";
        }
        int indexPonto = nomeArquivo.lastIndexOf(".");
        if (indexPonto == -1) {
            return "";
        }
        return nomeArquivo.substring(indexPonto + 1);
    }

    private ArquivoDTO converterParaDTO(Arquivo arquivo) {
        ArquivoDTO dto = new ArquivoDTO();
        dto.setId(arquivo.getId());
        dto.setNomeOriginal(arquivo.getNomeOriginal());
        dto.setTipo(arquivo.getTipo());
        dto.setTamanho(arquivo.getTamanho());
        dto.setUrl(baseUrl + "/arquivos/" + arquivo.getCaminho());
        dto.setContentType(arquivo.getContentType());
        return dto;
    }
}