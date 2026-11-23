package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassificadorTest {

    private Classificador carregarPadrao() throws IOException {
        Path arquivo = Files.createTempFile("categorias", ".properties");
        try (InputStream entrada = getClass().getResourceAsStream("/categorias.properties")) {
            Files.copy(entrada, arquivo, StandardCopyOption.REPLACE_EXISTING);
        }
        return Classificador.carregarDe(arquivo);
    }

    @Test
    void declaracaoDeExtensaoNaoCaiEmCursoMesmoCitandoOCursoDoAluno() throws IOException {
        Classificador classificador = carregarPadrao();
        String texto = "Declaração. Declaramos que o aluno, do curso de Ciência da Computação, participou do projeto.";
        assertEquals(Categoria.EXTENSAO, classificador.classificar("Projeto X", texto));
    }

    @Test
    void palavraDeclaracaoSozinhaClassificaComoExtensao() throws IOException {
        assertEquals(Categoria.EXTENSAO, carregarPadrao().classificar(null, "DECLARAÇÃO de participação"));
    }

    @Test
    void certificadoEmInglesComCourseVaiParaCurso() throws IOException {
        assertEquals(Categoria.CURSO, carregarPadrao().classificar("Java", "Certificate of completion of the course Java"));
    }

    @Test
    void cursoPalestraEOutrosContinuamFuncionando() throws IOException {
        Classificador classificador = carregarPadrao();
        assertEquals(Categoria.CURSO, classificador.classificar("Curso de Java", "carga horária 20h"));
        assertEquals(Categoria.PALESTRA, classificador.classificar("Palestra de segurança", ""));
        assertEquals(Categoria.OUTROS, classificador.classificar("Algo", "sem palavras conhecidas"));
    }
}
