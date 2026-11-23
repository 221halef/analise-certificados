package br.com.atividadescomplementares.nucleo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorPasta {

    private final LeitorPdf leitorPdf;
    private final ExtratorCampos extratorCampos;
    private final DetectorDuplicados detectorDuplicados;
    private final CalculadoraHoras calculadoraHoras;

    public ProcessadorPasta() {
        this.leitorPdf = new LeitorPdf();
        this.extratorCampos = new ExtratorCampos();
        this.detectorDuplicados = new DetectorDuplicados();
        this.calculadoraHoras = new CalculadoraHoras();
    }

    public Processamento processar(Path pasta, Classificador classificador) throws IOException {
        List<Path> arquivos = leitorPdf.listarPdfs(pasta);
        List<Atividade> atividades = new ArrayList<>();

        for (Path arquivo : arquivos) {
            TextoExtraido textoExtraido = leitorPdf.ler(arquivo);
            Atividade atividade = construirAtividade(textoExtraido, classificador);
            atividades.add(atividade);
        }

        detectorDuplicados.detectar(atividades);
        ResultadoCalculo resultado = calculadoraHoras.calcular(atividades);

        return new Processamento(atividades, resultado);
    }

    public ResultadoCalculo recalcular(List<Atividade> atividades) {
        return calculadoraHoras.calcular(atividades);
    }

    private Atividade construirAtividade(TextoExtraido textoExtraido, Classificador classificador) {
        Atividade atividade = new Atividade(
                textoExtraido.arquivoNome(), textoExtraido.caminhoArquivo(), textoExtraido.hashSha256());

        if (textoExtraido.semTextoExtraivel()) {
            atividade.setStatus(Status.REVISAR);
            atividade.setObservacao("sem texto extraível");
            atividade.setHorasDeclaradas(0);
            atividade.setCategoria(Categoria.OUTROS);
            return atividade;
        }

        CamposExtraidos campos = extratorCampos.extrair(textoExtraido.texto());

        atividade.setTitulo(campos.titulo());
        atividade.setEmissor(campos.emissor());
        atividade.setData(campos.data());
        atividade.setHorasDeclaradas(campos.horasDeclaradas() == null ? 0 : campos.horasDeclaradas());
        atividade.setCodigoVerificacao(campos.codigoVerificacao());
        atividade.setParticipante(campos.participante());

        Categoria categoria = classificador.classificar(campos.titulo(), textoExtraido.texto());
        atividade.setCategoria(categoria);

        if (!campos.camposFaltando().isEmpty()) {
            atividade.setStatus(Status.REVISAR);
            atividade.setObservacao("campos faltando: " + String.join(", ", campos.camposFaltando()));
        }

        return atividade;
    }
}
