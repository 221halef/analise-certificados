package br.com.atividadescomplementares.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import br.com.atividadescomplementares.nucleo.ArmazenamentoAtividades;
import br.com.atividadescomplementares.nucleo.ArquivoConfig;
import br.com.atividadescomplementares.nucleo.ArquivoCsv;
import br.com.atividadescomplementares.nucleo.Atividade;
import br.com.atividadescomplementares.nucleo.Categoria;
import br.com.atividadescomplementares.nucleo.Classificador;
import br.com.atividadescomplementares.nucleo.EdicoesManuais;
import br.com.atividadescomplementares.nucleo.PastaDados;
import br.com.atividadescomplementares.nucleo.Processamento;
import br.com.atividadescomplementares.nucleo.ProcessadorPasta;
import br.com.atividadescomplementares.nucleo.ResultadoCalculo;
import br.com.atividadescomplementares.nucleo.ResumoCategoria;
import br.com.atividadescomplementares.nucleo.Status;
import br.com.atividadescomplementares.nucleo.Validacao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class JanelaPrincipal {

    private static final String NOME_ARQUIVO_CATEGORIAS = "categorias.properties";
    private static final String NOME_ARQUIVO_CONFIG = "config.properties";
    private static final String NOME_ARQUIVO_ATIVIDADES = "atividades.csv";

    private static final double LARGURA_COLUNA_DATA = 120;
    private static final double LARGURA_COLUNA_HORAS = 110;
    private static final double LARGURA_COLUNA_STATUS = 210;
    private static final double TAMANHO_SETA = 16;
    private static final double LARGURA_RECUO_ITEM = TAMANHO_SETA + 20 + 10;

    private final PastaDados pastaDados = new PastaDados();
    private final ProcessadorPasta processadorPasta = new ProcessadorPasta();
    private final ArquivoConfig arquivoConfig = new ArquivoConfig();
    private final ArquivoCsv arquivoCsv = new ArquivoCsv();
    private final ArmazenamentoAtividades armazenamento = new ArmazenamentoAtividades();

    private final Map<String, Image> cachePreVisualizacao = new HashMap<>();

    private Stage palco;
    private BorderPane raiz;
    private String temaAtual = "claro";
    private Path pastaSelecionada;
    private List<Atividade> atividades = new ArrayList<>();
    private Classificador classificador;
    private Atividade atividadeSelecionada;
    private Atividade atividadeArrastada;
    private Categoria categoriaParaExpandir;

    private Label rotuloTotalHoras;
    private FlowPane legendaCategorias;
    private StackPane iconeStatusRodape;
    private Label rotuloStatusRodape;
    private TreeTableView<LinhaTabela> tabela;
    private ImageView visualizadorPreview;
    private Label mensagemPreview;
    private Button botaoEditar;

    public void iniciar(Stage palcoPrincipal) {
        this.palco = palcoPrincipal;

        try {
            carregarConfiguracoes();
        } catch (IOException erro) {
            mostrarErro("Não foi possível carregar as configurações salvas: " + erro.getMessage());
            classificador = new Classificador(new EnumMap<>(Categoria.class));
        }

        raiz = new BorderPane();
        raiz.getStyleClass().add("janela-principal");
        raiz.setTop(construirBarraSuperior());
        raiz.setCenter(construirAreaCentral());
        raiz.setBottom(construirBarraInferior());

        Scene cena = new Scene(raiz, 1200, 780);
        cena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        aplicarTema();

        palco.getIcons().add(new Image(getClass().getResourceAsStream("/sogac.png")));
        palco.setTitle("SOGAC - Sistema de Organização e Gerenciamento de Atividades Complementares");
        palco.setMinWidth(1000);
        palco.setMinHeight(640);
        palco.setScene(cena);
        palco.show();

        atualizarTabela();
        Platform.runLater(() -> raiz.requestFocus());
    }

    private void carregarConfiguracoes() throws IOException {
        Path arquivoCategorias = pastaDados.garantirArquivoPadrao(NOME_ARQUIVO_CATEGORIAS, "/" + NOME_ARQUIVO_CATEGORIAS);
        classificador = Classificador.carregarDe(arquivoCategorias);

        Path caminhoConfig = pastaDados.obterPasta().resolve(NOME_ARQUIVO_CONFIG);
        temaAtual = arquivoConfig.lerTema(caminhoConfig);
        pastaSelecionada = arquivoConfig.lerPasta(caminhoConfig);

        try {
            atividades = armazenamento.carregar(pastaDados.obterPasta().resolve(NOME_ARQUIVO_ATIVIDADES));
            atividades.forEach(Validacao::atualizarStatus);
        } catch (IOException erro) {
            atividades = new ArrayList<>();
            mostrarErro("Não foi possível carregar os certificados salvos: " + erro.getMessage());
        }
    }

    private void salvarEstado() {
        try {
            Path pasta = pastaDados.obterPasta();
            armazenamento.salvar(atividades, pasta.resolve(NOME_ARQUIVO_ATIVIDADES));
            arquivoConfig.salvarPasta(pastaSelecionada, pasta.resolve(NOME_ARQUIVO_CONFIG));
        } catch (IOException erro) {
            mostrarErro("Não foi possível salvar os certificados: " + erro.getMessage());
        }
    }

    private Button criarBotao(String texto, String icone, String classeEstilo) {
        Button botao = new Button(texto, Icones.criar(icone, 18));
        botao.getStyleClass().add(classeEstilo);
        return botao;
    }

    private Region criarEspacador() {
        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);
        return espacador;
    }

    private HBox construirBarraSuperior() {
        StackPane iconeApp = Icones.relogio(40);

        Label sigla = new Label("SOGAC");
        sigla.getStyleClass().add("titulo-app");

        Label nomeCompleto = new Label(
                "Sistema de Organização e Gerenciamento de Atividades Complementares");
        nomeCompleto.getStyleClass().add("subtitulo-app");

        VBox textosTitulo = new VBox(0, sigla, nomeCompleto);
        textosTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox grupoTitulo = new HBox(14, iconeApp, textosTitulo);
        grupoTitulo.setAlignment(Pos.CENTER_LEFT);

        Button botaoPasta = criarBotao("Pasta", "pasta", "botao-secundario");
        botaoPasta.setOnAction(evento -> escolherPasta());

        InterruptorTema interruptor = new InterruptorTema(temaAtual.equals("escuro"), this::alternarTema);

        HBox barra = new HBox(12, grupoTitulo, criarEspacador(), botaoPasta, interruptor);
        barra.setPadding(new Insets(16, 24, 16, 24));
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getStyleClass().add("barra-superior");
        return barra;
    }

    private HBox construirAreaCentral() {
        VBox colunaEsquerda = new VBox(16);
        colunaEsquerda.getChildren().add(construirPainelTotais());

        tabela = construirTabela();
        StackPane molduraTabela = new StackPane(tabela);
        molduraTabela.getStyleClass().add("moldura-tabela");
        recortarCantos(molduraTabela, 12);
        VBox.setVgrow(molduraTabela, Priority.ALWAYS);
        colunaEsquerda.getChildren().add(molduraTabela);
        HBox.setHgrow(colunaEsquerda, Priority.ALWAYS);

        HBox area = new HBox(16, colunaEsquerda, construirPainelPreview());
        area.setPadding(new Insets(16, 24, 16, 24));
        return area;
    }

    private HBox construirPainelTotais() {
        rotuloTotalHoras = new Label("0 h");
        rotuloTotalHoras.getStyleClass().add("rotulo-total");

        Label legendaTotal = new Label("Total de horas");
        legendaTotal.getStyleClass().add("texto-secundario");

        VBox textos = new VBox(2, rotuloTotalHoras, legendaTotal);
        textos.setAlignment(Pos.CENTER_LEFT);

        HBox cartaoTotal = new HBox(16, Icones.documento(44), textos);
        cartaoTotal.setAlignment(Pos.CENTER_LEFT);
        cartaoTotal.getStyleClass().add("cartao");
        cartaoTotal.setPadding(new Insets(16, 28, 16, 20));

        legendaCategorias = new FlowPane(20, 12);
        legendaCategorias.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legendaCategorias, Priority.ALWAYS);

        HBox cartaoLegenda = new HBox(legendaCategorias);
        cartaoLegenda.setAlignment(Pos.CENTER_LEFT);
        cartaoLegenda.getStyleClass().add("cartao");
        cartaoLegenda.setPadding(new Insets(16, 24, 16, 24));
        cartaoLegenda.setMinHeight(92);
        HBox.setHgrow(cartaoLegenda, Priority.ALWAYS);

        return new HBox(16, cartaoTotal, cartaoLegenda);
    }

    private VBox construirChipCategoria(ResumoCategoria resumo, boolean primeiro) {
        Circle ponto = new Circle(5);
        ponto.getStyleClass().addAll("ponto-categoria", CoresCategoria.classeEstiloPonto(resumo.getCategoria()));

        Label rotuloNome = new Label(resumo.getCategoria().getNomePlural());
        rotuloNome.getStyleClass().add("texto-secundario");

        Label rotuloHoras = new Label(LinhaTabela.formatarHoras(resumo.getHoras()) + " h");
        rotuloHoras.getStyleClass().add("texto-chip-horas");

        VBox chip = new VBox(4, new HBox(8, ponto, rotuloNome), rotuloHoras);
        ((HBox) chip.getChildren().get(0)).setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("chip-categoria");
        if (primeiro) {
            chip.getStyleClass().add("chip-primeiro");
        }
        return chip;
    }

    private TreeTableView<LinhaTabela> construirTabela() {
        TreeTableColumn<LinhaTabela, String> colunaTitulo = new TreeTableColumn<>("Título");
        colunaTitulo.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getValue().getTitulo()));
        colunaTitulo.setCellFactory(coluna -> criarCelulaTitulo());
        colunaTitulo.setMinWidth(240);

        TreeTableColumn<LinhaTabela, String> colunaData = new TreeTableColumn<>("Data");
        colunaData.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getValue().getDataTexto()));
        colunaData.setPrefWidth(LARGURA_COLUNA_DATA);

        TreeTableColumn<LinhaTabela, String> colunaHoras = new TreeTableColumn<>("Horas");
        colunaHoras.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getValue().getHorasTexto()));
        colunaHoras.setPrefWidth(LARGURA_COLUNA_HORAS);

        TreeTableColumn<LinhaTabela, String> colunaStatus = new TreeTableColumn<>("Status");
        colunaStatus.setCellValueFactory(dados ->
                new SimpleStringProperty(dados.getValue().getValue().getStatusTexto()));
        colunaStatus.setCellFactory(coluna -> criarCelulaStatus());
        colunaStatus.setPrefWidth(LARGURA_COLUNA_STATUS);

        TreeTableView<LinhaTabela> arvore = new TreeTableView<>();
        arvore.getColumns().add(colunaTitulo);
        arvore.getColumns().add(colunaData);
        arvore.getColumns().add(colunaHoras);
        arvore.getColumns().add(colunaStatus);
        arvore.setShowRoot(false);
        arvore.getStyleClass().add("tabela-atividades");

        for (TreeTableColumn<LinhaTabela, ?> coluna : arvore.getColumns()) {
            coluna.setSortable(false);
            coluna.setReorderable(false);
            coluna.setResizable(false);
        }

        double larguraFixas = LARGURA_COLUNA_DATA + LARGURA_COLUNA_HORAS + LARGURA_COLUNA_STATUS;
        colunaTitulo.prefWidthProperty().bind(arvore.widthProperty().subtract(larguraFixas + 20));

        arvore.setRowFactory(tabelaParaFactory -> {
            TreeTableRow<LinhaTabela> linha = new TreeTableRow<>();
            linha.itemProperty().addListener((observavel, antigo, novo) -> {
                linha.getStyleClass().removeAll("linha-ignorada");
                if (novo != null && novo.isIgnorado()) {
                    linha.getStyleClass().add("linha-ignorada");
                }
            });
            linha.setOnMouseClicked(eventoClique -> {
                if (eventoClique.getButton() == MouseButton.PRIMARY
                        && eventoClique.getClickCount() == 2
                        && linha.getItem() != null
                        && !linha.getItem().isEhCategoria()) {
                    alternarIgnorado(linha.getItem().getAtividade());
                }
            });
            configurarArrastar(linha);

            MenuItem itemEditar = new MenuItem("Editar...");
            itemEditar.setOnAction(evento -> editarAtividade(linha.getItem().getAtividade()));
            MenuItem itemIgnorar = new MenuItem("Ignorar / considerar na soma");
            itemIgnorar.setOnAction(evento -> alternarIgnorado(linha.getItem().getAtividade()));
            ContextMenu menuContexto = new ContextMenu(itemEditar, itemIgnorar);
            linha.itemProperty().addListener((observavel, antigo, novo) ->
                    linha.setContextMenu(novo == null || novo.isEhCategoria() ? null : menuContexto));
            return linha;
        });

        arvore.getSelectionModel().selectedItemProperty().addListener((observavel, antigo, novoSelecionado) -> {
            if (novoSelecionado != null && !novoSelecionado.getValue().isEhCategoria()) {
                carregarPreVisualizacao(novoSelecionado.getValue().getAtividade());
            } else {
                limparPreVisualizacao();
            }
        });

        return arvore;
    }

    private static final String CLASSE_ALVO_DROP = "linha-alvo-drop";

    private void configurarArrastar(TreeTableRow<LinhaTabela> linha) {
        linha.setOnDragDetected(evento -> {
            LinhaTabela item = linha.getItem();
            if (item == null || item.isEhCategoria()) {
                return;
            }
            Dragboard quadro = linha.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent conteudo = new ClipboardContent();
            conteudo.putString(item.getAtividade().getArquivoNome());
            quadro.setContent(conteudo);
            atividadeArrastada = item.getAtividade();
            evento.consume();
        });

        linha.setOnDragOver(evento -> {
            if (podeSoltarEm(linha.getItem())) {
                evento.acceptTransferModes(TransferMode.MOVE);
            }
            evento.consume();
        });

        linha.setOnDragEntered(evento -> {
            if (podeSoltarEm(linha.getItem()) && !linha.getStyleClass().contains(CLASSE_ALVO_DROP)) {
                linha.getStyleClass().add(CLASSE_ALVO_DROP);
            }
        });

        linha.setOnDragExited(evento -> linha.getStyleClass().remove(CLASSE_ALVO_DROP));

        linha.setOnDragDropped(evento -> {
            linha.getStyleClass().remove(CLASSE_ALVO_DROP);
            boolean sucesso = false;
            LinhaTabela alvo = linha.getItem();
            if (podeSoltarEm(alvo)) {
                Atividade movida = atividadeArrastada;
                Categoria destino = alvo.getCategoria();
                sucesso = true;
                Platform.runLater(() -> moverParaCategoria(movida, destino));
            }
            evento.setDropCompleted(sucesso);
            evento.consume();
        });

        linha.setOnDragDone(evento -> atividadeArrastada = null);
    }

    private boolean podeSoltarEm(LinhaTabela alvo) {
        return atividadeArrastada != null
                && alvo != null
                && alvo.getCategoria() != atividadeArrastada.getCategoria();
    }

    private void moverParaCategoria(Atividade atividade, Categoria destino) {
        atividade.setCategoria(destino);
        atividade.setCategoriaManual(true);
        categoriaParaExpandir = destino;
        Validacao.atualizarStatus(atividade);
        atualizarTabela();
        salvarEstado();
        selecionarAtividade(atividade);
    }

    private void selecionarAtividade(Atividade atividade) {
        TreeItem<LinhaTabela> raizArvore = tabela.getRoot();
        if (raizArvore == null) {
            return;
        }
        for (TreeItem<LinhaTabela> itemCategoria : raizArvore.getChildren()) {
            for (TreeItem<LinhaTabela> itemAtividade : itemCategoria.getChildren()) {
                if (itemAtividade.getValue().getAtividade() == atividade) {
                    tabela.getSelectionModel().select(itemAtividade);
                    return;
                }
            }
        }
    }

    private TreeTableCell<LinhaTabela, String> criarCelulaTitulo() {
        return new TreeTableCell<>() {
            private final HBox caixa = new HBox(10);

            {
                caixa.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String valor, boolean vazio) {
                super.updateItem(valor, vazio);
                TreeTableRow<LinhaTabela> linhaVisual = getTreeTableRow();
                LinhaTabela linha = linhaVisual == null ? null : linhaVisual.getItem();
                if (vazio || valor == null || linha == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                caixa.getChildren().clear();

                if (linha.isEhCategoria()) {
                    String[] partes = valor.split(LinhaTabela.SEPARADOR_TITULO, 2);

                    Circle ponto = new Circle(5);
                    ponto.getStyleClass().addAll("ponto-categoria", CoresCategoria.classeEstiloPonto(linha.getCategoria()));

                    Label rotuloNome = new Label(partes[0]);
                    rotuloNome.getStyleClass().add("texto-categoria");

                    Label rotuloHoras = new Label(partes.length > 1 ? partes[1] : "");
                    rotuloHoras.getStyleClass().add("texto-secundario");

                    caixa.setPadding(Insets.EMPTY);
                    caixa.getChildren().addAll(criarSetaExpansao(linhaVisual.getTreeItem()), ponto, rotuloNome, rotuloHoras);
                } else {
                    Label rotuloTitulo = new Label(valor);
                    rotuloTitulo.getStyleClass().add("texto-item");

                    caixa.setPadding(new Insets(0, 0, 0, LARGURA_RECUO_ITEM));
                    caixa.getChildren().add(rotuloTitulo);
                }

                setGraphic(caixa);
                setText(null);
            }
        };
    }

    private Region criarSetaExpansao(TreeItem<LinhaTabela> itemArvore) {
        if (itemArvore.getChildren().isEmpty()) {
            Region vazio = new Region();
            vazio.setMinSize(TAMANHO_SETA, TAMANHO_SETA);
            vazio.setPrefSize(TAMANHO_SETA, TAMANHO_SETA);
            return vazio;
        }

        StackPane seta = Icones.criar(itemArvore.isExpanded() ? "seta-baixo" : "seta-direita", TAMANHO_SETA);
        seta.getStyleClass().add("seta-categoria");
        seta.setCursor(Cursor.HAND);
        seta.setOnMouseClicked(evento -> {
            itemArvore.setExpanded(!itemArvore.isExpanded());
            evento.consume();
        });
        return seta;
    }

    private TreeTableCell<LinhaTabela, String> criarCelulaStatus() {
        return new TreeTableCell<>() {
            private final HBox caixa = new HBox(8);

            {
                caixa.setAlignment(Pos.CENTER_LEFT);
                caixa.prefWidthProperty().bind(widthProperty().subtract(28));
            }

            @Override
            protected void updateItem(String valor, boolean vazio) {
                super.updateItem(valor, vazio);
                TreeTableRow<LinhaTabela> linhaVisual = getTreeTableRow();
                LinhaTabela linha = linhaVisual == null ? null : linhaVisual.getItem();
                if (vazio || valor == null || linha == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                caixa.getChildren().clear();

                if (linha.isEhCategoria()) {
                    TreeItem<LinhaTabela> itemArvore = linhaVisual.getTreeItem();
                    boolean temFilhos = !itemArvore.getChildren().isEmpty();
                    boolean expandido = itemArvore.isExpanded();

                    caixa.getChildren().add(criarEspacador());
                    if (!expandido && linha.isTemItensParaRevisar()) {
                        caixa.getChildren().add(criarSelo("revisar", "selo-neutro"));
                    }
                    if (temFilhos) {
                        StackPane seta = Icones.criar(expandido ? "seta-cima" : "seta-baixo", 20);
                        seta.getStyleClass().add("seta-categoria");
                        seta.setCursor(Cursor.HAND);
                        seta.setOnMouseClicked(evento -> {
                            itemArvore.setExpanded(!itemArvore.isExpanded());
                            evento.consume();
                        });
                        caixa.getChildren().add(seta);
                    }
                } else {
                    Label selo = criarSelo(valor, linha.getEstiloStatus());
                    String observacao = linha.getAtividade().getObservacao();
                    if (observacao != null && !observacao.isBlank()) {
                        selo.setTooltip(new Tooltip(observacao));
                    }
                    caixa.getChildren().add(selo);
                }

                setGraphic(caixa);
                setText(null);
            }
        };
    }

    private Label criarSelo(String texto, String classeEstilo) {
        boolean sucesso = classeEstilo.equals("selo-sucesso");
        Label selo = new Label(texto, Icones.circuloStatus(sucesso, classeEstilo, 16));
        selo.setGraphicTextGap(6);
        selo.getStyleClass().addAll("selo", classeEstilo);
        return selo;
    }

    private VBox construirPainelPreview() {
        Label tituloPainel = new Label("Pré-visualização");
        tituloPainel.getStyleClass().add("titulo-painel");

        Button botaoAmpliar = new Button();
        botaoAmpliar.setGraphic(Icones.criar("ampliar", 20));
        botaoAmpliar.getStyleClass().add("botao-icone");
        botaoAmpliar.setTooltip(new Tooltip("Ampliar pré-visualização"));
        botaoAmpliar.setOnAction(evento -> ampliarPreVisualizacao());

        botaoEditar = new Button();
        botaoEditar.setGraphic(Icones.criar("editar", 20));
        botaoEditar.getStyleClass().add("botao-icone");
        botaoEditar.setTooltip(new Tooltip("Editar certificado"));
        botaoEditar.setDisable(true);
        botaoEditar.setOnAction(evento -> {
            if (atividadeSelecionada != null) {
                editarAtividade(atividadeSelecionada);
            }
        });

        HBox cabecalho = new HBox(8, tituloPainel, criarEspacador(), botaoEditar, botaoAmpliar);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        visualizadorPreview = new ImageView();
        visualizadorPreview.setPreserveRatio(true);

        mensagemPreview = new Label();
        mensagemPreview.getStyleClass().add("texto-secundario");
        mensagemPreview.setWrapText(true);
        mensagemPreview.setMaxWidth(240);
        mensagemPreview.setAlignment(Pos.CENTER);
        mensagemPreview.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        StackPane moldura = new StackPane(visualizadorPreview, mensagemPreview);
        moldura.getStyleClass().add("moldura-preview");
        moldura.setMinHeight(240);
        moldura.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(mensagemPreview, Pos.CENTER);
        visualizadorPreview.fitWidthProperty().bind(moldura.widthProperty().subtract(2));

        mostrarMensagemPreview("Selecione um certificado para ver a pré-visualização");

        VBox painel = new VBox(16, cabecalho, moldura);
        painel.setPadding(new Insets(20));
        painel.setMinWidth(340);
        painel.setPrefWidth(340);
        painel.setMaxWidth(340);
        painel.getStyleClass().add("cartao");
        return painel;
    }

    private HBox construirBarraInferior() {
        iconeStatusRodape = Icones.circuloStatus(true, "selo-sucesso", 22);

        rotuloStatusRodape = new Label("Nenhuma pasta selecionada");
        rotuloStatusRodape.getStyleClass().add("texto-status");

        Button botaoExportarCsv = criarBotao("Exportar CSV", "baixar", "botao-secundario");
        botaoExportarCsv.setOnAction(evento -> exportarCsv());

        Button botaoAtualizar = criarBotao("Atualizar", "atualizar", "botao-principal");
        botaoAtualizar.setOnAction(evento -> reprocessar());

        HBox barra = new HBox(12, iconeStatusRodape, rotuloStatusRodape, criarEspacador(), botaoExportarCsv, botaoAtualizar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(12, 16, 12, 20));
        barra.getStyleClass().addAll("cartao", "barra-inferior");

        HBox moldura = new HBox(barra);
        HBox.setHgrow(barra, Priority.ALWAYS);
        moldura.setPadding(new Insets(0, 24, 16, 24));
        return moldura;
    }

    private void recortarCantos(Region regiao, double raio) {
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(raio * 2);
        recorte.setArcHeight(raio * 2);
        recorte.widthProperty().bind(regiao.widthProperty());
        recorte.heightProperty().bind(regiao.heightProperty());
        regiao.setClip(recorte);
    }

    private void escolherPasta() {
        DirectoryChooser seletor = new DirectoryChooser();
        seletor.setTitle("Escolha a pasta com os certificados em PDF");
        Path pasta = pedirPasta(seletor);
        if (pasta == null) {
            return;
        }

        pastaSelecionada = pasta;
        processarPastaSelecionada();
    }

    private void reprocessar() {
        if (pastaSelecionada == null) {
            atualizarTabela();
            return;
        }
        processarPastaSelecionada();
    }

    private void processarPastaSelecionada() {
        try {
            Processamento processamento = processadorPasta.processar(pastaSelecionada, classificador);
            EdicoesManuais.preservar(atividades, processamento.atividades());
            atividades = processamento.atividades();
            atualizarTabela();
            salvarEstado();
        } catch (IOException erro) {
            mostrarErro("Não foi possível ler a pasta selecionada: " + erro.getMessage());
        }
    }

    private Path pedirPasta(DirectoryChooser seletor) {
        java.io.File arquivo = seletor.showDialog(palco);
        return arquivo == null ? null : arquivo.toPath();
    }

    private void atualizarTabela() {
        ResultadoCalculo resultado = processadorPasta.recalcular(atividades);

        rotuloTotalHoras.setText(LinhaTabela.formatarHoras(resultado.getTotalHoras()) + " h");

        legendaCategorias.getChildren().clear();
        boolean primeiro = true;
        for (ResumoCategoria resumo : resultado.getResumos()) {
            if (resumo.getQuantidadeItens() > 0) {
                legendaCategorias.getChildren().add(construirChipCategoria(resumo, primeiro));
                primeiro = false;
            }
        }
        if (primeiro) {
            Label vazio = new Label("Nenhum certificado carregado");
            vazio.getStyleClass().add("texto-secundario");
            legendaCategorias.getChildren().add(vazio);
        }

        Map<Categoria, List<Atividade>> porCategoria = new EnumMap<>(Categoria.class);
        for (Atividade atividade : atividades) {
            porCategoria.computeIfAbsent(atividade.getCategoria(), chave -> new ArrayList<>()).add(atividade);
        }

        Map<Categoria, Boolean> expandidoAntes = new EnumMap<>(Categoria.class);
        if (tabela.getRoot() != null) {
            for (TreeItem<LinhaTabela> anterior : tabela.getRoot().getChildren()) {
                if (!anterior.getChildren().isEmpty()) {
                    expandidoAntes.put(anterior.getValue().getCategoria(), anterior.isExpanded());
                }
            }
        }

        TreeItem<LinhaTabela> raizArvore = new TreeItem<>();
        for (ResumoCategoria resumo : resultado.getResumos()) {
            List<Atividade> itens = porCategoria.getOrDefault(resumo.getCategoria(), List.of());

            boolean temItensParaRevisar = false;
            for (Atividade atividade : itens) {
                if (atividade.getStatus() == Status.REVISAR && !atividade.isIgnorado()) {
                    temItensParaRevisar = true;
                }
            }

            TreeItem<LinhaTabela> itemCategoria = new TreeItem<>(LinhaTabela.deCategoria(resumo, temItensParaRevisar));
            for (Atividade atividade : itens) {
                itemCategoria.getChildren().add(new TreeItem<>(LinhaTabela.deAtividade(atividade)));
            }
            boolean expandir = !itens.isEmpty()
                    && (resumo.getCategoria() == categoriaParaExpandir
                    || expandidoAntes.getOrDefault(resumo.getCategoria(), true));
            itemCategoria.setExpanded(expandir);
            itemCategoria.expandedProperty().addListener((observavel, antes, depois) -> tabela.refresh());

            raizArvore.getChildren().add(itemCategoria);
        }

        categoriaParaExpandir = null;
        tabela.setRoot(raizArvore);
        tabela.refresh();

        atualizarStatusRodape();
    }

    private void atualizarStatusRodape() {
        if (pastaSelecionada == null && atividades.isEmpty()) {
            rotuloStatusRodape.setText("Nenhuma pasta selecionada");
            iconeStatusRodape.setVisible(false);
            iconeStatusRodape.setManaged(false);
            return;
        }
        int quantidade = atividades.size();
        String plural = quantidade == 1 ? "certificado encontrado" : "certificados encontrados";
        rotuloStatusRodape.setText("Processamento concluído • " + quantidade + " " + plural);
        iconeStatusRodape.setVisible(true);
        iconeStatusRodape.setManaged(true);
    }

    private static final DateTimeFormatter FORMATO_DATA_EDICAO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private void editarAtividade(Atividade atividade) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(palco);
        dialogo.setTitle("Editar certificado");
        dialogo.setHeaderText(atividade.getArquivoNome());

        TextField campoTitulo = new TextField(atividade.getTitulo() == null ? "" : atividade.getTitulo());
        TextField campoEmissor = new TextField(atividade.getEmissor() == null ? "" : atividade.getEmissor());
        TextField campoParticipante = new TextField(atividade.getParticipante() == null ? "" : atividade.getParticipante());
        TextField campoCodigo = new TextField(
                atividade.getCodigoVerificacao() == null ? "" : atividade.getCodigoVerificacao());
        TextField campoHoras = new TextField(LinhaTabela.formatarHoras(atividade.getHorasDeclaradas()));

        DatePicker campoData = new DatePicker(atividade.getData());
        campoData.setEditable(true);
        campoData.setPromptText("dd/MM/aaaa");
        campoData.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : data.format(FORMATO_DATA_EDICAO);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(texto.trim(), FORMATO_DATA_EDICAO);
                } catch (DateTimeParseException erro) {
                    return null;
                }
            }
        });

        ComboBox<Categoria> campoCategoria = new ComboBox<>();
        campoCategoria.getItems().addAll(Categoria.values());
        campoCategoria.setValue(atividade.getCategoria());

        for (TextField campo : List.of(campoTitulo, campoEmissor, campoParticipante, campoCodigo)) {
            campo.setPrefColumnCount(28);
        }

        GridPane grade = new GridPane();
        grade.setHgap(12);
        grade.setVgap(10);
        grade.setPadding(new Insets(16, 8, 8, 8));
        grade.addRow(0, new Label("Título"), campoTitulo);
        grade.addRow(1, new Label("Categoria"), campoCategoria);
        grade.addRow(2, new Label("Data"), campoData);
        grade.addRow(3, new Label("Horas"), campoHoras);
        grade.addRow(4, new Label("Emissor"), campoEmissor);
        grade.addRow(5, new Label("Participante"), campoParticipante);
        grade.addRow(6, new Label("Código de verificação"), campoCodigo);

        Label rotuloErro = new Label();
        rotuloErro.setStyle("-fx-text-fill: #DC2626;");
        rotuloErro.setManaged(false);
        rotuloErro.setVisible(false);
        grade.add(rotuloErro, 0, 7, 2, 1);

        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) dialogo.getDialogPane().lookupButton(ButtonType.OK)).setText("Salvar");
        ((Button) dialogo.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Cancelar");

        // Valida antes de fechar; se algo estiver errado, o diálogo continua aberto.
        dialogo.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            String erro = null;
            double horas = 0;
            try {
                String textoHoras = campoHoras.getText().trim().replace(',', '.');
                horas = textoHoras.isEmpty() ? 0 : Double.parseDouble(textoHoras);
                if (horas < 0) {
                    erro = "As horas não podem ser negativas.";
                }
            } catch (NumberFormatException excecao) {
                erro = "Horas inválidas. Use um número, como 20 ou 7,5.";
            }

            String textoData = campoData.getEditor().getText();
            LocalDate data = campoData.getConverter().fromString(textoData);
            if (erro == null && textoData != null && !textoData.isBlank() && data == null) {
                erro = "Data inválida. Use o formato dd/mm/aaaa.";
            }

            if (erro != null) {
                rotuloErro.setText(erro);
                rotuloErro.setManaged(true);
                rotuloErro.setVisible(true);
                evento.consume();
            }
        });

        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String textoHoras = campoHoras.getText().trim().replace(',', '.');
        atividade.setHorasDeclaradas(textoHoras.isEmpty() ? 0 : Double.parseDouble(textoHoras));
        atividade.setData(campoData.getConverter().fromString(campoData.getEditor().getText()));
        atividade.setTitulo(textoOuNulo(campoTitulo.getText()));
        atividade.setEmissor(textoOuNulo(campoEmissor.getText()));
        atividade.setParticipante(textoOuNulo(campoParticipante.getText()));
        atividade.setCodigoVerificacao(textoOuNulo(campoCodigo.getText()));

        Categoria novaCategoria = campoCategoria.getValue();
        if (novaCategoria != null && novaCategoria != atividade.getCategoria()) {
            atividade.setCategoria(novaCategoria);
            atividade.setCategoriaManual(true);
            categoriaParaExpandir = novaCategoria;
        }

        atividade.setEditado(true);
        Validacao.atualizarStatus(atividade);

        atualizarTabela();
        salvarEstado();
        selecionarAtividade(atividade);
    }

    private String textoOuNulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private void alternarIgnorado(Atividade atividade) {
        atividade.setIgnorado(!atividade.isIgnorado());
        atualizarTabela();
        salvarEstado();
    }

    private void exportarCsv() {
        if (atividades.isEmpty()) {
            mostrarErro("Não há atividades para exportar. Escolha uma pasta primeiro.");
            return;
        }

        FileChooser seletor = new FileChooser();
        seletor.setTitle("Exportar CSV");
        seletor.setInitialFileName("sogac.csv");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));

        java.io.File arquivo = seletor.showSaveDialog(palco);
        if (arquivo == null) {
            return;
        }

        try {
            arquivoCsv.exportar(atividades, arquivo.toPath());
        } catch (IOException erro) {
            mostrarErro("Não foi possível exportar o CSV: " + erro.getMessage());
        }
    }

    private void mostrarMensagemPreview(String texto) {
        mensagemPreview.setText(texto);
        boolean visivel = texto != null && !texto.isBlank();
        mensagemPreview.setVisible(visivel);
        mensagemPreview.setManaged(visivel);
    }

    private void carregarPreVisualizacao(Atividade atividade) {
        atividadeSelecionada = atividade;
        botaoEditar.setDisable(false);
        String chave = atividade.getHashSha256();
        Image imagemEmCache = cachePreVisualizacao.get(chave);
        if (imagemEmCache != null) {
            visualizadorPreview.setImage(imagemEmCache);
            mostrarMensagemPreview("");
            return;
        }

        mostrarMensagemPreview("Carregando pré-visualização...");
        visualizadorPreview.setImage(null);

        Task<Image> tarefa = new Task<>() {
            @Override
            protected Image call() {
                return renderizarPrimeiraPagina(atividade.getCaminhoArquivo());
            }
        };

        tarefa.setOnSucceeded(evento -> {
            Image imagem = tarefa.getValue();
            if (imagem != null) {
                cachePreVisualizacao.put(chave, imagem);
            }
            if (atividadeSelecionada != atividade) {
                return;
            }
            visualizadorPreview.setImage(imagem);
            mostrarMensagemPreview(imagem == null ? "Não foi possível gerar a pré-visualização." : "");
        });

        tarefa.setOnFailed(evento -> {
            if (atividadeSelecionada == atividade) {
                mostrarMensagemPreview("Não foi possível gerar a pré-visualização.");
            }
        });

        Thread execucao = new Thread(tarefa, "pre-visualizacao-pdf");
        execucao.setDaemon(true);
        execucao.start();
    }

    private Image renderizarPrimeiraPagina(Path arquivo) {
        if (arquivo == null || !Files.exists(arquivo)) {
            return null;
        }
        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {
            if (documento.getNumberOfPages() == 0) {
                return null;
            }
            PDFRenderer renderizador = new PDFRenderer(documento);
            java.awt.image.BufferedImage imagemAwt = renderizador.renderImageWithDPI(0, 100);

            ByteArrayOutputStream bufferSaida = new ByteArrayOutputStream();
            ImageIO.write(imagemAwt, "png", bufferSaida);
            return new Image(new ByteArrayInputStream(bufferSaida.toByteArray()));
        } catch (IOException erro) {
            return null;
        }
    }

    private void limparPreVisualizacao() {
        atividadeSelecionada = null;
        botaoEditar.setDisable(true);
        visualizadorPreview.setImage(null);
        mostrarMensagemPreview("Selecione um certificado para ver a pré-visualização");
    }

    private void ampliarPreVisualizacao() {
        Image imagem = visualizadorPreview.getImage();
        if (imagem == null) {
            return;
        }

        Stage janela = new Stage();
        janela.initOwner(palco);
        janela.initModality(Modality.WINDOW_MODAL);
        janela.setTitle(atividadeSelecionada == null ? "Pré-visualização" : atividadeSelecionada.getArquivoNome());

        ImageView imagemAmpliada = new ImageView(imagem);
        imagemAmpliada.setPreserveRatio(true);
        imagemAmpliada.setFitHeight(800);

        StackPane raizAmpliada = new StackPane(imagemAmpliada);
        raizAmpliada.setPadding(new Insets(16));
        raizAmpliada.getStyleClass().add("janela-principal");
        if (temaAtual.equals("escuro")) {
            raizAmpliada.getStyleClass().add("tema-escuro");
        }

        Scene cena = new Scene(raizAmpliada);
        cena.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());

        janela.setScene(cena);
        janela.show();
    }

    private void alternarTema(boolean escuro) {
        temaAtual = escuro ? "escuro" : "claro";
        aplicarTema();

        try {
            Path caminhoConfig = pastaDados.obterPasta().resolve(NOME_ARQUIVO_CONFIG);
            arquivoConfig.salvarTema(temaAtual, caminhoConfig);
        } catch (IOException erro) {
            mostrarErro("Não foi possível salvar a preferência de tema: " + erro.getMessage());
        }
    }

    private void aplicarTema() {
        boolean escuro = temaAtual.equals("escuro");

        String folhaEstiloBase = escuro
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet();
        Application.setUserAgentStylesheet(folhaEstiloBase);

        raiz.getStyleClass().remove("tema-escuro");
        if (escuro) {
            raiz.getStyleClass().add("tema-escuro");
        }
    }

    private void mostrarErro(String mensagem) {
        Platform.runLater(() -> {
            Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem);
            alerta.setHeaderText(null);
            alerta.showAndWait();
        });
    }
}
