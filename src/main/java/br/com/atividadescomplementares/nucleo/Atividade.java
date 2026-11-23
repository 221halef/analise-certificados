package br.com.atividadescomplementares.nucleo;

import java.nio.file.Path;
import java.time.LocalDate;

public class Atividade {

    private final String arquivoNome;
    private final Path caminhoArquivo;
    private final String hashSha256;

    private String titulo;
    private Categoria categoria;
    private String emissor;
    private LocalDate data;
    private double horasDeclaradas;
    private String codigoVerificacao;
    private String participante;
    private Status status;
    private String observacao;
    private boolean ignorado;
    private boolean categoriaManual;
    private boolean editado;

    public Atividade(String arquivoNome, Path caminhoArquivo, String hashSha256) {
        this.arquivoNome = arquivoNome;
        this.caminhoArquivo = caminhoArquivo;
        this.hashSha256 = hashSha256;
        this.categoria = Categoria.OUTROS;
        this.status = Status.OK;
        this.observacao = "";
        this.ignorado = false;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public Path getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public String getHashSha256() {
        return hashSha256;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        this.emissor = emissor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getHorasDeclaradas() {
        return horasDeclaradas;
    }

    public void setHorasDeclaradas(double horasDeclaradas) {
        this.horasDeclaradas = horasDeclaradas;
    }

    public String getCodigoVerificacao() {
        return codigoVerificacao;
    }

    public void setCodigoVerificacao(String codigoVerificacao) {
        this.codigoVerificacao = codigoVerificacao;
    }

    public String getParticipante() {
        return participante;
    }

    public void setParticipante(String participante) {
        this.participante = participante;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void adicionarObservacao(String novaObservacao) {
        if (observacao == null || observacao.isBlank()) {
            observacao = novaObservacao;
        } else {
            observacao = observacao + "; " + novaObservacao;
        }
    }

    public boolean isIgnorado() {
        return ignorado;
    }

    public void setIgnorado(boolean ignorado) {
        this.ignorado = ignorado;
    }

    public boolean isCategoriaManual() {
        return categoriaManual;
    }

    public void setCategoriaManual(boolean categoriaManual) {
        this.categoriaManual = categoriaManual;
    }

    public boolean isEditado() {
        return editado;
    }

    public void setEditado(boolean editado) {
        this.editado = editado;
    }

    public boolean contaNaSoma() {
        return !ignorado && status != Status.DUPLICADO;
    }
}
