package br.com.atividadescomplementares.nucleo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ArquivoConfig {

    public String lerTema(Path arquivo) throws IOException {
        Properties propriedades = new Properties();
        if (Files.exists(arquivo)) {
            try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
                propriedades.load(leitor);
            }
        }
        return propriedades.getProperty("tema", "claro");
    }

    public void salvarTema(String tema, Path arquivo) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
            escritor.write("tema=" + tema);
            escritor.newLine();
        }
    }
}
