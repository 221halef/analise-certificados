package br.com.atividadescomplementares.nucleo;

import java.time.LocalDate;
import java.util.List;

public record CamposExtraidos(String titulo, String emissor, LocalDate data, Double horasDeclaradas,
                               String codigoVerificacao, String participante, List<String> camposFaltando) {
}
