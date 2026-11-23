package br.com.atividadescomplementares.nucleo;

import java.util.ArrayList;
import java.util.List;

public class CalculadoraHoras {

    public ResultadoCalculo calcular(List<Atividade> atividades) {
        List<ResumoCategoria> resumos = new ArrayList<>();
        double totalHoras = 0;

        for (Categoria categoria : Categoria.values()) {
            int quantidade = 0;
            double horasCategoria = 0;

            for (Atividade atividade : atividades) {
                if (atividade.getCategoria() == categoria && atividade.contaNaSoma()) {
                    quantidade++;
                    horasCategoria += atividade.getHorasDeclaradas();
                }
            }

            resumos.add(new ResumoCategoria(categoria, quantidade, horasCategoria));
            totalHoras += horasCategoria;
        }

        return new ResultadoCalculo(resumos, totalHoras);
    }
}
