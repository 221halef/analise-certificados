package br.com.atividadescomplementares.nucleo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectorDuplicados {

    public void detectar(List<Atividade> atividades) {
        detectarExatos(atividades);
        detectarPossiveis(atividades);
    }

    private void detectarExatos(List<Atividade> atividades) {
        Map<String, List<Atividade>> porHash = new HashMap<>();

        for (Atividade atividade : atividades) {
            String hash = atividade.getHashSha256();
            if (hash == null || hash.isBlank()) {
                continue;
            }
            List<Atividade> grupo = porHash.get(hash);
            if (grupo == null) {
                grupo = new ArrayList<>();
                porHash.put(hash, grupo);
            }
            grupo.add(atividade);
        }

        for (List<Atividade> grupo : porHash.values()) {
            if (grupo.size() <= 1) {
                continue;
            }
            Atividade original = grupo.get(0);
            for (int i = 1; i < grupo.size(); i++) {
                Atividade duplicado = grupo.get(i);
                duplicado.setStatus(Status.DUPLICADO);
                duplicado.adicionarObservacao("mesmo arquivo que " + original.getArquivoNome());
            }
        }
    }

    private void detectarPossiveis(List<Atividade> atividades) {
        Map<String, List<Atividade>> porChave = new HashMap<>();

        for (Atividade atividade : atividades) {
            if (atividade.getStatus() == Status.DUPLICADO) {
                continue;
            }
            String chave = construirChave(atividade);
            if (chave == null) {
                continue;
            }
            List<Atividade> grupo = porChave.get(chave);
            if (grupo == null) {
                grupo = new ArrayList<>();
                porChave.put(chave, grupo);
            }
            grupo.add(atividade);
        }

        for (List<Atividade> grupo : porChave.values()) {
            if (grupo.size() <= 1) {
                continue;
            }
            Atividade original = grupo.get(0);
            for (int i = 1; i < grupo.size(); i++) {
                Atividade possivelDuplicado = grupo.get(i);
                if (possivelDuplicado.getStatus() == Status.OK) {
                    possivelDuplicado.setStatus(Status.POSSIVEL_DUPLICADO);
                }
                possivelDuplicado.adicionarObservacao("possível duplicado de " + original.getArquivoNome());
            }
        }
    }

    private String construirChave(Atividade atividade) {
        if (atividade.getTitulo() == null || atividade.getTitulo().isBlank()
                || atividade.getData() == null
                || atividade.getEmissor() == null || atividade.getEmissor().isBlank()) {
            return null;
        }

        String tituloNormalizado = normalizarTitulo(atividade.getTitulo());
        String emissorNormalizado = atividade.getEmissor().trim().toLowerCase();

        return tituloNormalizado + "|" + atividade.getData() + "|"
                + atividade.getHorasDeclaradas() + "|" + emissorNormalizado;
    }

    private String normalizarTitulo(String titulo) {
        return titulo.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
