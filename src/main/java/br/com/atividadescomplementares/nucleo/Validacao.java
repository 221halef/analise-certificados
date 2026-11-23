package br.com.atividadescomplementares.nucleo;

import java.util.ArrayList;
import java.util.List;

/**
 * Regra de validação: um certificado só é "válido" quando título, categoria, data e horas estão preenchidos.
 * Emissor, participante e código de verificação são opcionais.
 */
public final class Validacao {

    private static final String PREFIXO_CAMPOS_FALTANDO = "campos faltando";
    private static final String SEM_TEXTO = "sem texto extraível";

    private Validacao() {
    }

    public static List<String> camposObrigatoriosFaltando(Atividade atividade) {
        List<String> faltando = new ArrayList<>();
        if (atividade.getTitulo() == null || atividade.getTitulo().isBlank()) {
            faltando.add("título");
        }
        if (atividade.getCategoria() == null || atividade.getCategoria() == Categoria.OUTROS) {
            faltando.add("categoria");
        }
        if (atividade.getData() == null) {
            faltando.add("data");
        }
        if (atividade.getHorasDeclaradas() <= 0) {
            faltando.add("horas");
        }
        return faltando;
    }

    /** Recalcula o status entre "válida" e "revisar". Duplicados não são alterados. */
    public static void atualizarStatus(Atividade atividade) {
        if (atividade.getStatus() == Status.DUPLICADO || atividade.getStatus() == Status.POSSIVEL_DUPLICADO) {
            return;
        }

        List<String> faltando = camposObrigatoriosFaltando(atividade);

        List<String> observacoes = new ArrayList<>();
        String atual = atividade.getObservacao();
        if (atual != null && !atual.isBlank()) {
            for (String parte : atual.split("; ")) {
                if (parte.startsWith(PREFIXO_CAMPOS_FALTANDO)) {
                    continue;
                }
                if (parte.equals(SEM_TEXTO) && faltando.isEmpty()) {
                    continue;
                }
                observacoes.add(parte);
            }
        }

        if (faltando.isEmpty()) {
            atividade.setStatus(Status.OK);
        } else {
            atividade.setStatus(Status.REVISAR);
            observacoes.add(PREFIXO_CAMPOS_FALTANDO + ": " + String.join(", ", faltando));
        }
        atividade.setObservacao(String.join("; ", observacoes));
    }
}
