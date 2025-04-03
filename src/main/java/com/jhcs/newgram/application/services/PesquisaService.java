package com.jhcs.newgram.application.services;

import com.jhcs.newgram.application.dtos.pesquisa.PesquisaResponseDTO;
import com.jhcs.newgram.core.domain.entities.Pesquisa;
import com.jhcs.newgram.core.domain.repositories.PesquisaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PesquisaService {

    @Autowired
    private PesquisaRepository pesquisaRepository;

    @Transactional(readOnly = true)
    public Page<PesquisaResponseDTO> listarPesquisasPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Pesquisa> pesquisas = pesquisaRepository.findByUsuarioIdOrderByDataPesquisaDesc(usuarioId, pageable);
        return pesquisas.map(this::converterParaPesquisaResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<String> listarTermosPesquisadosRecentes(Long usuarioId, Pageable pageable) {
        return pesquisaRepository.findTermosPesquisadosRecentes(usuarioId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> listarTermosPesquisadosFrequentes(Long usuarioId, Pageable pageable) {
        return pesquisaRepository.findTermosPesquisadosFrequentes(usuarioId, pageable);
    }

    @Transactional
    public void deletarPesquisaPorTermo(Long usuarioId, String termoPesquisado) {
        pesquisaRepository.deleteByUsuarioIdAndTermoPesquisado(usuarioId, termoPesquisado);
    }

    private PesquisaResponseDTO converterParaPesquisaResponseDTO(Pesquisa pesquisa) {
        PesquisaResponseDTO dto = new PesquisaResponseDTO();
        dto.setId(pesquisa.getId());
        dto.setTermoPesquisado(pesquisa.getTermoPesquisado());
        dto.setDataPesquisa(pesquisa.getDataPesquisa());
        dto.setUsuarioId(pesquisa.getUsuario().getId());
        return dto;
    }
}