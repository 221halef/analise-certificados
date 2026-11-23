package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidacaoTest {

    private Atividade completa() {
        Atividade atividade = new Atividade("a.pdf", Path.of("a.pdf"), "h");
        atividade.setTitulo("Titulo");
        atividade.setCategoria(Categoria.CURSO);
        atividade.setData(LocalDate.of(2026, 1, 10));
        atividade.setHorasDeclaradas(20);
        return atividade;
    }

    @Test
    void emissorEParticipanteSaoOpcionais() {
        Atividade atividade = completa();
        Validacao.atualizarStatus(atividade);
        assertEquals(Status.OK, atividade.getStatus());
    }

    @Test
    void faltandoHorasDataTituloOuCategoriaFicaEmRevisar() {
        Atividade semHoras = completa();
        semHoras.setHorasDeclaradas(0);
        Validacao.atualizarStatus(semHoras);
        assertEquals(Status.REVISAR, semHoras.getStatus());
        assertEquals("campos faltando: horas", semHoras.getObservacao());

        Atividade semCategoria = completa();
        semCategoria.setCategoria(Categoria.OUTROS);
        semCategoria.setData(null);
        Validacao.atualizarStatus(semCategoria);
        assertEquals(Status.REVISAR, semCategoria.getStatus());
        assertEquals("campos faltando: categoria, data", semCategoria.getObservacao());
    }

    @Test
    void salvarSemPreencherNaoValida() {
        Atividade atividade = completa();
        atividade.setHorasDeclaradas(0);
        Validacao.atualizarStatus(atividade);
        atividade.setEditado(true);
        Validacao.atualizarStatus(atividade);
        assertEquals(Status.REVISAR, atividade.getStatus());
    }

    @Test
    void preencherOQueFaltavaValidaELimpaOMotivo() {
        Atividade atividade = completa();
        atividade.setHorasDeclaradas(0);
        Validacao.atualizarStatus(atividade);
        atividade.setHorasDeclaradas(8);
        Validacao.atualizarStatus(atividade);
        assertEquals(Status.OK, atividade.getStatus());
        assertEquals("", atividade.getObservacao());
    }

    @Test
    void duplicadosNaoSaoAlterados() {
        Atividade atividade = completa();
        atividade.setStatus(Status.DUPLICADO);
        atividade.setHorasDeclaradas(0);
        Validacao.atualizarStatus(atividade);
        assertEquals(Status.DUPLICADO, atividade.getStatus());
    }
}
