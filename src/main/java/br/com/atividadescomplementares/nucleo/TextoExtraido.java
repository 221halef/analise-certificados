package br.com.atividadescomplementares.nucleo;

import java.nio.file.Path;

public record TextoExtraido(String arquivoNome, Path caminhoArquivo, String hashSha256,
                             String texto, boolean semTextoExtraivel) {
}
