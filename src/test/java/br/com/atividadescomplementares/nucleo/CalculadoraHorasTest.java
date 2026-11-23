package br.com.atividadescomplementares.nucleo;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculadoraHorasTest {

    private Atividade criarAtividade(String nome, Categoria categoria, double horas) {
        Atividade atividade = new Atividade(nome, Path.of(nome), "hash-" + nome);
        atividade.setCategoria(categoria);
        atividade.setHorasDeclaradas(horas);
        return atividade;
    }

    @Test
    void somaHorasPorCategoriaETotalGeral() {
        List<Atividade> atividades = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            atividades.add(criarAtividade("palestra" + i + ".pdf", Categoria.PALESTRA, 3));
        }
        atividades.add(criarAtividade("curso1.pdf", Categoria.CURSO, 40));
        atividades.add(criarAtividade("curso2.pdf", Categoria.CURSO, 30));

        ResultadoCalculo resultado = new CalculadoraHoras().calcular(atividades);

        ResumoCategoria palestras = buscarResumo(resultado, Categoria.PALESTRA);
        assertEquals(5, palestras.getQuantidadeItens());
        assertEquals(15, palestras.getHoras());

        ResumoCategoria cursos = buscarResumo(resultado, Categoria.CURSO);
        assertEquals(2, cursos.getQuantidadeItens());
        assertEquals(70, cursos.getHoras());

        assertEquals(85, resultado.getTotalHoras());
    }

    @Test
    void todasAsCategoriasAparecemNoResultadoMesmoSemItens() {
        List<Atividade> atividades = new ArrayList<>();
        atividades.add(criarAtividade("curso1.pdf", Categoria.CURSO, 40));

        ResultadoCalculo resultado = new CalculadoraHoras().calcular(atividades);

        assertEquals(Categoria.values().length, resultado.getResumos().size());
        assertEquals(Categoria.PALESTRA, resultado.getResumos().get(0).getCategoria());

        ResumoCategoria estagio = buscarResumo(resultado, Categoria.ESTAGIO);
        assertEquals(0, estagio.getQuantidadeItens());
        assertEquals(0, estagio.getHoras());
    }

    @Test
    void itensIgnoradosEDuplicadosFicamForaDaSoma() {
        Atividade normal = criarAtividade("curso1.pdf", Categoria.CURSO, 10);
        Atividade ignorado = criarAtividade("curso2.pdf", Categoria.CURSO, 10);
        ignorado.setIgnorado(true);
        Atividade duplicado = criarAtividade("curso3.pdf", Categoria.CURSO, 10);
        duplicado.setStatus(Status.DUPLICADO);

        List<Atividade> atividades = new ArrayList<>();
        atividades.add(normal);
        atividades.add(ignorado);
        atividades.add(duplicado);

        ResultadoCalculo resultado = new CalculadoraHoras().calcular(atividades);

        assertEquals(10, resultado.getTotalHoras());
        assertEquals(1, buscarResumo(resultado, Categoria.CURSO).getQuantidadeItens());
    }

    private ResumoCategoria buscarResumo(ResultadoCalculo resultado, Categoria categoria) {
        for (ResumoCategoria resumo : resultado.getResumos()) {
            if (resumo.getCategoria() == categoria) {
                return resumo;
            }
        }
        throw new IllegalStateException("Categoria não encontrada no resultado: " + categoria);
    }
}
