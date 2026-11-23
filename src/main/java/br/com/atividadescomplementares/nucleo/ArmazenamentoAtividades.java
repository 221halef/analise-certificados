package br.com.atividadescomplementares.nucleo;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Guarda a lista de atividades em disco para que ela volte na próxima vez que o programa abrir. */
public class ArmazenamentoAtividades {

    private final ArquivoCsv arquivoCsv = new ArquivoCsv();

    public void salvar(List<Atividade> atividades, Path destino) throws IOException {
        Path temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
        arquivoCsv.exportar(atividades, temporario);
        try {
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException erro) {
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public List<Atividade> carregar(Path origem) throws IOException {
        if (!Files.exists(origem)) {
            return new ArrayList<>();
        }
        return arquivoCsv.abrir(origem);
    }
}
