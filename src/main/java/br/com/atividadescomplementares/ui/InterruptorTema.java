package br.com.atividadescomplementares.ui;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.function.Consumer;

public final class InterruptorTema extends StackPane {

    private static final double LARGURA = 60;
    private static final double ALTURA = 32;
    private static final double DIAMETRO = 26;
    private static final double MARGEM = 3;
    private static final double DESLOCAMENTO = LARGURA - DIAMETRO - 2 * MARGEM;

    private final StackPane botao = new StackPane();
    private final Consumer<Boolean> aoAlternar;
    private boolean escuro;

    public InterruptorTema(boolean escuroInicial, Consumer<Boolean> aoAlternar) {
        this.escuro = escuroInicial;
        this.aoAlternar = aoAlternar;

        Region trilho = new Region();
        trilho.getStyleClass().add("interruptor-trilho");
        trilho.setPrefSize(LARGURA, ALTURA);

        botao.getStyleClass().add("interruptor-botao");
        botao.setMinSize(DIAMETRO, DIAMETRO);
        botao.setPrefSize(DIAMETRO, DIAMETRO);
        botao.setMaxSize(DIAMETRO, DIAMETRO);
        StackPane.setAlignment(botao, Pos.CENTER_LEFT);
        StackPane.setMargin(botao, new Insets(0, 0, 0, MARGEM));

        getChildren().addAll(trilho, botao);
        setMinSize(LARGURA, ALTURA);
        setPrefSize(LARGURA, ALTURA);
        setMaxSize(LARGURA, ALTURA);
        setCursor(Cursor.HAND);
        setFocusTraversable(true);
        Tooltip.install(this, new Tooltip("Alternar entre tema claro e escuro"));

        setOnMouseClicked(evento -> alternar());
        setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.SPACE || evento.getCode() == KeyCode.ENTER) {
                alternar();
            }
        });

        atualizarAparencia(false);
    }

    private void alternar() {
        escuro = !escuro;
        atualizarAparencia(true);
        aoAlternar.accept(escuro);
    }

    private void atualizarAparencia(boolean comAnimacao) {
        getStyleClass().remove("interruptor-escuro");
        if (escuro) {
            getStyleClass().add("interruptor-escuro");
        }

        botao.getChildren().setAll(Icones.criar(escuro ? "sol" : "lua", 16));

        double destino = escuro ? 0 : DESLOCAMENTO;
        if (comAnimacao) {
            TranslateTransition transicao = new TranslateTransition(Duration.millis(160), botao);
            transicao.setToX(destino);
            transicao.play();
        } else {
            botao.setTranslateX(destino);
        }
    }
}
