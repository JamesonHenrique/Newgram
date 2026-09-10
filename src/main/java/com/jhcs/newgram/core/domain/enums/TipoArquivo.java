package com.jhcs.newgram.core.domain.enums;

public enum TipoArquivo {
        FOTO_PERFIL("fotoPerfil"),
        POST("posts"),
        STORIE("stories"),
        FOTO_DESTAQUE("destaques");
        private final String pasta;
        TipoArquivo(String pasta) {
            this.pasta = pasta;
        }
        public String getPasta() {
            return pasta;
        }


}
