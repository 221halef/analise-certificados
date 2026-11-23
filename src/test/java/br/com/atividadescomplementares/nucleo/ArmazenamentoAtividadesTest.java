package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArmazenamentoAtividadesTest {

    @TempDir
    Path pasta;

    @Test
    void salvarECarregarMantemCaminhoCategoriaManualEObservacao() throws IOException {
        Path pdf = pasta.resolve("certificados").resolve("a.pdf");
        Atividade atividade = new Atividade("a.pdf", pdf, "hash1");
        atividade.setTitulo("Titulo");
        atividade.setCategoria(Categoria.EXTENSAO);
        atividade.setCategoriaManual(true);
        atividade.setObservacao("campos faltando: data");
        atividade.setData(LocalDate.of(2026, 5, 1));
        atividade.setHorasDeclaradas(10);

        ArmazenamentoAtividades armazenamento = new ArmazenamentoAtividades();
        Path destino = pasta.resolve("atividades.csv");
        armazenamento.salvar(List.of(atividade), destino);

        List<Atividade> lidas = armazenamento.carregar(destino);
        assertEquals(1, lidas.size());
        Atividade lida = lidas.get(0);
        assertEquals(pdf, lida.getCaminhoArquivo());
        assertEquals(Categoria.EXTENSAO, lida.getCategoria());
        assertTrue(lida.isCategoriaManual());
        assertEquals("campos faltando: data", lida.getObservacao());
    }

    @Test
    void carregarSemArquivoDevolveListaVazia() throws IOException {
        assertTrue(new ArmazenamentoAtividades().carregar(pasta.resolve("nao-existe.csv")).isEmpty());
    }

    @Test
    void edicoesManuaisSobrevivemAoReprocessamento() {
        Path pdf = pasta.resolve("a.pdf");
        Atividade antiga = new Atividade("a.pdf", pdf, "h");
        antiga.setCategoria(Categoria.EXTENSAO);
        antiga.setCategoriaManual(true);
        antiga.setIgnorado(true);

        Atividade nova = new Atividade("a.pdf", pdf, "h");
        nova.setCategoria(Categoria.CURSO);
        antiga.setEditado(true);
        antiga.setTitulo("Titulo corrigido");
        antiga.setHorasDeclaradas(12);
        nova.setStatus(Status.REVISAR);
        nova.setObservacao("campos faltando: horas");
        Atividade outra = new Atividade("b.pdf", pasta.resolve("b.pdf"), "h2");
        outra.setCategoria(Categoria.PALESTRA);

        EdicoesManuais.preservar(new ArrayList<>(List.of(antiga)), List.of(nova, outra));

        assertEquals(Categoria.EXTENSAO, nova.getCategoria());
        assertTrue(nova.isIgnorado());
        assertEquals("Titulo corrigido", nova.getTitulo());
        assertEquals(12, nova.getHorasDeclaradas());
        assertEquals(Status.OK, nova.getStatus());
        assertEquals(Categoria.PALESTRA, outra.getCategoria());
    }

    @Test
    void configMantemPastaETemaJuntos() throws IOException {
        ArquivoConfig config = new ArquivoConfig();
        Path arquivo = pasta.resolve("config.properties");
        config.salvarTema("escuro", arquivo);
        config.salvarPasta(pasta, arquivo);
        config.salvarTema("claro", arquivo);

        assertEquals("claro", config.lerTema(arquivo));
        assertEquals(pasta, config.lerPasta(arquivo));
    }
}
