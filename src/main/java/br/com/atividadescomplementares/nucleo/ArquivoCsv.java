package br.com.atividadescomplementares.nucleo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArquivoCsv {

    private static final char SEPARADOR = ';';
    private static final String CABECALHO =
            "arquivo;hash;titulo;categoria;emissor;data;horas;status;codigo_verificacao;participante;ignorado";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exportar(List<Atividade> atividades, Path destino) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write('﻿');
            escritor.write(CABECALHO);
            escritor.newLine();

            for (Atividade atividade : atividades) {
                escritor.write(construirLinha(atividade));
                escritor.newLine();
            }
        }
    }

    private String construirLinha(Atividade atividade) {
        List<String> campos = new ArrayList<>();
        campos.add(atividade.getArquivoNome());
        campos.add(atividade.getHashSha256());
        campos.add(atividade.getTitulo());
        campos.add(atividade.getCategoria().getNomeExibicao());
        campos.add(atividade.getEmissor());
        campos.add(atividade.getData() == null ? "" : atividade.getData().format(FORMATO_DATA));
        campos.add(formatarNumero(atividade.getHorasDeclaradas()));
        campos.add(atividade.getStatus().getNomeExibicao());
        campos.add(atividade.getCodigoVerificacao());
        campos.add(atividade.getParticipante());
        campos.add(atividade.isIgnorado() ? "sim" : "não");

        StringBuilder linha = new StringBuilder();
        for (int i = 0; i < campos.size(); i++) {
            if (i > 0) {
                linha.append(SEPARADOR);
            }
            linha.append(escaparCampo(campos.get(i)));
        }
        return linha.toString();
    }

    private String formatarNumero(double numero) {
        String texto = String.valueOf(numero);
        return texto.replace('.', ',');
    }

    private String escaparCampo(String campo) {
        if (campo == null) {
            return "";
        }
        boolean precisaDeAspas = campo.indexOf(SEPARADOR) >= 0
                || campo.indexOf('"') >= 0
                || campo.indexOf('\n') >= 0;
        if (!precisaDeAspas) {
            return campo;
        }
        String escapado = campo.replace("\"", "\"\"");
        return "\"" + escapado + "\"";
    }

    public List<Atividade> abrir(Path origem) throws IOException {
        List<Atividade> atividades = new ArrayList<>();

        try (BufferedReader leitor = Files.newBufferedReader(origem, StandardCharsets.UTF_8)) {
            String cabecalho = leitor.readLine();
            if (cabecalho == null) {
                return atividades;
            }
            Map<String, Integer> indices = mapearColunas(removerBom(cabecalho));

            String linha = leitor.readLine();
            while (linha != null) {
                if (!linha.isBlank()) {
                    Atividade atividade = interpretarLinha(linha, indices);
                    if (atividade != null) {
                        atividades.add(atividade);
                    }
                }
                linha = leitor.readLine();
            }
        }

        return atividades;
    }

    private Map<String, Integer> mapearColunas(String cabecalho) {
        Map<String, Integer> indices = new HashMap<>();
        List<String> nomes = dividirLinha(cabecalho);
        for (int i = 0; i < nomes.size(); i++) {
            indices.put(nomes.get(i).trim().toLowerCase(), i);
        }
        return indices;
    }

    private String valorDaColuna(List<String> campos, Map<String, Integer> indices, String... nomesAceitos) {
        for (String nome : nomesAceitos) {
            Integer indice = indices.get(nome);
            if (indice != null && indice < campos.size()) {
                return campos.get(indice);
            }
        }
        return "";
    }

    private String removerBom(String linha) {
        if (!linha.isEmpty() && linha.charAt(0) == '﻿') {
            return linha.substring(1);
        }
        return linha;
    }

    private Atividade interpretarLinha(String linha, Map<String, Integer> indices) {
        List<String> campos = dividirLinha(linha);
        if (campos.size() < indices.size() || !indices.containsKey("arquivo") || !indices.containsKey("hash")) {
            return null;
        }

        String arquivoNome = valorDaColuna(campos, indices, "arquivo");
        String hash = valorDaColuna(campos, indices, "hash");
        String titulo = valorDaColuna(campos, indices, "titulo");
        Categoria categoria = categoriaPorNome(valorDaColuna(campos, indices, "categoria"));
        String emissor = valorDaColuna(campos, indices, "emissor");
        LocalDate data = interpretarData(valorDaColuna(campos, indices, "data"));
        double horas = interpretarNumero(valorDaColuna(campos, indices, "horas", "horas_declaradas"));
        Status status = statusPorNome(valorDaColuna(campos, indices, "status"));
        String codigoVerificacao = valorDaColuna(campos, indices, "codigo_verificacao");
        String participante = valorDaColuna(campos, indices, "participante");
        String textoIgnorado = valorDaColuna(campos, indices, "ignorado");
        boolean ignorado = textoIgnorado.equalsIgnoreCase("sim") || textoIgnorado.equalsIgnoreCase("true");

        Atividade atividade = new Atividade(arquivoNome, Path.of(arquivoNome), hash);
        atividade.setTitulo(tratarVazio(titulo));
        atividade.setCategoria(categoria);
        atividade.setEmissor(tratarVazio(emissor));
        atividade.setData(data);
        atividade.setHorasDeclaradas(horas);
        atividade.setStatus(status);
        atividade.setCodigoVerificacao(tratarVazio(codigoVerificacao));
        atividade.setParticipante(tratarVazio(participante));
        atividade.setIgnorado(ignorado);

        return atividade;
    }

    private String tratarVazio(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto;
    }

    private LocalDate interpretarData(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (Exception erro) {
            return null;
        }
    }

    private double interpretarNumero(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException erro) {
            return 0;
        }
    }

    private Categoria categoriaPorNome(String nome) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.getNomeExibicao().equalsIgnoreCase(nome) || categoria.name().equalsIgnoreCase(nome)) {
                return categoria;
            }
        }
        return Categoria.OUTROS;
    }

    private Status statusPorNome(String nome) {
        for (Status status : Status.values()) {
            if (status.getNomeExibicao().equalsIgnoreCase(nome) || status.name().equalsIgnoreCase(nome)) {
                return status;
            }
        }
        return Status.OK;
    }

    private List<String> dividirLinha(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder campoAtual = new StringBuilder();
        boolean dentroDeAspas = false;

        int i = 0;
        while (i < linha.length()) {
            char caractere = linha.charAt(i);

            if (dentroDeAspas) {
                if (caractere == '"') {
                    if (i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                        campoAtual.append('"');
                        i++;
                    } else {
                        dentroDeAspas = false;
                    }
                } else {
                    campoAtual.append(caractere);
                }
            } else {
                if (caractere == '"') {
                    dentroDeAspas = true;
                } else if (caractere == SEPARADOR) {
                    campos.add(campoAtual.toString());
                    campoAtual = new StringBuilder();
                } else {
                    campoAtual.append(caractere);
                }
            }
            i++;
        }
        campos.add(campoAtual.toString());

        return campos;
    }
}
