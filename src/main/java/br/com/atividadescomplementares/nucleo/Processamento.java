package br.com.atividadescomplementares.nucleo;

import java.util.List;

public record Processamento(List<Atividade> atividades, ResultadoCalculo resultado) {
}
