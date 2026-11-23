package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DetectorDuplicadosTest {

    @Test
    void mesmoHashViraDuplicadoEFicaForaDaSoma() {
        Atividade original = new Atividade("certificado_a.pdf", Path.of("certificado_a.pdf"), "hash-igual");
        Atividade copia = new Atividade("certificado_b.pdf", Path.of("certificado_b.pdf"), "hash-igual");

        new DetectorDuplicados().detectar(List.of(original, copia));

        assertEquals(Status.OK, original.getStatus());
        assertEquals(Status.DUPLICADO, copia.getStatus());
        assertEquals(true, original.contaNaSoma());
        assertEquals(false, copia.contaNaSoma());
    }

    @Test
    void mesmosCamposComHashesDiferentesViraPossivelDuplicado() {
        Atividade original = criarAtividade("hash-1");
        Atividade possivelCopia = criarAtividade("hash-2");

        new DetectorDuplicados().detectar(List.of(original, possivelCopia));

        assertEquals(Status.OK, original.getStatus());
        assertEquals(Status.POSSIVEL_DUPLICADO, possivelCopia.getStatus());
        assertEquals(true, possivelCopia.contaNaSoma());
    }

    @Test
    void camposDiferentesNaoGeramAlertaDeDuplicado() {
        Atividade atividade1 = criarAtividade("hash-1");
        Atividade atividade2 = criarAtividade("hash-2");
        atividade2.setHorasDeclaradas(99);

        new DetectorDuplicados().detectar(List.of(atividade1, atividade2));

        assertEquals(Status.OK, atividade1.getStatus());
        assertEquals(Status.OK, atividade2.getStatus());
    }

    private Atividade criarAtividade(String hash) {
        Atividade atividade = new Atividade(hash + ".pdf", Path.of(hash + ".pdf"), hash);
        atividade.setTitulo("Curso de Introdução à Programação");
        atividade.setEmissor("Instituto Alpha de Tecnologia");
        atividade.setData(LocalDate.of(2026, 3, 12));
        atividade.setHorasDeclaradas(40);
        return atividade;
    }
}
