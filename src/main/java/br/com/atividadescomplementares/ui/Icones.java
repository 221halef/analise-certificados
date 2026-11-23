package br.com.atividadescomplementares.ui;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.util.Map;

public final class Icones {

    private static final Map<String, String> CAMINHOS = Map.ofEntries(
            Map.entry("pasta", "M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"),
            Map.entry("arquivo", "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8"),
            Map.entry("baixar", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4 M7 10l5 5 5-5 M12 15V3"),
            Map.entry("atualizar", "M23 4v6h-6 M1 20v-6h6 M3.51 9a9 9 0 0 1 14.85-3.36L23 10 M1 14l4.64 4.36A9 9 0 0 0 20.49 15"),
            Map.entry("ampliar", "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6 M15 3h6v6 M10 14L21 3"),
            Map.entry("lua", "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"),
            Map.entry("sol", "M17 12A5 5 0 1 1 7 12A5 5 0 1 1 17 12 M12 1v2 M12 21v2 M4.22 4.22l1.42 1.42 M18.36 18.36l1.42 1.42 M1 12h2 M21 12h2 M4.22 19.78l1.42-1.42 M18.36 5.64l1.42-1.42"),
            Map.entry("seta-baixo", "M6 9l6 6 6-6"),
            Map.entry("seta-cima", "M18 15l-6-6-6 6"),
            Map.entry("seta-direita", "M9 18l6-6-6-6"),
            Map.entry("capelo", "M12 3L1 9l11 6 11-6z M5 12v5c0 1.7 3.1 3 7 3s7-1.3 7-3v-5 M23 9v6"),
            Map.entry("documento-corpo", "M6 2h8l6 6v12a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z"),
            Map.entry("documento-linhas", "M8 13h8 M8 17h8 M8 9h3")
    );

    private Icones() {
    }

    public static StackPane criar(String nome, double tamanho) {
        SVGPath caminho = new SVGPath();
        caminho.setContent(CAMINHOS.get(nome));
        caminho.getStyleClass().addAll("icone", "icone-" + nome);
        escalar(caminho, tamanho / 24.0);
        return embalar(new Group(caminho), tamanho);
    }

    public static StackPane documento(double tamanho) {
        SVGPath corpo = new SVGPath();
        corpo.setContent(CAMINHOS.get("documento-corpo"));
        corpo.getStyleClass().add("icone-documento-corpo");

        SVGPath linhas = new SVGPath();
        linhas.setContent(CAMINHOS.get("documento-linhas"));
        linhas.getStyleClass().add("icone-documento-linhas");

        Group grupo = new Group(corpo, linhas);
        grupo.setScaleX(tamanho / 24.0);
        grupo.setScaleY(tamanho / 24.0);
        return embalar(new Group(grupo), tamanho);
    }

    public static StackPane circuloStatus(boolean confirmado, String classeCor, double tamanho) {
        Circle circulo = new Circle(tamanho / 2);
        circulo.getStyleClass().addAll("circulo-status", classeCor);

        SVGPath simbolo = new SVGPath();
        simbolo.setContent(confirmado ? "M20 6L9 17l-5-5" : "M12 7v6 M12 17h.01");
        simbolo.getStyleClass().add("simbolo-status");
        escalar(simbolo, tamanho / 24.0 * 0.62);

        StackPane caixa = new StackPane(circulo, new Group(simbolo));
        fixarTamanho(caixa, tamanho);
        return caixa;
    }

    private static void escalar(Node no, double escala) {
        no.setScaleX(escala);
        no.setScaleY(escala);
    }

    private static StackPane embalar(Node conteudo, double tamanho) {
        StackPane caixa = new StackPane(conteudo);
        fixarTamanho(caixa, tamanho);
        return caixa;
    }

    private static void fixarTamanho(StackPane caixa, double tamanho) {
        caixa.setMinSize(tamanho, tamanho);
        caixa.setPrefSize(tamanho, tamanho);
        caixa.setMaxSize(tamanho, tamanho);
    }
}
