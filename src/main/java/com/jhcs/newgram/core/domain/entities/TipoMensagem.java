package com.jhcs.newgram.core.domain.entities;

import lombok.Getter;

@Getter
public enum TipoMensagem {
    TEXTO,
    IMAGEM,
    VIDEO,
    AUDIO,
    ARQUIVO
}