package br.com.atividadescomplementares.ui;

import br.com.atividadescomplementares.nucleo.Categoria;

public class CoresCategoria {

    public static String classeEstiloPonto(Categoria categoria) {
        return switch (categoria) {
            case CURSO -> "ponto-curso";
            case PALESTRA -> "ponto-palestra";
            case EXTENSAO -> "ponto-extensao";
            case ESTAGIO -> "ponto-estagio";
            case MONITORIA -> "ponto-monitoria";
            case OUTROS -> "ponto-outros";
        };
    }
}
