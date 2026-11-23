package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArquivoCsvTest {

    @TempDir
    Path pastaTemporaria;

    @Test
    void exportarEAbrirMantemOsDados() throws IOException {
        Atividade original = new Atividade("palestra.pdf", Path.of("palestra.pdf"), "abc123");
        original.setTitulo("Palestra; com \"aspas\"");
        original.setCategoria(Categoria.PALESTRA);
        original.setEmissor("Faculdade Exemplo");
        original.setData(LocalDate.of(2026, 3, 12));
        original.setHorasDeclaradas(3.5);
        original.setStatus(Status.REVISAR);
        original.setIgnorado(true);

        Path arquivo = pastaTemporaria.resolve("saida.csv");
        ArquivoCsv arquivoCsv = new ArquivoCsv();
        arquivoCsv.exportar(List.of(original), arquivo);

        List<Atividade> lidas = arquivoCsv.abrir(arquivo);

        assertEquals(1, lidas.size());
        Atividade lida = lidas.get(0);
        assertEquals("Palestra; com \"aspas\"", lida.getTitulo());
        assertEquals(Categoria.PALESTRA, lida.getCategoria());
        assertEquals(LocalDate.of(2026, 3, 12), lida.getData());
        assertEquals(3.5, lida.getHorasDeclaradas());
        assertEquals(Status.REVISAR, lida.getStatus());
        assertTrue(lida.isIgnorado());
    }

    @Test
    void exportacaoNaoTemColunaDeHorasValidas() throws IOException {
        Path arquivo = pastaTemporaria.resolve("saida.csv");
        new ArquivoCsv().exportar(List.of(), arquivo);

        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(conteudo.contains("horas;status"));
        assertTrue(!conteudo.contains("validas"));
    }

    @Test
    void abreCsvAntigoComColunasDeHorasDeclaradasEValidas() throws IOException {
        String csvAntigo = "\uFEFFarquivo;hash;titulo;categoria;emissor;data;horas_declaradas;horas_validas;status;codigo_verificacao;participante;ignorado\n"
                + "curso.pdf;h1;Curso de Java;Curso;Escola;15/02/2026;40,0;40,0;OK;ABC;João;não\n";
        Path arquivo = pastaTemporaria.resolve("antigo.csv");
        Files.writeString(arquivo, csvAntigo, StandardCharsets.UTF_8);

        List<Atividade> lidas = new ArquivoCsv().abrir(arquivo);

        assertEquals(1, lidas.size());
        assertEquals(40.0, lidas.get(0).getHorasDeclaradas());
        assertEquals(Categoria.CURSO, lidas.get(0).getCategoria());
    }
}
