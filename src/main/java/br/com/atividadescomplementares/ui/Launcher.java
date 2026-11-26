package br.com.atividadescomplementares.ui;

import javafx.application.Application;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Launcher {

    private static final String NOME_PASTA_DADOS = ".atividades-complementares";
    private static final String NOME_ARQUIVO_LOG = "erro-inicializacao.log";

    private Launcher() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(Launcher::registrarErro);
        try {
            Application.launch(App.class, args);
        } catch (Throwable erro) {
            registrarErro(Thread.currentThread(), erro);
        }
    }

    private static void registrarErro(Thread thread, Throwable erro) {
        StringWriter texto = new StringWriter();
        try (PrintWriter escritor = new PrintWriter(texto)) {
            escritor.println("Falha ao iniciar o SOGAC");
            escritor.println("Data: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            escritor.println("Sistema: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            escritor.println("Java: " + System.getProperty("java.version"));
            escritor.println("Diretório: " + System.getProperty("user.dir"));
            escritor.println("Classpath: " + System.getProperty("java.class.path"));
            escritor.println("Thread: " + thread.getName());
            erro.printStackTrace(escritor);
        }

        String mensagem = texto.toString();
        System.err.print(mensagem);

        try {
            Path pasta = Path.of(System.getProperty("user.home"), NOME_PASTA_DADOS);
            Files.createDirectories(pasta);
            Files.writeString(pasta.resolve(NOME_ARQUIVO_LOG), mensagem, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (Exception falhaLog) {
            System.err.println("Não foi possível gravar o arquivo de log: " + falhaLog);
        }
    }
}
