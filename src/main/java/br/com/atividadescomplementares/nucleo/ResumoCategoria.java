package br.com.atividadescomplementares.nucleo;

public class ResumoCategoria {

    private final Categoria categoria;
    private final int quantidadeItens;
    private final double horas;

    public ResumoCategoria(Categoria categoria, int quantidadeItens, double horas) {
        this.categoria = categoria;
        this.quantidadeItens = quantidadeItens;
        this.horas = horas;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public double getHoras() {
        return horas;
    }
}
