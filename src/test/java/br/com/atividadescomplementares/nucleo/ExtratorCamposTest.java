package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtratorCamposTest {

    @Test
    void extraiCamposDeCertificadoComDataNumericaECargaHorariaPorExtenso() {
        String texto = """
                CERTIFICADO

                Certificamos que Maria Oliveira Souza participou do curso "Introdução à Programação em Java",
                promovido pelo Instituto Alpha de Tecnologia, com carga horária de 40 horas,
                realizado em 12/03/2026.

                Código de verificação: 7F3A9C
                """;

        CamposExtraidos campos = new ExtratorCampos().extrair(texto);

        assertEquals("Introdução à Programação em Java", campos.titulo());
        assertEquals("Instituto Alpha de Tecnologia", campos.emissor());
        assertEquals(LocalDate.of(2026, 3, 12), campos.data());
        assertEquals(40.0, campos.horasDeclaradas());
        assertEquals("7F3A9C", campos.codigoVerificacao());
        assertEquals("Maria Oliveira Souza", campos.participante());
        assertTrue(campos.camposFaltando().isEmpty());
    }

    @Test
    void extraiDataPorExtensoECargaHorariaAbreviada() {
        String texto = """
                Certificamos que João Pedro Lima participou da palestra "Boas práticas de segurança",
                realizado pela Universidade Beta, CH: 8h, no dia 12 de março de 2026.
                """;

        CamposExtraidos campos = new ExtratorCampos().extrair(texto);

        assertEquals(LocalDate.of(2026, 3, 12), campos.data());
        assertEquals(8.0, campos.horasDeclaradas());
        assertEquals("Universidade Beta", campos.emissor());
    }

    @Test
    void certificadoDeEstagioSemCargaHorariaFicaComStatusRevisarENaoQuebra() {
        String texto = """
                Certificamos que Ana Paula Ferreira realizou estágio na empresa Beta Soluções,
                no período de 01/02/2026 a 30/04/2026.
                """;

        CamposExtraidos campos = new ExtratorCampos().extrair(texto);

        assertNull(campos.horasDeclaradas());
        assertTrue(campos.camposFaltando().contains("horas"));
        assertFalse(campos.camposFaltando().contains("participante"));
        assertFalse(campos.camposFaltando().contains("data"));
    }

    @Test
    void textoSemCamposReconheciveisMarcaTodosComoFaltando() {
        CamposExtraidos campos = new ExtratorCampos().extrair("Documento sem nenhum padrão reconhecido.");

        assertTrue(campos.camposFaltando().contains("titulo"));
        assertTrue(campos.camposFaltando().contains("emissor"));
        assertTrue(campos.camposFaltando().contains("data"));
        assertTrue(campos.camposFaltando().contains("horas"));
        assertTrue(campos.camposFaltando().contains("participante"));
    }
}
