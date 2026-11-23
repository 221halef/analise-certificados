package br.com.atividadescomplementares.nucleo;

import java.util.List;

public class ResultadoCalculo {

    private final List<ResumoCategoria> resumos;
    private final double totalHoras;

    public ResultadoCalculo(List<ResumoCategoria> resumos, double totalHoras) {
        this.resumos = resumos;
        this.totalHoras = totalHoras;
    }

    public List<ResumoCategoria> getResumos() {
        return resumos;
    }

    public double getTotalHoras() {
        return totalHoras;
    }
}
