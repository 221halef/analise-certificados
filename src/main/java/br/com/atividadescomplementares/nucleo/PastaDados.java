package br.com.atividadescomplementares.nucleo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PastaDados {

    private static final String NOME_PASTA = ".atividades-complementares";

    public Path obterPasta() throws IOException {
        Path pasta = Path.of(System.getProperty("user.home"), NOME_PASTA);
        if (!Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }
        return pasta;
    }

    public Path garantirArquivoPadrao(String nomeArquivo, String recursoClasspath) throws IOException {
        Path pasta = obterPasta();
        Path arquivo = pasta.resolve(nomeArquivo);
        if (!Files.exists(arquivo)) {
            try (InputStream entrada = getClass().getResourceAsStream(recursoClasspath)) {
                if (entrada != null) {
                    Files.copy(entrada, arquivo, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createFile(arquivo);
                }
            }
        }
        return arquivo;
    }
}
