package br.com.atividadescomplementares.nucleo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Mantém as escolhas do usuário (categoria arrastada, item ignorado) quando a pasta é lida de novo. */
public final class EdicoesManuais {

    private EdicoesManuais() {
    }

    public static void preservar(List<Atividade> antigas, List<Atividade> novas) {
        Map<Path, Atividade> porCaminho = new HashMap<>();
        for (Atividade antiga : antigas) {
            if (antiga.getCaminhoArquivo() != null) {
                porCaminho.put(antiga.getCaminhoArquivo().toAbsolutePath().normalize(), antiga);
            }
        }

        for (Atividade nova : novas) {
            if (nova.getCaminhoArquivo() == null) {
                continue;
            }
            Atividade antiga = porCaminho.get(nova.getCaminhoArquivo().toAbsolutePath().normalize());
            if (antiga == null) {
                continue;
            }
            if (antiga.isCategoriaManual()) {
                nova.setCategoria(antiga.getCategoria());
                nova.setCategoriaManual(true);
            }
            nova.setIgnorado(antiga.isIgnorado());
            if (antiga.isEditado()) {
                nova.setTitulo(antiga.getTitulo());
                nova.setEmissor(antiga.getEmissor());
                nova.setData(antiga.getData());
                nova.setHorasDeclaradas(antiga.getHorasDeclaradas());
                nova.setCodigoVerificacao(antiga.getCodigoVerificacao());
                nova.setParticipante(antiga.getParticipante());
                nova.setEditado(true);
            }
            if (antiga.isEditado() || antiga.isCategoriaManual()) {
                Validacao.atualizarStatus(nova);
            }
        }
    }
}
