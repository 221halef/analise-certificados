package br.com.atividadescomplementares.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage palcoPrincipal) {
        new JanelaPrincipal().iniciar(palcoPrincipal);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
