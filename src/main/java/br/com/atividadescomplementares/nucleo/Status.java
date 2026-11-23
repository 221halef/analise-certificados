package br.com.atividadescomplementares.nucleo;

public enum Status {
    OK("OK"),
    REVISAR("Revisar"),
    DUPLICADO("Duplicado"),
    POSSIVEL_DUPLICADO("Possível duplicado");

    private final String nomeExibicao;

    Status(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
