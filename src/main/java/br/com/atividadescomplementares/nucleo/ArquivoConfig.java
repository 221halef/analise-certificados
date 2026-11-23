package br.com.atividadescomplementares.nucleo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ArquivoConfig {

    private static final String CHAVE_TEMA = "tema";
    private static final String CHAVE_PASTA = "pasta";

    public String lerTema(Path arquivo) throws IOException {
        return carregar(arquivo).getProperty(CHAVE_TEMA, "claro");
    }

    public void salvarTema(String tema, Path arquivo) throws IOException {
        Properties propriedades = carregar(arquivo);
        propriedades.setProperty(CHAVE_TEMA, tema);
        gravar(propriedades, arquivo);
    }

    public Path lerPasta(Path arquivo) throws IOException {
        String valor = carregar(arquivo).getProperty(CHAVE_PASTA, "");
        if (valor.isBlank()) {
            return null;
        }
        try {
            return Path.of(valor);
        } catch (java.nio.file.InvalidPathException erro) {
            return null;
        }
    }

    public void salvarPasta(Path pasta, Path arquivo) throws IOException {
        Properties propriedades = carregar(arquivo);
        if (pasta == null) {
            propriedades.remove(CHAVE_PASTA);
        } else {
            propriedades.setProperty(CHAVE_PASTA, pasta.toString());
        }
        gravar(propriedades, arquivo);
    }

    private Properties carregar(Path arquivo) throws IOException {
        Properties propriedades = new Properties();
        if (Files.exists(arquivo)) {
            try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                propriedades.load(leitor);
            }
        }
        return propriedades;
    }

    private void gravar(Properties propriedades, Path arquivo) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            propriedades.store(escritor, null);
        }
    }
}
