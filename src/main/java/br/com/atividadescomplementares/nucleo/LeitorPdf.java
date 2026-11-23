package br.com.atividadescomplementares.nucleo;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeitorPdf {

    public List<Path> listarPdfs(Path pasta) throws IOException {
        List<Path> arquivos = new ArrayList<>();
        try (DirectoryStream<Path> conteudoPasta = Files.newDirectoryStream(pasta, "*.pdf")) {
            for (Path arquivo : conteudoPasta) {
                if (Files.isRegularFile(arquivo)) {
                    arquivos.add(arquivo);
                }
            }
        }
        Collections.sort(arquivos, Comparator.comparing(a -> a.getFileName().toString()));
        return arquivos;
    }

    public TextoExtraido ler(Path arquivo) {
        String nomeArquivo = arquivo.getFileName().toString();
        try {
            byte[] bytes = Files.readAllBytes(arquivo);
            String hash = calcularHashSha256(bytes);
            String texto = extrairTexto(bytes);
            boolean semTexto = texto == null || texto.isBlank();
            return new TextoExtraido(nomeArquivo, arquivo, hash, semTexto ? "" : texto, semTexto);
        } catch (IOException | NoSuchAlgorithmException erro) {
            return new TextoExtraido(nomeArquivo, arquivo, "", "", true);
        }
    }

    private String extrairTexto(byte[] bytes) throws IOException {
        try (PDDocument documento = Loader.loadPDF(bytes)) {
            PDFTextStripper extrator = new PDFTextStripper();
            return extrator.getText(documento);
        }
    }

    private String calcularHashSha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digestor = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digestor.digest(bytes);
        StringBuilder textoHash = new StringBuilder();
        for (byte b : hashBytes) {
            textoHash.append(String.format("%02x", b));
        }
        return textoHash.toString();
    }
}
