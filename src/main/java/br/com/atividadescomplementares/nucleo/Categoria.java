package br.com.atividadescomplementares.nucleo;

public enum Categoria {
    PALESTRA("Palestra", "Palestras"),
    CURSO("Curso", "Cursos"),
    EXTENSAO("Extensão", "Extensão"),
    ESTAGIO("Estágio", "Estágio"),
    MONITORIA("Monitoria", "Monitoria"),
    OUTROS("Outros", "Outros");

    private final String nomeExibicao;
    private final String nomePlural;

    Categoria(String nomeExibicao, String nomePlural) {
        this.nomeExibicao = nomeExibicao;
        this.nomePlural = nomePlural;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getNomePlural() {
        return nomePlural;
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
