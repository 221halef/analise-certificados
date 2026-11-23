package br.com.atividadescomplementares.nucleo;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Classificador {

    // Ordem em que as categorias são testadas: a primeira que casar vence.
    // Extensão vem antes de Curso porque declarações de extensão costumam citar o "curso" do aluno.
    private static final List<Categoria> ORDEM_DE_BUSCA = List.of(
            Categoria.EXTENSAO, Categoria.PALESTRA, Categoria.CURSO, Categoria.ESTAGIO, Categoria.MONITORIA);

    private final Map<Categoria, List<String>> palavrasChave;

    public Classificador(Map<Categoria, List<String>> palavrasChave) {
        this.palavrasChave = palavrasChave;
    }

    public static Classificador carregarDe(Path arquivoProperties) throws IOException {
        Properties propriedades = new Properties();
        try (BufferedReader entrada = Files.newBufferedReader(arquivoProperties, StandardCharsets.UTF_8)) {
            propriedades.load(entrada);
        }

        Map<Categoria, List<String>> palavrasChave = new EnumMap<>(Categoria.class);
        for (Categoria categoria : Categoria.values()) {
            if (categoria == Categoria.OUTROS) {
                continue;
            }
            String chave = categoria.name().toLowerCase();
            String valor = propriedades.getProperty(chave, "");
            List<String> palavras = new ArrayList<>();
            for (String palavra : valor.split(",")) {
                String palavraLimpa = palavra.trim();
                if (!palavraLimpa.isEmpty()) {
                    palavras.add(palavraLimpa);
                }
            }
            palavrasChave.put(categoria, palavras);
        }

        return new Classificador(palavrasChave);
    }

    public Categoria classificar(String titulo, String textoCompleto) {
        String textoBusca = ((titulo == null ? "" : titulo) + " " + (textoCompleto == null ? "" : textoCompleto))
                .toLowerCase();

        for (Categoria categoria : ORDEM_DE_BUSCA) {
            List<String> palavras = palavrasChave.get(categoria);
            if (palavras == null) {
                continue;
            }
            for (String palavra : palavras) {
                if (textoBusca.contains(palavra.toLowerCase())) {
                    return categoria;
                }
            }
        }

        return Categoria.OUTROS;
    }
}
