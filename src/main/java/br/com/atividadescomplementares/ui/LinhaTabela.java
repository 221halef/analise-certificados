package br.com.atividadescomplementares.ui;

import br.com.atividadescomplementares.nucleo.Atividade;
import br.com.atividadescomplementares.nucleo.Categoria;
import br.com.atividadescomplementares.nucleo.ResumoCategoria;

import java.time.format.DateTimeFormatter;

public class LinhaTabela {

    public static final String SEPARADOR_TITULO = "\t";

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final boolean ehCategoria;
    private final Atividade atividade;
    private final ResumoCategoria resumoCategoria;
    private final boolean temItensParaRevisar;

    private LinhaTabela(boolean ehCategoria, Atividade atividade, ResumoCategoria resumoCategoria,
                        boolean temItensParaRevisar) {
        this.ehCategoria = ehCategoria;
        this.atividade = atividade;
        this.resumoCategoria = resumoCategoria;
        this.temItensParaRevisar = temItensParaRevisar;
    }

    public static LinhaTabela deCategoria(ResumoCategoria resumo, boolean temItensParaRevisar) {
        return new LinhaTabela(true, null, resumo, temItensParaRevisar);
    }

    public static LinhaTabela deAtividade(Atividade atividade) {
        return new LinhaTabela(false, atividade, null, false);
    }

    public static String formatarHoras(double horas) {
        if (horas == Math.floor(horas)) {
            return String.valueOf((long) horas);
        }
        return String.valueOf(horas);
    }

    public boolean isEhCategoria() {
        return ehCategoria;
    }

    public boolean isTemItensParaRevisar() {
        return temItensParaRevisar;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public Categoria getCategoria() {
        return ehCategoria ? resumoCategoria.getCategoria() : atividade.getCategoria();
    }

    public String getTitulo() {
        if (ehCategoria) {
            return resumoCategoria.getCategoria().getNomePlural() + SEPARADOR_TITULO
                    + formatarHoras(resumoCategoria.getHoras()) + " h";
        }
        String titulo = atividade.getTitulo();
        return (titulo == null || titulo.isBlank()) ? atividade.getArquivoNome() : titulo;
    }

    public String getDataTexto() {
        if (ehCategoria || atividade.getData() == null) {
            return "";
        }
        return atividade.getData().format(FORMATO_DATA);
    }

    public String getHorasTexto() {
        return ehCategoria ? "" : formatarHoras(atividade.getHorasDeclaradas()) + " h";
    }

    public String getStatusTexto() {
        if (ehCategoria) {
            return "categoria:" + resumoCategoria.getCategoria().name() + ":" + (temItensParaRevisar ? "r" : "-");
        }
        if (atividade.isIgnorado()) {
            return "ignorado";
        }
        return switch (atividade.getStatus()) {
            case OK -> "válida";
            case REVISAR -> "revisar";
            case DUPLICADO -> "duplicado";
            case POSSIVEL_DUPLICADO -> "possível duplicado";
        };
    }

    public String getEstiloStatus() {
        if (atividade.isIgnorado()) {
            return "selo-neutro";
        }
        return switch (atividade.getStatus()) {
            case OK -> "selo-sucesso";
            case REVISAR -> "selo-atencao";
            case DUPLICADO, POSSIVEL_DUPLICADO -> "selo-erro";
        };
    }

    public boolean isIgnorado() {
        return !ehCategoria && atividade.isIgnorado();
    }
}
