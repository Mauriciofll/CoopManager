package view;

import controller.PedidoController;
import controller.ProdutoController;
import model.Cliente;
import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import repository.ClienteRepository;
import repository.PedidoRepository;
import repository.ProdutoRepository;
import repository.UsuarioRepository;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;

@SuppressWarnings({"serial", "this-escape"})
public class MainSwingView extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final String[] CATEGORIAS = {"AGRICULTURA", "ARTESANATO"};
    private static final String[] TIPOS_ENTREGA = {"RETIRADA", "ENTREGA"};
    private static final String[] STATUS_PEDIDO = {
            "EM_SEPARACAO", "PRONTO", "ENTREGUE"
    };
    private static final String ACAO_ENVIAR_TUDO = "ENVIAR_TUDO";
    private static final String ACAO_ENVIAR_PARCIAL = "ENVIAR_PARCIAL";
    private static final String ACAO_RECEBER_TUDO = "RECEBER_TUDO";
    private static final String ACAO_RECEBER_PARCIAL = "RECEBER_PARCIAL";
    private static final String FILTRO_COM_DIVERGENCIA = "COM_DIVERGENCIA";
    private static final String FILTRO_AGUARDANDO_PRODUTOR = "AGUARDANDO_PRODUTOR";
    private static final String FILTRO_EM_TRANSITO = "EM_TRANSITO";
    private static final String FILTRO_PREVISTO_HOJE = "PREVISTO_HOJE";
    private static final String FILTRO_ATRASADO = "ATRASADO";
    private static final String FILTRO_PRONTO_SEPARACAO = "PRONTO_SEPARACAO";
    private static final String TITULO_REGISTRAR_PEDIDO = "Registrar pedido";
    private static final String[] TIPOS_USUARIO = {"PRODUTOR", "ADMIN"};
    private static final String[] TIPOS_RELATORIO_OPERACIONAL = {
            "Lista do produtor", "Conferência na cooperativa", "Separação por cliente"
    };
    private static final String TODOS_OS_PRODUTORES = "Todos os produtores";

    private boolean darkMode = false;

    private Color backgroundColor = new Color(242, 248, 244);
    private Color panelColor = Color.WHITE;
    private Color accentColor = new Color(0, 115, 58);
    private Color textColor = new Color(28, 43, 35);
    private Color mutedTextColor = new Color(82, 101, 90);
    private Color borderColor = new Color(212, 226, 218);
    private Color fieldColor = Color.WHITE;
    private Color secondaryButtonColor = new Color(229, 240, 233);
    private Color tableHeaderColor = new Color(233, 242, 236);

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final ClienteRepository clienteRepository = new ClienteRepository();
    private final ProdutoRepository produtoRepository = new ProdutoRepository();
    private final PedidoRepository pedidoRepository = new PedidoRepository();
    private final ProdutoController produtoController = new ProdutoController(produtoRepository);
    private final PedidoController pedidoController = new PedidoController(pedidoRepository);

    private Usuario produtorLogado;
    private int produtoSelecionadoId = -1;
    private int usuarioSelecionadoId = -1;
    private int clienteSelecionadoId = -1;

    private final JLabel produtorLogadoLabel = new JLabel("Usuário: -");
    private final JButton temaButton = secondaryButton("");
    private final JLabel totalProdutosValue = metricValue();
    private final JLabel estoqueTotalValue = metricValue();
    private final JLabel totalPedidosValue = metricValue();
    private final JLabel valorPedidosValue = metricValue();
    private final JLabel estoqueBaixoValue = metricValue();
    private final JLabel pedidosAbertosValue = metricValue();
    private final JLabel pedidosHojeValue = metricValue();
    private final JLabel pedidosAtrasadosValue = metricValue();
    private final JLabel divergenciasValue = metricValue();
    private javax.swing.JTabbedPane navigationTabs;
    private final JTextArea alertasOperacionaisArea = new JTextArea();
    private final JTextArea produtosAtencaoArea = new JTextArea();
    private final JTextArea pedidosRecentesArea = new JTextArea();
    private JPanel acoesRapidasPanel;
    private JButton novoProdutoQuickButton;
    private JButton novoPedidoQuickButton;
    private JButton usuariosQuickButton;
    private JButton clientesQuickButton;
    private JPanel produtosTabPanel;
    private JPanel usuariosTabPanel;
    private JPanel clientesTabPanel;
    private JPanel novoPedidoPanel;
    private JPanel pedidoStatusPanel;
    private JPanel relatorioOperacionalPanel;
    private javax.swing.JTabbedPane pedidosToolsTabs;
    private javax.swing.JTabbedPane pedidosSituacaoTabs;
    private JPanel produtoFormPanel;
    private JPanel produtosFiltroPanel;
    private JPanel pedidosFiltroPanel;
    private JLabel produtorProdutosFiltroLabel;
    private JLabel produtorPedidosFiltroLabel;
    private final DefaultComboBoxModel<Usuario> produtorProdutosModel = new DefaultComboBoxModel<>();
    private final JComboBox<Usuario> produtorProdutosCombo = new JComboBox<>(produtorProdutosModel);
    private final DefaultComboBoxModel<Object> produtorPedidosModel = new DefaultComboBoxModel<>();
    private final JComboBox<Object> produtorPedidosCombo = new JComboBox<>(produtorPedidosModel);
    private final JButton meuCadastroButton = secondaryButton("Meu cadastro");
    private boolean atualizandoFiltrosProdutor;
    private boolean filtrosConfigurados;

    private final JTextField produtoBuscaField = new JTextField();
    private final JCheckBox estoqueBaixoCheckBox = new JCheckBox("Baixo estoque");
    private final JTextField usuarioBuscaField = new JTextField();
    private final JTextField clienteBuscaField = new JTextField();
    private final JTextField pedidoBuscaField = new JTextField();
    private final JComboBox<String> statusPedidoFiltroCombo = new JComboBox<>(new String[] {
            "Todos os status", FILTRO_COM_DIVERGENCIA, FILTRO_AGUARDANDO_PRODUTOR, FILTRO_EM_TRANSITO,
            FILTRO_PREVISTO_HOJE, FILTRO_ATRASADO, FILTRO_PRONTO_SEPARACAO,
            "AGUARDANDO_PRODUTORES", "AGUARDANDO_RECEBIMENTO", "PARCIALMENTE_RECEBIDO", "PENDENCIA",
            "EM_SEPARACAO", "PRONTO", "ENTREGUE", "CANCELADO"
    });

    private final JTable produtosTable = createTable(new String[] {
            "ID", "Produto", "Categoria", "Preço", "Estoque", "Mínimo"
    });
    private final JTable pedidosTable = createTable(new String[] {
            "ID", "Data", "Prevista", "Produtores", "Cliente", "Status", "Entrega", "Itens", "Envio", "Recebimento", "Total"
    });
    private final JTable usuariosTable = createTable(new String[] {
            "ID", "Nome", "Tipo", "Login"
    });
    private final JTable clientesTable = createTable(new String[] {
            "ID", "Nome", "E-mail", "Telefone"
    });

    private final JTextField nomeProdutoField = new JTextField();
    private final JComboBox<String> categoriaProdutoCombo = new JComboBox<>(CATEGORIAS);
    private final JTextField precoProdutoField = new JTextField();
    private final JSpinner estoqueProdutoSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    private final JSpinner estoqueMinimoProdutoSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 9999, 1));
    private final JLabel produtoFormStateLabel = new JLabel("Novo produto");
    private final JButton salvarProdutoButton = primaryButton("Cadastrar");
    private final JButton limparProdutoButton = secondaryButton("Novo");
    private final JButton excluirProdutoButton = secondaryButton("Excluir");

    private final JTextField nomeUsuarioField = new JTextField();
    private final JComboBox<String> tipoUsuarioCombo = new JComboBox<>(TIPOS_USUARIO);
    private final JTextField loginUsuarioField = new JTextField();
    private final JPasswordField senhaUsuarioField = new JPasswordField();
    private final JPasswordField confirmarSenhaUsuarioField = new JPasswordField();
    private final JLabel usuarioFormStateLabel = new JLabel("Novo usuário");
    private final JButton salvarUsuarioButton = primaryButton("Cadastrar");
    private final JButton limparUsuarioButton = secondaryButton("Novo");
    private final JButton excluirUsuarioButton = secondaryButton("Excluir");

    private final JTextField nomeClienteField = new JTextField();
    private final JTextField emailClienteField = new JTextField();
    private final JTextField telefoneClienteField = new JTextField();
    private final JLabel clienteFormStateLabel = new JLabel("Novo cliente");
    private final JButton salvarClienteButton = primaryButton("Cadastrar");
    private final JButton limparClienteButton = secondaryButton("Novo");
    private final JButton excluirClienteButton = secondaryButton("Excluir");

    private final DefaultComboBoxModel<Cliente> clienteComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<Cliente> clientePedidoCombo = new JComboBox<>(clienteComboModel);
    private final JComboBox<String> tipoEntregaCombo = new JComboBox<>(TIPOS_ENTREGA);
    private final JTextField dataPrevistaPedidoField = new JTextField();
    private final DefaultComboBoxModel<Produto> produtoPedidoComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<Produto> produtoPedidoCombo = new JComboBox<>(produtoPedidoComboModel);
    private final JSpinner quantidadePedidoSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JTable itensNovoPedidoTable = createTable(new String[] {
            "Produtor", "Produto", "Quantidade", "Subtotal"
    });
    private final List<ItemPedido> itensNovoPedido = new ArrayList<>();
    private final JButton adicionarItemPedidoButton = secondaryButton("Adicionar item");
    private final JButton removerItemPedidoButton = secondaryButton("Remover item");

    private final DefaultComboBoxModel<Pedido> pedidoStatusComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<Pedido> pedidoStatusCombo = new JComboBox<>(pedidoStatusComboModel);
    private final DefaultComboBoxModel<ItemPedido> itemStatusComboModel = new DefaultComboBoxModel<>();
    private final JComboBox<ItemPedido> itemStatusCombo = new JComboBox<>(itemStatusComboModel);
    private final JCheckBox operacaoCompletaCheckBox = new JCheckBox("Todos enviados");
    private final JSpinner quantidadeEnviadaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    private final JSpinner quantidadeRecebidaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
    private final JComboBox<String> statusPedidoCombo = new JComboBox<>(STATUS_PEDIDO);
    private final JLabel fluxoItemPedidoLabel = new JLabel("Selecione um item.");
    private final JTextField observacaoPedidoField = new JTextField();
    private JLabel operacaoCompletaLabel;
    private final JLabel acaoItemMensagemLabel = new JLabel("Selecione um item.");
    private JLabel quantidadeEnviadaLabel;
    private JLabel quantidadeRecebidaLabel;
    private JLabel statusPedidoLabel;
    private JButton atualizarItemPedidoButton;
    private final JButton itemIndisponivelButton = secondaryButton("Informar indisponível");
    private JButton atualizarPedidoStatusButton;
    private final JButton cancelarPedidoButton = secondaryButton("Cancelar pedido");
    private final JButton excluirPedidoButton = secondaryButton("Excluir pedido");
    private final JTextArea detalhesPedidoArea = new JTextArea();
    private final JComboBox<String> relatorioOperacionalCombo = new JComboBox<>(TIPOS_RELATORIO_OPERACIONAL);
    private final JTextArea relatorioOperacionalArea = new JTextArea();
    private final JButton atualizarRelatorioButton = secondaryButton("Atualizar lista");
    private final JButton copiarRelatorioButton = secondaryButton("Copiar lista");

    public MainSwingView() {
        super("CoopManager");
        carregarDadosExemploSeSolicitado();
        configurarJanela();
        montarLayout();
        configurarEntradaDePreco();
        configurarFiltros();
        aplicarTema();
    }

    public void iniciar() {
        if (!garantirUsuarioInicial()) {
            dispose();
            System.exit(0);
            return;
        }

        atualizarTela();
        setVisible(true);
    }

    private void configurarJanela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        aplicarIconeAplicacao();
        setSize(1100, 720);
        setMinimumSize(new Dimension(760, 520));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(backgroundColor);
    }

    private void aplicarIconeAplicacao() {
        URL iconUrl = MainSwingView.class.getResource("/assets/CoopManager.png");
        if (iconUrl == null) {
            return;
        }

        try {
            BufferedImage source = ImageIO.read(iconUrl);
            List<Image> icons = new ArrayList<>();
            int[] sizes = {256, 128, 64, 48, 32, 16};

            for (int size : sizes) {
                icons.add(criarIconeQuadrado(source, size));
            }

            setIconImages(icons);
        } catch (Exception exception) {
            System.err.println("Não foi possível carregar o ícone da aplicação.");
        }
    }

    private Image criarIconeQuadrado(BufferedImage source, int size) {
        double cropFactor = switch (size) {
            case 256 -> 1.00;
            case 128 -> 0.96;
            case 64 -> 0.88;
            case 48 -> 0.82;
            case 32 -> 0.74;
            case 16 -> 0.68;
            default -> 1.00;
        };

        int sourceSquare = (int) Math.round(Math.min(source.getWidth(), source.getHeight()) * cropFactor);
        int sourceX = (source.getWidth() - sourceSquare) / 2;
        int sourceY = (source.getHeight() - sourceSquare) / 2;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = icon.createGraphics();

        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(
                    source,
                    0,
                    0,
                    size,
                    size,
                    sourceX,
                    sourceY,
                    sourceX + sourceSquare,
                    sourceY + sourceSquare,
                    null
            );
        } finally {
            graphics.dispose();
        }

        return icon;
    }

    private void montarLayout() {
        setLayout(new BorderLayout(16, 16));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    private void configurarEntradaDePreco() {
        precoProdutoField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                precoProdutoField.selectAll();
            }

            @Override
            public void focusLost(FocusEvent event) {
                formatarPrecoProduto();
            }
        });
    }

    private void configurarFiltros() {
        if (filtrosConfigurados) {
            return;
        }

        filtrosConfigurados = true;
        aoAlterarTexto(produtoBuscaField, () -> {
            produtosTable.clearSelection();
            atualizarProdutos();
        });
        estoqueBaixoCheckBox.addActionListener(event -> {
            produtosTable.clearSelection();
            atualizarProdutos();
        });
        aoAlterarTexto(usuarioBuscaField, () -> {
            usuariosTable.clearSelection();
            atualizarUsuarios();
        });
        aoAlterarTexto(clienteBuscaField, () -> {
            clientesTable.clearSelection();
            atualizarClientes();
        });
        aoAlterarTexto(pedidoBuscaField, () -> {
            pedidosTable.clearSelection();
            atualizarPedidos();
            atualizarDetalhesPedido();
            atualizarRelatorioOperacional();
        });
        statusPedidoFiltroCombo.addActionListener(event -> {
            pedidosTable.clearSelection();
            atualizarPedidos();
            atualizarDetalhesPedido();
            atualizarRelatorioOperacional();
        });
    }

    private void aoAlterarTexto(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                action.run();
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new HeaderPanel(new BorderLayout(18, 8), 28);
        header.putClientProperty("surface", "header");
        header.setBackground(accentColor);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JLabel title = new JLabel("CoopManager");
        title.putClientProperty("role", "headerText");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));

        JLabel subtitle = new JLabel("Gestão de produtos, pedidos e estoque da cooperativa");
        subtitle.putClientProperty("role", "headerSubtle");
        subtitle.setForeground(new Color(226, 238, 232));
        subtitle.setFont(subtitle.getFont().deriveFont(14f));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 5));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);

        JPanel brand = new JPanel(new BorderLayout(14, 0));
        brand.setOpaque(false);
        brand.add(createLogoLabel(78), BorderLayout.WEST);
        brand.add(text, BorderLayout.CENTER);
        header.add(brand, BorderLayout.WEST);

        produtorLogadoLabel.putClientProperty("role", "headerText");
        produtorLogadoLabel.setForeground(Color.WHITE);
        produtorLogadoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        produtorLogadoLabel.setFont(produtorLogadoLabel.getFont().deriveFont(Font.BOLD, 13f));

        meuCadastroButton.addActionListener(event -> editarMeuCadastro());
        JButton trocarProdutorButton = secondaryButton("Trocar usuário");
        trocarProdutorButton.addActionListener(event -> trocarProdutor());
        temaButton.addActionListener(event -> alternarTema());

        JPanel actions = new JPanel(new BorderLayout(0, 10));
        actions.setOpaque(false);
        actions.add(produtorLogadoLabel, BorderLayout.NORTH);

        JPanel actionButtons = new JPanel(new GridLayout(1, 3, 8, 0));
        actionButtons.setOpaque(false);
        actionButtons.add(meuCadastroButton);
        actionButtons.add(trocarProdutorButton);
        actionButtons.add(temaButton);
        actions.add(actionButtons, BorderLayout.SOUTH);

        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JLabel createLogoLabel(int size) {
        URL iconUrl = MainSwingView.class.getResource("/assets/CoopManager.png");
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(size, size));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        if (iconUrl == null) {
            return label;
        }

        try {
            BufferedImage source = ImageIO.read(iconUrl);
            label.setIcon(new javax.swing.ImageIcon(criarIconeQuadrado(source, size)));
        } catch (Exception exception) {
            label.setText("CM");
            label.setForeground(Color.WHITE);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        }
        return label;
    }

    private javax.swing.JTabbedPane buildTabs() {
        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
        navigationTabs = tabs;
        tabs.setUI(new ThemedTabbedPaneUI());
        tabs.setOpaque(false);
        tabs.setBackground(backgroundColor);
        tabs.setForeground(textColor);
        tabs.setBorder(BorderFactory.createEmptyBorder());
        tabs.addTab("Resumo", buildResumoTab());
        produtosTabPanel = buildProdutosTab();
        tabs.addTab("Produtos", produtosTabPanel);
        usuariosTabPanel = buildUsuariosTab();
        tabs.addTab("Usuários", usuariosTabPanel);
        clientesTabPanel = buildClientesTab();
        tabs.addTab("Clientes", clientesTabPanel);
        tabs.addTab("Pedidos", buildPedidosTab());
        return tabs;
    }

    private JPanel buildResumoTab() {
        JPanel panel = basePanel(new BorderLayout(16, 16));
        JPanel metrics = new JPanel(new GridLayout(3, 3, 12, 12));
        metrics.setOpaque(false);
        metrics.add(metricPanel("Produtos", totalProdutosValue));
        metrics.add(metricPanel("Itens em estoque", estoqueTotalValue));
        metrics.add(metricPanel("Baixo estoque", estoqueBaixoValue));
        metrics.add(metricPanel("Pedidos", totalPedidosValue));
        metrics.add(metricPanel("Em operação", pedidosAbertosValue));
        metrics.add(metricPanel("Para hoje", pedidosHojeValue));
        metrics.add(metricPanel("Atrasados", pedidosAtrasadosValue));
        metrics.add(metricPanel("Divergências", divergenciasValue));
        metrics.add(metricPanel("Valor acompanhado", valorPedidosValue));

        estilizarAreaResumo(alertasOperacionaisArea);
        estilizarAreaResumo(produtosAtencaoArea);
        estilizarAreaResumo(pedidosRecentesArea);

        panel.add(metrics, BorderLayout.NORTH);

        JPanel movimento = new JPanel(new GridLayout(1, 3, 16, 16));
        movimento.setOpaque(false);
        movimento.add(wrapPanel("Alertas do intermediador", new JScrollPane(alertasOperacionaisArea)));
        movimento.add(wrapPanel("Itens pendentes", new JScrollPane(produtosAtencaoArea)));
        movimento.add(wrapPanel("Pedidos recentes", new JScrollPane(pedidosRecentesArea)));

        panel.add(movimento, BorderLayout.CENTER);
        panel.add(buildAcoesRapidas(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAcoesRapidas() {
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));
        acoesRapidasPanel = actions;
        actions.setOpaque(false);

        novoProdutoQuickButton = primaryButton("Novo produto");
        novoProdutoQuickButton.addActionListener(event -> selecionarAba("Produtos"));

        novoPedidoQuickButton = secondaryButton(TITULO_REGISTRAR_PEDIDO);
        novoPedidoQuickButton.addActionListener(event -> selecionarAba("Pedidos"));

        usuariosQuickButton = secondaryButton("Usuários");
        usuariosQuickButton.addActionListener(event -> selecionarAba("Usuários"));

        clientesQuickButton = secondaryButton("Clientes");
        clientesQuickButton.addActionListener(event -> selecionarAba("Clientes"));

        actions.add(novoProdutoQuickButton);
        actions.add(novoPedidoQuickButton);
        actions.add(usuariosQuickButton);
        actions.add(clientesQuickButton);
        return actions;
    }

    private JPanel buildBuscaPanel(String labelText, JTextField field) {
        JPanel panel = new RoundedPanel(new GridBagLayout(), 20);
        panel.putClientProperty("surface", "card");
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        field.putClientProperty("placeholder", labelText);
        field.setToolTipText(labelText);

        panel.add(filterLabel(labelText), filterConstraints(0, 0, 0));
        GridBagConstraints fieldConstraints = filterConstraints(1, 0, 1);
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, fieldConstraints);
        return panel;
    }

    private JLabel filterLabel(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty("role", "muted");
        label.setForeground(mutedTextColor);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private GridBagConstraints filterConstraints(int x, int y, double weightx) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.insets = new Insets(0, 0, 0, 10);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = weightx;
        return constraints;
    }

    private void estilizarAreaResumo(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(panelColor);
        textArea.setForeground(textColor);
        textArea.setFont(textArea.getFont().deriveFont(14f));
        textArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    private JPanel buildProdutosTab() {
        JPanel panel = basePanel(new BorderLayout(16, 16));
        produtosFiltroPanel = buildProdutosFiltroPanel();
        produtoFormPanel = wrapPanel("Cadastro de produto", buildProdutoForm());
        panel.add(produtosFiltroPanel, BorderLayout.NORTH);
        panel.add(leftColumn(scrollPane(produtoFormPanel)), BorderLayout.WEST);
        panel.add(wrapPanel("Produtos", new JScrollPane(produtosTable)), BorderLayout.CENTER);

        produtosTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && produtosTable.getSelectedRow() >= 0) {
                    editarProdutoSelecionado();
                }
            }
        });

        return panel;
    }

    private JPanel buildProdutosFiltroPanel() {
        JPanel panel = new RoundedPanel(new GridBagLayout(), 20);
        panel.putClientProperty("surface", "card");
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        produtoBuscaField.putClientProperty("placeholder", "Buscar produto, categoria ou produtor");
        produtoBuscaField.setToolTipText("Buscar produto, categoria ou produtor");
        estoqueBaixoCheckBox.setOpaque(false);
        estoqueBaixoCheckBox.setForeground(textColor);

        produtorProdutosCombo.setPreferredSize(new Dimension(220, 34));
        produtorProdutosFiltroLabel = filterLabel("Produtor");
        produtorProdutosCombo.addActionListener(event -> {
            if (!atualizandoFiltrosProdutor && usuarioAdministrador()) {
                produtosTable.clearSelection();
                limparFormularioProduto();
                atualizarProdutos();
                atualizarResumo();
            }
        });

        GridBagConstraints labelConstraints = filterConstraints(0, 0, 0);
        panel.add(filterLabel("Buscar"), labelConstraints);
        GridBagConstraints searchConstraints = filterConstraints(1, 0, 1);
        searchConstraints.weightx = 1;
        searchConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(produtoBuscaField, searchConstraints);
        panel.add(estoqueBaixoCheckBox, filterConstraints(2, 0, 0));
        panel.add(produtorProdutosFiltroLabel, filterConstraints(0, 1, 0));
        GridBagConstraints producerConstraints = filterConstraints(1, 1, 1);
        producerConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(produtorProdutosCombo, producerConstraints);
        return panel;
    }

    private JPanel buildProdutoForm() {
        JPanel form = formPanel();

        produtoFormStateLabel.setForeground(accentColor);
        produtoFormStateLabel.setFont(produtoFormStateLabel.getFont().deriveFont(Font.BOLD, 13f));

        GridBagConstraints titleConstraints = fieldConstraints(0);
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.gridwidth = 2;
        titleConstraints.weightx = 1;
        form.add(produtoFormStateLabel, titleConstraints);

        addInstruction(form, 1, "Mantenha o catálogo do produtor atualizado; o estoque mínimo alimenta os alertas do resumo.");

        addField(form, 2, "Nome", nomeProdutoField);
        addField(form, 3, "Categoria", categoriaProdutoCombo);
        addField(form, 4, "Preço", precoProdutoField);
        addField(form, 5, "Estoque", estoqueProdutoSpinner);
        addField(form, 6, "Estoque mínimo", estoqueMinimoProdutoSpinner);

        salvarProdutoButton.addActionListener(event -> salvarProduto());
        limparProdutoButton.addActionListener(event -> limparFormularioProduto());
        limparProdutoButton.setText("Limpar");

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.setOpaque(false);
        buttons.add(salvarProdutoButton);
        buttons.add(limparProdutoButton);

        GridBagConstraints gbc = fieldConstraints(7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buttons, gbc);
        addFormFiller(form, 8);

        return form;
    }

    private JPanel buildUsuariosTab() {
        JPanel panel = basePanel(new BorderLayout(16, 16));
        panel.add(buildBuscaPanel("Buscar usuário", usuarioBuscaField), BorderLayout.NORTH);
        panel.add(leftColumn(scrollPane(wrapPanel("Cadastro de usuário", buildUsuarioForm()))), BorderLayout.WEST);
        panel.add(wrapPanel("Usuários", new JScrollPane(usuariosTable)), BorderLayout.CENTER);

        usuariosTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && usuariosTable.getSelectedRow() >= 0) {
                    editarUsuarioSelecionado();
                }
            }
        });

        return panel;
    }

    private JPanel buildUsuarioForm() {
        JPanel form = formPanel();

        usuarioFormStateLabel.setForeground(accentColor);
        usuarioFormStateLabel.setFont(usuarioFormStateLabel.getFont().deriveFont(Font.BOLD, 13f));
        senhaUsuarioField.setToolTipText("Em edição, deixe em branco para manter a senha atual.");
        confirmarSenhaUsuarioField.setToolTipText("Repita apenas quando for definir uma nova senha.");

        GridBagConstraints titleConstraints = fieldConstraints(0);
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.gridwidth = 2;
        titleConstraints.weightx = 1;
        form.add(usuarioFormStateLabel, titleConstraints);

        addInstruction(form, 1, "Cadastre apenas quem participa do fluxo: produtores informam envios e administradores acompanham pedidos.");

        addField(form, 2, "Nome", nomeUsuarioField);
        addField(form, 3, "Tipo", tipoUsuarioCombo);
        addField(form, 4, "Login", loginUsuarioField);
        addField(form, 5, "Senha", senhaUsuarioField);
        addField(form, 6, "Confirmar", confirmarSenhaUsuarioField);

        salvarUsuarioButton.addActionListener(event -> salvarUsuario());
        limparUsuarioButton.addActionListener(event -> limparFormularioUsuario());
        limparUsuarioButton.setText("Limpar");

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.setOpaque(false);
        buttons.add(salvarUsuarioButton);
        buttons.add(limparUsuarioButton);

        GridBagConstraints gbc = fieldConstraints(7);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buttons, gbc);
        addFormFiller(form, 8);

        return form;
    }

    private JPanel buildClientesTab() {
        JPanel panel = basePanel(new BorderLayout(16, 16));
        panel.add(buildBuscaPanel("Buscar cliente", clienteBuscaField), BorderLayout.NORTH);
        panel.add(leftColumn(scrollPane(wrapPanel("Cadastro de cliente", buildClienteForm()))), BorderLayout.WEST);
        panel.add(wrapPanel("Clientes", new JScrollPane(clientesTable)), BorderLayout.CENTER);

        clientesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && clientesTable.getSelectedRow() >= 0) {
                    editarClienteSelecionado();
                }
            }
        });

        return panel;
    }

    private JPanel buildClienteForm() {
        JPanel form = formPanel();

        clienteFormStateLabel.setForeground(accentColor);
        clienteFormStateLabel.setFont(clienteFormStateLabel.getFont().deriveFont(Font.BOLD, 13f));

        GridBagConstraints titleConstraints = fieldConstraints(0);
        titleConstraints.fill = GridBagConstraints.HORIZONTAL;
        titleConstraints.gridwidth = 2;
        titleConstraints.weightx = 1;
        form.add(clienteFormStateLabel, titleConstraints);

        addInstruction(form, 1, "Guarde contato suficiente para retorno quando houver atraso, troca ou divergência no pedido.");

        addField(form, 2, "Nome", nomeClienteField);
        addField(form, 3, "E-mail", emailClienteField);
        addField(form, 4, "Telefone", telefoneClienteField);

        salvarClienteButton.addActionListener(event -> salvarCliente());
        limparClienteButton.addActionListener(event -> limparFormularioCliente());
        limparClienteButton.setText("Limpar");

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.setOpaque(false);
        buttons.add(salvarClienteButton);
        buttons.add(limparClienteButton);

        GridBagConstraints gbc = fieldConstraints(5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        form.add(buttons, gbc);
        addFormFiller(form, 6);

        return form;
    }

    private JPanel buildPedidosTab() {
        JPanel panel = basePanel(new BorderLayout(16, 16));

        pedidosFiltroPanel = buildPedidosFiltroPanel();
        panel.add(pedidosFiltroPanel, BorderLayout.NORTH);

        novoPedidoPanel = wrapPanel(TITULO_REGISTRAR_PEDIDO, scrollPane(buildPedidoForm()));
        pedidoStatusPanel = wrapPanel("Envio e recebimento", scrollPane(buildStatusForm()));
        relatorioOperacionalPanel = wrapPanel("Listas operacionais", scrollPane(buildRelatorioOperacionalPanel()));
        pedidosToolsTabs = new javax.swing.JTabbedPane();
        pedidosToolsTabs.setUI(new ThemedTabbedPaneUI());
        pedidosToolsTabs.setOpaque(false);
        pedidosToolsTabs.setBackground(backgroundColor);
        pedidosToolsTabs.setForeground(textColor);
        pedidosToolsTabs.setBorder(BorderFactory.createEmptyBorder());
        pedidosToolsTabs.addTab("Envio e recebimento", pedidoStatusPanel);
        pedidosToolsTabs.addTab("Listas", relatorioOperacionalPanel);
        pedidosToolsTabs.addTab(TITULO_REGISTRAR_PEDIDO, novoPedidoPanel);
        pedidosToolsTabs.setPreferredSize(new Dimension(390, 0));
        pedidosToolsTabs.setMinimumSize(new Dimension(300, 300));

        detalhesPedidoArea.setEditable(false);
        detalhesPedidoArea.setLineWrap(true);
        detalhesPedidoArea.setWrapStyleWord(true);
        detalhesPedidoArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        detalhesPedidoArea.setBackground(fieldColor);
        detalhesPedidoArea.setForeground(textColor);

        JPanel detalhesPanel = new JPanel(new BorderLayout());
        detalhesPanel.setOpaque(false);
        detalhesPanel.add(sectionTitle("Detalhes do pedido"), BorderLayout.NORTH);
        detalhesPanel.add(new JScrollPane(detalhesPedidoArea), BorderLayout.CENTER);
        JSplitPane pedidosContent = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(pedidosTable),
                detalhesPanel
        );
        pedidosContent.setResizeWeight(0.62);
        pedidosContent.setDividerLocation(0.62);
        pedidosContent.setContinuousLayout(true);
        pedidosContent.setBorder(BorderFactory.createEmptyBorder());
        pedidosContent.setOpaque(false);

        pedidosTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                Pedido pedido = pedidoSelecionadoNaTabela();
                if (pedido != null) {
                    selecionarPedidoNoFormulario(pedido.getId());
                }
                atualizarDetalhesPedido();
            }
        });

        pedidosSituacaoTabs = new javax.swing.JTabbedPane();
        pedidosSituacaoTabs.setUI(new ThemedTabbedPaneUI());
        pedidosSituacaoTabs.setOpaque(false);
        pedidosSituacaoTabs.setBackground(backgroundColor);
        pedidosSituacaoTabs.setForeground(textColor);
        pedidosSituacaoTabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        pedidosSituacaoTabs.setPreferredSize(new Dimension(0, 44));
        pedidosSituacaoTabs.addTab("Em andamento", transparentPanel());
        pedidosSituacaoTabs.addTab("Finalizados", transparentPanel());
        pedidosSituacaoTabs.addTab("Cancelados", transparentPanel());
        pedidosSituacaoTabs.addChangeListener(event -> {
            pedidosTable.clearSelection();
            atualizarPedidos();
            atualizarDetalhesPedido();
            atualizarRelatorioOperacional();
        });

        JPanel pedidosListPanel = new JPanel(new BorderLayout(0, 8));
        pedidosListPanel.setOpaque(false);
        pedidosListPanel.add(pedidosSituacaoTabs, BorderLayout.NORTH);
        pedidosListPanel.add(pedidosContent, BorderLayout.CENTER);

        JSplitPane pedidosLayout = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                pedidosToolsTabs,
                wrapPanel("Pedidos", pedidosListPanel)
        );
        pedidosLayout.setResizeWeight(0.24);
        pedidosLayout.setDividerLocation(400);
        pedidosLayout.setOneTouchExpandable(true);
        pedidosLayout.setContinuousLayout(true);
        pedidosLayout.setBorder(BorderFactory.createEmptyBorder());
        pedidosLayout.setOpaque(false);
        panel.add(pedidosLayout, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPedidosFiltroPanel() {
        JPanel panel = new RoundedPanel(new GridBagLayout(), 20);
        panel.putClientProperty("surface", "card");
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        pedidoBuscaField.putClientProperty("placeholder", "Buscar por pedido, cliente, produto ou produtor");
        pedidoBuscaField.setToolTipText("Buscar por pedido, cliente, produto ou produtor");

        produtorPedidosCombo.setPreferredSize(new Dimension(220, 34));
        statusPedidoFiltroCombo.setPreferredSize(new Dimension(180, 34));
        produtorPedidosFiltroLabel = filterLabel("Produtor");
        produtorPedidosCombo.addActionListener(event -> {
            if (!atualizandoFiltrosProdutor && usuarioAdministrador()) {
                pedidosTable.clearSelection();
                atualizarPedidos();
                atualizarResumo();
                atualizarDetalhesPedido();
                atualizarCombosPedidoStatus();
                atualizarRelatorioOperacional();
            }
        });

        GridBagConstraints labelConstraints = filterConstraints(0, 0, 0);
        panel.add(filterLabel("Buscar"), labelConstraints);
        GridBagConstraints searchConstraints = filterConstraints(1, 0, 1);
        searchConstraints.weightx = 1;
        searchConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pedidoBuscaField, searchConstraints);
        panel.add(filterLabel("Status"), filterConstraints(0, 1, 0));
        GridBagConstraints statusConstraints = filterConstraints(1, 1, 1);
        statusConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statusPedidoFiltroCombo, statusConstraints);
        panel.add(produtorPedidosFiltroLabel, filterConstraints(2, 1, 0));
        GridBagConstraints producerConstraints = filterConstraints(3, 1, 1);
        producerConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(produtorPedidosCombo, producerConstraints);

        JLabel filtroHelper = helperLabel("Os filtros operacionais destacam onde agir: divergências, itens aguardando produtor e itens em trânsito.");
        GridBagConstraints helperConstraints = filterConstraints(0, 2, 1);
        helperConstraints.gridwidth = 4;
        helperConstraints.fill = GridBagConstraints.HORIZONTAL;
        helperConstraints.insets = new Insets(8, 0, 0, 10);
        panel.add(filtroHelper, helperConstraints);
        return panel;
    }

    private JPanel buildPedidoForm() {
        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setOpaque(false);

        JPanel fields = formPanel();

        addInstruction(fields, 0, "Registre pedidos recebidos para acompanhamento; os itens adicionados já reservam estoque.");

        addField(fields, 1, "Cliente", clientePedidoCombo);
        addField(fields, 2, "Entrega", tipoEntregaCombo);
        dataPrevistaPedidoField.setToolTipText("Data prevista para entrega ou retirada. Use dd/mm/aaaa.");
        addField(fields, 3, "Data prevista", dataPrevistaPedidoField);
        addField(fields, 4, "Produto", produtoPedidoCombo);
        addField(fields, 5, "Quantidade", quantidadePedidoSpinner);

        adicionarItemPedidoButton.addActionListener(event -> adicionarItemAoNovoPedido());
        removerItemPedidoButton.addActionListener(event -> removerItemDoNovoPedido());
        removerItemPedidoButton.setEnabled(false);
        produtoPedidoCombo.addActionListener(event -> ajustarQuantidadeMaximaPedido());

        JPanel itemButtons = new JPanel(new GridLayout(1, 2, 6, 0));
        itemButtons.setOpaque(false);
        itemButtons.add(adicionarItemPedidoButton);
        itemButtons.add(removerItemPedidoButton);

        GridBagConstraints itemButtonsConstraints = fieldConstraints(6);
        itemButtonsConstraints.fill = GridBagConstraints.HORIZONTAL;
        itemButtonsConstraints.gridwidth = 2;
        itemButtonsConstraints.weightx = 1;
        fields.add(itemButtons, itemButtonsConstraints);

        JPanel itensPanel = new JPanel(new BorderLayout(0, 8));
        itensPanel.setOpaque(false);
        itensNovoPedidoTable.setPreferredScrollableViewportSize(new Dimension(360, 150));
        itensPanel.add(new JScrollPane(itensNovoPedidoTable), BorderLayout.CENTER);

        JButton criar = primaryButton(TITULO_REGISTRAR_PEDIDO);
        criar.addActionListener(event -> criarPedido());
        itensPanel.add(criar, BorderLayout.SOUTH);

        form.add(fields, BorderLayout.NORTH);
        form.add(itensPanel, BorderLayout.CENTER);

        return form;
    }

    private JPanel buildRelatorioOperacionalPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel fields = formPanel();
        addInstruction(fields, 0, "Use os filtros acima para montar listas de trabalho por produtor, conferência ou separação.");
        relatorioOperacionalCombo.setToolTipText("Escolha a lista operacional que deseja gerar.");
        addField(fields, 1, "Lista", relatorioOperacionalCombo);
        addFormFiller(fields, 2);

        relatorioOperacionalArea.setEditable(false);
        relatorioOperacionalArea.setLineWrap(true);
        relatorioOperacionalArea.setWrapStyleWord(true);
        relatorioOperacionalArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        relatorioOperacionalArea.setBackground(fieldColor);
        relatorioOperacionalArea.setForeground(textColor);

        relatorioOperacionalCombo.addActionListener(event -> atualizarRelatorioOperacional());
        atualizarRelatorioButton.addActionListener(event -> atualizarRelatorioOperacional());
        copiarRelatorioButton.addActionListener(event -> copiarRelatorioOperacional());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 6, 0));
        buttons.setOpaque(false);
        buttons.add(atualizarRelatorioButton);
        buttons.add(copiarRelatorioButton);

        panel.add(fields, BorderLayout.NORTH);
        panel.add(new JScrollPane(relatorioOperacionalArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildStatusForm() {
        JPanel form = new JPanel(new BorderLayout(12, 10));
        form.setOpaque(false);

        JPanel fields = formPanel();
        addInstruction(fields, 0, "Selecione um item e registre apenas o que aconteceu agora; a quantidade aparece somente quando a operação for parcial.");

        addField(fields, 1, "Pedido", pedidoStatusCombo);
        addField(fields, 2, "Item", itemStatusCombo);
        acaoItemMensagemLabel.putClientProperty("role", "muted");
        acaoItemMensagemLabel.setFont(acaoItemMensagemLabel.getFont().deriveFont(Font.BOLD, 12f));
        addField(fields, 3, "Ação", acaoItemMensagemLabel);
        operacaoCompletaCheckBox.setToolTipText("Marque para registrar a quantidade completa desta etapa.");
        operacaoCompletaLabel = addField(fields, 4, "Envio", operacaoCompletaCheckBox);
        quantidadeEnviadaSpinner.setToolTipText("Quantidade enviada nesta movimentação.");
        quantidadeEnviadaLabel = addField(fields, 5, "Enviar agora", quantidadeEnviadaSpinner);
        quantidadeRecebidaSpinner.setToolTipText("Quantidade recebida nesta conferência.");
        quantidadeRecebidaLabel = addField(fields, 6, "Receber agora", quantidadeRecebidaSpinner);
        fluxoItemPedidoLabel.putClientProperty("role", "muted");
        fluxoItemPedidoLabel.setFont(fluxoItemPedidoLabel.getFont().deriveFont(Font.BOLD, 12f));
        addField(fields, 7, "Situação", fluxoItemPedidoLabel);
        statusPedidoLabel = addField(fields, 8, "Etapa do pedido", statusPedidoCombo);
        observacaoPedidoField.setToolTipText("Observação opcional para o histórico do pedido.");
        addField(fields, 9, "Observação", observacaoPedidoField);
        addFormFiller(fields, 10);

        pedidoStatusCombo.addActionListener(event -> {
            atualizarItensStatusCombo();
            atualizarAcoesPedido();
        });
        itemStatusCombo.addActionListener(event -> atualizarAcoesPedido());
        operacaoCompletaCheckBox.addActionListener(event -> {
            atualizarCampoQuantidadeEnviada();
            atualizarCampoQuantidadeRecebida();
            atualizarControleAcaoItemSelecionado();
        });
        atualizarItemPedidoButton = primaryButton("Aplicar ação");
        atualizarItemPedidoButton.addActionListener(event -> atualizarStatusItemPedido());
        itemIndisponivelButton.addActionListener(event -> marcarItemIndisponivel());
        atualizarPedidoStatusButton = secondaryButton("Atualizar pedido");
        atualizarPedidoStatusButton.addActionListener(event -> atualizarStatusPedido());
        cancelarPedidoButton.addActionListener(event -> cancelarPedido());
        excluirPedidoButton.addActionListener(event -> excluirPedido());

        JPanel buttons = new JPanel();
        buttons.setLayout(new javax.swing.BoxLayout(buttons, javax.swing.BoxLayout.Y_AXIS));
        buttons.setOpaque(false);
        buttons.add(atualizarItemPedidoButton);
        buttons.add(itemIndisponivelButton);
        buttons.add(atualizarPedidoStatusButton);
        buttons.add(cancelarPedidoButton);
        buttons.add(excluirPedidoButton);

        for (Component button : buttons.getComponents()) {
            if (button instanceof JButton jButton) {
                jButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                jButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            }
        }

        form.add(fields, BorderLayout.CENTER);
        form.add(buttons, BorderLayout.SOUTH);

        return form;
    }

    private void carregarDadosExemploSeSolicitado() {
        if (!usuarioRepository.estaVazio() || !modoDemonstracaoSolicitado()) {
            return;
        }

        usuarioRepository.salvar("Cassiano", "ADMIN", "admin", "admin123");
        Usuario produtor1 = usuarioRepository.salvar("Produtor da Agricultura", "PRODUTOR", "agricultura", "agro123");
        Usuario produtor2 = usuarioRepository.salvar("Artesão Associado", "PRODUTOR", "artesao", "arte123");
        clienteRepository.salvar("Cliente Exemplo");

        produtoController.cadastrarProduto("Cesta de verduras", "AGRICULTURA", 35.00, 10, 4, produtor1);
        produtoController.cadastrarProduto("Pano de prato artesanal", "ARTESANATO", 22.50, 8, 2, produtor2);
    }

    private boolean modoDemonstracaoSolicitado() {
        String propriedade = System.getProperty("coopmanager.demo", "");
        String ambiente = System.getenv("COOPMANAGER_DEMO");
        return valorVerdadeiro(propriedade) || valorVerdadeiro(ambiente);
    }

    private boolean valorVerdadeiro(String value) {
        if (value == null) {
            return false;
        }

        String normalized = value.trim();
        return "true".equalsIgnoreCase(normalized)
                || "1".equals(normalized)
                || "sim".equalsIgnoreCase(normalized)
                || "yes".equalsIgnoreCase(normalized);
    }

    private boolean garantirUsuarioInicial() {
        if (usuarioRepository.estaVazio()) {
            Usuario primeiroAdministrador = exibirPrimeiroAcesso();
            if (primeiroAdministrador == null) {
                return false;
            }

            produtorLogado = primeiroAdministrador;
            aplicarTemaPreferidoDoUsuario(produtorLogado);
            atualizarProdutorLogado();
            limparFormularioProduto();
            limparFormularioUsuario();
            limparFormularioCliente();
            limparNovoPedido();
            return true;
        }

        return exibirLogin();
    }

    private Usuario exibirPrimeiroAcesso() {
        JTextField nomeField = new JTextField(20);
        JTextField loginField = new JTextField(20);
        JPasswordField senhaField = new JPasswordField(20);
        JPasswordField confirmarSenhaField = new JPasswordField(20);
        JFrame setupWindow = new JFrame("CoopManager - Primeiro acesso");
        SecondaryLoop setupLoop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
        Usuario[] usuarioCriado = {null};

        JPanel content = basePanel(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.add(sectionTitle("Primeiro acesso"), BorderLayout.NORTH);

        JTextArea helper = new JTextArea("Crie o primeiro usuário para usar o CoopManager.");
        helper.setEditable(false);
        helper.setLineWrap(true);
        helper.setWrapStyleWord(true);
        helper.setOpaque(false);
        helper.setFocusable(false);
        helper.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helper.setForeground(mutedTextColor);
        header.add(helper, BorderLayout.CENTER);
        content.add(header, BorderLayout.NORTH);

        JPanel form = formPanel();
        form.setPreferredSize(new Dimension(390, 180));
        addField(form, 0, "Nome", nomeField);
        addField(form, 1, "Login", loginField);
        addField(form, 2, "Senha", senhaField);
        addField(form, 3, "Confirmar", confirmarSenhaField);
        content.add(form, BorderLayout.CENTER);

        JButton sairButton = secondaryButton("Sair");
        JButton criarButton = primaryButton("Criar administrador");
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(sairButton);
        buttons.add(criarButton);
        content.add(buttons, BorderLayout.SOUTH);

        Runnable fechar = () -> {
            setupWindow.dispose();
            setupLoop.exit();
        };

        Runnable criarAdministrador = () -> {
            String nome = nomeField.getText().trim();
            String login = loginField.getText().trim();
            String senha = new String(senhaField.getPassword());
            String confirmarSenha = new String(confirmarSenhaField.getPassword());

            if (nome.isEmpty()) {
                mostrarErroPrimeiroAcesso(setupWindow, "Informe o nome do administrador.");
                nomeField.requestFocusInWindow();
                return;
            }

            if (login.isEmpty()) {
                mostrarErroPrimeiroAcesso(setupWindow, "Informe o login do administrador.");
                loginField.requestFocusInWindow();
                return;
            }

            if (senha.isBlank()) {
                mostrarErroPrimeiroAcesso(setupWindow, "Informe uma senha para o administrador.");
                senhaField.requestFocusInWindow();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                mostrarErroPrimeiroAcesso(setupWindow, "A confirmação de senha não confere.");
                confirmarSenhaField.setText("");
                confirmarSenhaField.requestFocusInWindow();
                return;
            }

            try {
                usuarioCriado[0] = usuarioRepository.salvar(nome, "ADMIN", login, senha);
                JOptionPane.showMessageDialog(
                        setupWindow,
                        "Administrador criado com sucesso. Você já será conectado ao CoopManager.",
                        "CoopManager",
                        JOptionPane.INFORMATION_MESSAGE
                );
                fechar.run();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarErroPrimeiroAcesso(setupWindow, exception.getMessage());
            }
        };

        criarButton.addActionListener(event -> criarAdministrador.run());
        sairButton.addActionListener(event -> fechar.run());
        nomeField.addActionListener(event -> loginField.requestFocusInWindow());
        loginField.addActionListener(event -> senhaField.requestFocusInWindow());
        senhaField.addActionListener(event -> confirmarSenhaField.requestFocusInWindow());
        confirmarSenhaField.addActionListener(event -> criarAdministrador.run());
        setupWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                fechar.run();
            }
        });

        boolean mainWasEnabled = isEnabled();
        setEnabled(false);
        setupWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setupWindow.setIconImages(getIconImages());
        aplicarTemaEm(content);
        setupWindow.setContentPane(content);
        setupWindow.setResizable(false);
        setupWindow.setMinimumSize(new Dimension(420, 350));
        setupWindow.pack();
        setupWindow.setLocationRelativeTo(isShowing() ? this : null);
        setupWindow.setVisible(true);
        setupWindow.toFront();
        nomeField.requestFocusInWindow();
        setupLoop.enter();
        setEnabled(mainWasEnabled);

        return usuarioCriado[0];
    }

    private void mostrarErroPrimeiroAcesso(JFrame owner, String message) {
        String texto = message == null || message.isBlank()
                ? "Não foi possível criar o administrador."
                : message;
        JOptionPane.showMessageDialog(owner, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private boolean exibirLogin() {
        JTextField loginField = new JTextField(18);
        JPasswordField senhaField = new JPasswordField(18);
        JFrame loginWindow = new JFrame("CoopManager - Login");
        SecondaryLoop loginLoop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
        boolean[] autenticado = {false};

        JPanel content = basePanel(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = sectionTitle("Login");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        content.add(title, BorderLayout.NORTH);

        JPanel loginPanel = formPanel();
        loginPanel.setPreferredSize(new Dimension(340, 88));
        addField(loginPanel, 0, "Login", loginField);
        addField(loginPanel, 1, "Senha", senhaField);
        content.add(loginPanel, BorderLayout.CENTER);

        JButton entrarButton = primaryButton("Entrar");
        JButton cancelarButton = secondaryButton("Cancelar");
        JButton loginTemaButton = secondaryButton(temaSymbol());
        loginTemaButton.setToolTipText(temaTooltip());
        loginTemaButton.setPreferredSize(new Dimension(44, 36));

        JPanel buttons = new JPanel(new BorderLayout(8, 0));
        buttons.setOpaque(false);
        JPanel loginActions = new JPanel(new GridLayout(1, 2, 8, 0));
        loginActions.setOpaque(false);
        loginActions.add(cancelarButton);
        loginActions.add(entrarButton);
        buttons.add(loginTemaButton, BorderLayout.WEST);
        buttons.add(loginActions, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        Runnable fecharLogin = () -> {
            loginWindow.dispose();
            loginLoop.exit();
        };

        Runnable tentarLogin = () -> {
            Usuario usuario = usuarioRepository.autenticarUsuario(
                    loginField.getText().trim(),
                    new String(senhaField.getPassword())
            );

            if (usuario == null) {
                JOptionPane.showMessageDialog(loginWindow, "Login ou senha inválidos.", "Atenção", JOptionPane.WARNING_MESSAGE);
                senhaField.setText("");
                senhaField.requestFocusInWindow();
                return;
            }

            produtorLogado = usuario;
            aplicarTemaPreferidoDoUsuario(produtorLogado);
            atualizarProdutorLogado();
            limparFormularioProduto();
            limparFormularioUsuario();
            limparFormularioCliente();
            limparNovoPedido();
            autenticado[0] = true;
            fecharLogin.run();
        };

        entrarButton.addActionListener(event -> tentarLogin.run());
        loginField.addActionListener(event -> senhaField.requestFocusInWindow());
        senhaField.addActionListener(event -> tentarLogin.run());
        cancelarButton.addActionListener(event -> fecharLogin.run());
        loginTemaButton.addActionListener(event -> {
            alternarTema(false);
            loginTemaButton.setText(temaSymbol());
            loginTemaButton.setToolTipText(temaTooltip());
            content.setBackground(backgroundColor);
            aplicarTemaEm(content);
            loginWindow.repaint();
        });
        loginWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                fecharLogin.run();
            }
        });

        boolean mainWasEnabled = isEnabled();
        setEnabled(false);
        loginWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loginWindow.setIconImages(getIconImages());
        aplicarTemaEm(content);
        loginWindow.setContentPane(content);
        loginWindow.setResizable(false);
        loginWindow.pack();
        loginWindow.setLocationRelativeTo(isShowing() ? this : null);
        loginWindow.setVisible(true);
        loginWindow.toFront();
        loginField.requestFocusInWindow();
        loginLoop.enter();
        setEnabled(mainWasEnabled);

        if (isShowing()) {
            toFront();
        }

        return autenticado[0];
    }

    private void trocarProdutor() {
        Usuario produtorAnterior = produtorLogado;
        if (exibirLogin()) {
            produtosTable.clearSelection();
            pedidosTable.clearSelection();
            atualizarTela();
        } else {
            produtorLogado = produtorAnterior;
            aplicarTemaPreferidoDoUsuario(produtorLogado);
        }
    }

    private void editarMeuCadastro() {
        if (produtorLogado == null) {
            return;
        }

        JTextField nomeField = new JTextField(produtorLogado.getNome());
        JTextField loginField = new JTextField(produtorLogado.getLogin());
        JPasswordField senhaField = new JPasswordField();
        JPasswordField confirmarSenhaField = new JPasswordField();
        JPanel form = formPanel();
        form.setPreferredSize(new Dimension(340, 190));

        addField(form, 0, "Nome", nomeField);
        addField(form, 1, "Login", loginField);
        addField(form, 2, "Nova senha", senhaField);
        addField(form, 3, "Confirmar", confirmarSenhaField);
        aplicarTemaEm(form);

        if (!exibirDialogoMeuCadastro(form)) {
            return;
        }

        String nome = nomeField.getText().trim();
        String login = loginField.getText().trim();
        String senha = new String(senhaField.getPassword());
        String confirmarSenha = new String(confirmarSenhaField.getPassword());

        if (nome.isEmpty()) {
            showError("Informe o nome do usuário.");
            return;
        }

        if (login.isEmpty()) {
            showError("Informe o login do usuário.");
            return;
        }

        if (!produtorLogado.possuiSenha() && senha.isBlank()) {
            showError("Informe uma senha para este login.");
            return;
        }

        if (senha.isBlank() && !confirmarSenha.isBlank()) {
            showError("Informe a senha antes de confirmar.");
            return;
        }

        if (!senha.isBlank() && !senha.equals(confirmarSenha)) {
            showError("A confirmação de senha não confere.");
            return;
        }

        try {
            boolean sucesso = usuarioRepository.atualizar(
                    produtorLogado.getId(),
                    nome,
                    produtorLogado.getTipo(),
                    login,
                    senha
            );

            if (!sucesso) {
                showError("Usuário não encontrado.");
                return;
            }

            produtorLogado = usuarioRepository.buscarPorId(produtorLogado.getId());
            atualizarProdutorLogado();
            limparFormularioUsuario();
            atualizarTela();
            showInfo("Cadastro atualizado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    private boolean exibirDialogoMeuCadastro(JPanel form) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, "Meu cadastro", true);
        boolean[] confirmado = {false};

        JPanel content = basePanel(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        content.add(form, BorderLayout.CENTER);

        JButton cancelarButton = secondaryButton("Cancelar");
        JButton salvarButton = primaryButton("OK");
        cancelarButton.addActionListener(event -> dialog.dispose());
        salvarButton.addActionListener(event -> {
            confirmado[0] = true;
            dialog.dispose();
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(salvarButton);
        buttons.add(cancelarButton);
        content.add(buttons, BorderLayout.SOUTH);

        aplicarTemaEm(content);
        dialog.setContentPane(content);
        dialog.setIconImages(getIconImages());
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(dialogOwner());
        dialog.setVisible(true);
        return confirmado[0];
    }

    private void alternarTema() {
        alternarTema(true);
    }

    private void alternarTema(boolean salvarPreferencia) {
        darkMode = !darkMode;
        aplicarTema();
        if (salvarPreferencia) {
            salvarTemaDoUsuarioLogado();
        }
    }

    private String temaSymbol() {
        return darkMode ? "\u2600" : "\u263E";
    }

    private String temaTooltip() {
        return darkMode ? "Trocar para modo claro" : "Trocar para modo noturno";
    }

    private void aplicarTemaPreferidoDoUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        darkMode = usuario.isTemaEscuro();
        aplicarTema();
    }

    private void salvarTemaDoUsuarioLogado() {
        if (produtorLogado == null) {
            return;
        }

        try {
            if (usuarioRepository.atualizarTema(produtorLogado.getId(), darkMode)) {
                produtorLogado.atualizarTemaEscuro(darkMode);
            }
        } catch (IllegalStateException exception) {
            showError("N\u00E3o foi poss\u00EDvel salvar a prefer\u00EAncia de tema do usu\u00E1rio.");
        }
    }

    private void salvarProduto() {
        String nome = nomeProdutoField.getText().trim();
        String categoria = (String) categoriaProdutoCombo.getSelectedItem();
        double preco;

        if (produtorLogado == null) {
            showError("Faça login como produtor para gerenciar produtos.");
            return;
        }

        if (nome.isEmpty()) {
            showError("Informe o nome do produto.");
            return;
        }

        try {
            preco = parseMoney(precoProdutoField.getText());
        } catch (NumberFormatException exception) {
            showError("Informe um preço válido.");
            return;
        }

        if (preco <= 0) {
            showError("O preço precisa ser maior que zero.");
            return;
        }

        int estoque = (Integer) estoqueProdutoSpinner.getValue();
        int estoqueMinimo = (Integer) estoqueMinimoProdutoSpinner.getValue();
        precoProdutoField.setText(currencyFormat.format(preco));
        produtoController.cadastrarProduto(nome, categoria, preco, estoque, estoqueMinimo, produtorLogado);
        showInfo("Produto cadastrado com sucesso.");

        limparFormularioProduto();
        atualizarTela();
    }

    private void excluirProduto() {
        if (produtoSelecionadoId <= 0) {
            showError("Selecione um produto para excluir.");
            return;
        }

        Produto produto = produtoController.buscarProduto(produtoSelecionadoId);
        if (!produtoPertenceAoProdutor(produto)) {
            showError("Produto não encontrado para este produtor.");
            return;
        }

        if (produtoEmPedido(produto.getId())) {
            showError("Este produto já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o produto \"" + produto.getNome() + "\" do catálogo?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        if (produtoController.removerProduto(produtoSelecionadoId, produtorLogado)) {
            limparFormularioProduto();
            atualizarTela();
            showInfo("Produto excluído com sucesso.");
        } else {
            showError("Não foi possível excluir o produto.");
        }
    }

    private void salvarUsuario() {
        String nome = nomeUsuarioField.getText().trim();
        String tipo = (String) tipoUsuarioCombo.getSelectedItem();
        String login = loginUsuarioField.getText().trim();
        String senha = new String(senhaUsuarioField.getPassword());
        String confirmarSenha = new String(confirmarSenhaUsuarioField.getPassword());

        if (nome.isEmpty()) {
            showError("Informe o nome do usuário.");
            return;
        }

        if (tipo == null || tipo.isBlank()) {
            showError("Selecione o tipo do usuário.");
            return;
        }

        boolean administrador = usuarioAdministrador();

        if (!administrador) {
            showError("Apenas administradores podem cadastrar usuários.");
            return;
        }

        if (login.isEmpty()) {
            showError("Informe o login do usuário.");
            return;
        }

        if (senha.isBlank()) {
            showError("Informe a senha para este login.");
            return;
        }

        if (senha.isBlank() && !confirmarSenha.isBlank()) {
            showError("Informe a senha antes de confirmar.");
            return;
        }

        if (!senha.isBlank() && !senha.equals(confirmarSenha)) {
            showError("A confirmação de senha não confere.");
            return;
        }

        try {
            usuarioRepository.salvar(nome, tipo, login, senha);
            showInfo("Usuário cadastrado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
            return;
        }

        limparFormularioUsuario();
        atualizarTela();
    }

    private void excluirUsuario() {
        if (!usuarioAdministrador()) {
            showError("Apenas administradores podem excluir usuários.");
            return;
        }

        if (usuarioSelecionadoId <= 0) {
            showError("Selecione um usuário para excluir.");
            return;
        }

        if (produtorLogado != null && usuarioSelecionadoId == produtorLogado.getId()) {
            showError("Você não pode excluir o próprio usuário logado.");
            return;
        }

        Usuario usuario = usuarioRepository.buscarPorId(usuarioSelecionadoId);
        if (usuario == null) {
            showError("Usuário não encontrado.");
            return;
        }

        if ("ADMIN".equals(usuario.getTipo()) && quantidadeAdministradores() <= 1) {
            showError("Não é possível excluir o último administrador.");
            return;
        }

        if ("PRODUTOR".equals(usuario.getTipo()) && produtorEmPedido(usuario.getId())) {
            showError("Este produtor já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return;
        }

        if ("PRODUTOR".equals(usuario.getTipo()) && produtorTemProdutos(usuario.getId())) {
            showError("Este produtor ainda possui produtos cadastrados. Remova esses produtos antes de excluir o produtor.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o usuário \"" + usuario.getNome() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        if (usuarioRepository.remover(usuarioSelecionadoId)) {
            limparFormularioUsuario();
            atualizarTela();
            showInfo("Usuário excluído com sucesso.");
        } else {
            showError("Não foi possível excluir o usuário.");
        }
    }

    private void salvarCliente() {
        String nome = nomeClienteField.getText().trim();
        String email = emailClienteField.getText().trim();
        String telefone = telefoneClienteField.getText().trim();

        if (nome.isEmpty()) {
            showError("Informe o nome do cliente.");
            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            showError("Informe um e-mail válido ou deixe o campo em branco.");
            return;
        }

        clienteRepository.salvar(nome, email, telefone);
        showInfo("Cliente cadastrado com sucesso.");

        limparFormularioCliente();
        atualizarTela();
    }

    private void excluirCliente() {
        if (clienteSelecionadoId <= 0) {
            showError("Selecione um cliente para excluir.");
            return;
        }

        Cliente cliente = clienteRepository.buscarPorId(clienteSelecionadoId);
        if (cliente == null) {
            showError("Cliente não encontrado.");
            return;
        }

        if (clienteEmPedido(cliente.getId())) {
            showError("Este cliente já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o cliente \"" + cliente.getNome() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        if (clienteRepository.remover(clienteSelecionadoId)) {
            limparFormularioCliente();
            atualizarTela();
            showInfo("Cliente excluído com sucesso.");
        } else {
            showError("Não foi possível excluir o cliente.");
        }
    }

    private void adicionarItemAoNovoPedido() {
        Produto produto = (Produto) produtoPedidoCombo.getSelectedItem();
        int quantidade = (Integer) quantidadePedidoSpinner.getValue();

        if (produto == null) {
            showError("Selecione um produto com estoque disponível.");
            return;
        }

        int disponivel = estoqueDisponivelParaNovoPedido(produto);
        if (quantidade <= 0 || quantidade > disponivel) {
            showError("Quantidade indisponível em estoque.");
            return;
        }

        int index = indiceItemNovoPedido(produto);
        if (index >= 0) {
            ItemPedido itemAtual = itensNovoPedido.get(index);
            itensNovoPedido.set(index, new ItemPedido(produto, itemAtual.getQuantidade() + quantidade));
        } else {
            itensNovoPedido.add(new ItemPedido(produto, quantidade));
        }

        quantidadePedidoSpinner.setValue(1);
        atualizarItensNovoPedido();
    }

    private void removerItemDoNovoPedido() {
        int selectedRow = itensNovoPedidoTable.getSelectedRow();
        if (selectedRow < 0 && !itensNovoPedido.isEmpty()) {
            selectedRow = itensNovoPedido.size() - 1;
        }

        if (selectedRow < 0) {
            return;
        }

        int modelRow = itensNovoPedidoTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < itensNovoPedido.size()) {
            itensNovoPedido.remove(modelRow);
            atualizarItensNovoPedido();
        }
    }

    private void criarPedido() {
        if (!usuarioAdministrador()) {
            showError("O acompanhamento de pedidos deve ser registrado por um administrador.");
            return;
        }

        Cliente cliente = (Cliente) clientePedidoCombo.getSelectedItem();
        String tipoEntrega = (String) tipoEntregaCombo.getSelectedItem();
        LocalDate dataPrevista;

        if (cliente == null) {
            showError("Selecione um cliente.");
            return;
        }

        try {
            dataPrevista = parseDataOpcional(dataPrevistaPedidoField.getText());
        } catch (DateTimeParseException exception) {
            showError("Informe a data prevista no formato dd/mm/aaaa ou deixe em branco.");
            dataPrevistaPedidoField.requestFocusInWindow();
            return;
        }

        if (dataPrevista != null && dataPrevista.isBefore(LocalDate.now())) {
            showError("A data prevista não pode ser anterior à data de hoje.");
            dataPrevistaPedidoField.requestFocusInWindow();
            return;
        }

        if (itensNovoPedido.isEmpty()) {
            showError("Adicione pelo menos um produto ao pedido.");
            return;
        }

        List<ItemPedido> itensParaSalvar = new ArrayList<>();
        for (ItemPedido item : itensNovoPedido) {
            Produto produto = item.getProduto() == null ? null : produtoController.buscarProduto(item.getProduto().getId());
            if (produto == null) {
                showError("Um dos produtos do pedido n\u00E3o est\u00E1 mais dispon\u00EDvel.");
                return;
            }

            if (item.getQuantidade() <= 0 || item.getQuantidade() > produto.getEstoque()) {
                showError("Estoque insuficiente para o produto \"" + produto.getNome() + "\".");
                return;
            }

            itensParaSalvar.add(new ItemPedido(produto, item.getQuantidade()));
        }

        Pedido pedido = pedidoController.criarPedido(cliente, tipoEntrega, dataPrevista);
        for (ItemPedido item : itensParaSalvar) {
            boolean sucesso = pedidoController.adicionarProdutoAoPedido(pedido, item.getProduto(), item.getQuantidade());
            if (!sucesso) {
                pedidoController.cancelarPedido(pedido.getId());
                showError("Não foi possível adicionar todos os itens. O pedido foi cancelado para manter o estoque correto.");
                return;
            }
        }

        pedidoController.registrarHistorico(pedido.getId(), autorHistorico(), "Pedido registrado para acompanhamento", null);
        limparNovoPedido();
        atualizarTela();
        selecionarPedido(pedido.getId());
        showInfo("Pedido registrado para acompanhamento.");
    }

    private void atualizarItensNovoPedido() {
        DefaultTableModel model = (DefaultTableModel) itensNovoPedidoTable.getModel();
        model.setRowCount(0);

        for (ItemPedido item : itensNovoPedido) {
            Produto produto = item.getProduto();
            model.addRow(new Object[] {
                    nomeProdutor(produto),
                    produto == null ? "Produto removido" : produto.getNome(),
                    item.getQuantidade(),
                    currencyFormat.format(item.getSubtotal())
            });
        }

        removerItemPedidoButton.setEnabled(!itensNovoPedido.isEmpty());
        ajustarQuantidadeMaximaPedido();
    }

    private void limparNovoPedido() {
        itensNovoPedido.clear();
        dataPrevistaPedidoField.setText("");
        quantidadePedidoSpinner.setValue(1);
        atualizarItensNovoPedido();
    }

    private int indiceItemNovoPedido(Produto produto) {
        if (produto == null) {
            return -1;
        }

        for (int index = 0; index < itensNovoPedido.size(); index++) {
            ItemPedido item = itensNovoPedido.get(index);
            Produto itemProduto = item.getProduto();
            if (itemProduto != null && itemProduto.getId() == produto.getId()) {
                return index;
            }
        }
        return -1;
    }

    private int estoqueDisponivelParaNovoPedido(Produto produto) {
        if (produto == null) {
            return 0;
        }

        int quantidadeJaAdicionada = 0;
        for (ItemPedido item : itensNovoPedido) {
            Produto itemProduto = item.getProduto();
            if (itemProduto != null && itemProduto.getId() == produto.getId()) {
                quantidadeJaAdicionada += item.getQuantidade();
            }
        }
        return Math.max(0, produto.getEstoque() - quantidadeJaAdicionada);
    }

    private void ajustarQuantidadeMaximaPedido() {
        if (quantidadePedidoSpinner == null) {
            return;
        }

        Produto produto = (Produto) produtoPedidoCombo.getSelectedItem();
        int disponivel = estoqueDisponivelParaNovoPedido(produto);
        int maximo = Math.max(1, disponivel);
        SpinnerNumberModel model = (SpinnerNumberModel) quantidadePedidoSpinner.getModel();
        int valorAtual = (Integer) model.getValue();
        model.setMaximum(maximo);
        if (valorAtual > maximo) {
            model.setValue(maximo);
        }
        adicionarItemPedidoButton.setEnabled(produto != null && disponivel > 0);
    }

    private void atualizarStatusPedido() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        String status = (String) statusPedidoCombo.getSelectedItem();

        if (!usuarioAdministrador()) {
            showError("Apenas administradores alteram o status geral do pedido.");
            return;
        }

        if (pedido == null) {
            showError("Selecione um pedido.");
            return;
        }

        if (status == null || status.isBlank()) {
            showError("Não há uma próxima etapa disponível para este pedido.");
            return;
        }

        if ("CANCELADO".equals(pedido.getStatus())) {
            showError("Pedidos cancelados não podem ter o status alterado.");
            return;
        }

        if (pedidoController.atualizarStatus(pedido.getId(), status)) {
            pedidoController.registrarHistorico(
                    pedido.getId(),
                    autorHistorico(),
                    "Status do pedido alterado para " + formatarStatus(status),
                    observacaoHistorico()
            );
            observacaoPedidoField.setText("");
            atualizarTela();
            selecionarPedido(pedido.getId());
            showInfo("Status atualizado.");
        } else {
            showError("Pedido não encontrado.");
        }
    }

    private void atualizarStatusItemPedido() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();
        if (pedido == null || item == null || item.getProduto() == null) {
            showError("Selecione um item do pedido.");
            return;
        }

        if ("CANCELADO".equals(pedido.getStatus())) {
            showError("Pedidos cancelados n\u00E3o podem ser alterados.");
            return;
        }

        if (!usuarioAdministrador() && !item.pertenceAoProdutor(produtorLogado)) {
            showError("Voc\u00EA s\u00F3 pode alterar itens dos seus produtos.");
            return;
        }

        String acao = acaoOperacionalSelecionada(item);
        if (acao == null) {
            showError(mensagemAcaoIndisponivel(item));
            return;
        }

        if (usuarioAdministrador() && item.getQuantidadeEnviada() <= item.getQuantidadeRecebida()) {
            showError("Não há quantidade enviada pendente de recebimento para este item.");
            return;
        }

        if (!usuarioAdministrador() && item.getQuantidadeRecebida() >= item.getQuantidade()) {
            showError("Este item já foi recebido por completo pela cooperativa.");
            return;
        }

        int quantidadeEnviada;
        int quantidadeRecebida;
        String statusParaSalvar;
        if (usuarioAdministrador()) {
            quantidadeEnviada = item.getQuantidadeEnviada();
            quantidadeRecebida = quantidadeRecebidaParaAcao(item, acao);
            statusParaSalvar = quantidadeRecebida >= item.getQuantidade() ? "ENTREGUE_COOPERATIVA" : "ENVIADO";
        } else {
            quantidadeRecebida = item.getQuantidadeRecebida();
            quantidadeEnviada = quantidadeEnviadaParaAcao(item, acao);
            statusParaSalvar = "ENVIADO";
        }

        if (usuarioAdministrador() && quantidadeRecebida <= item.getQuantidadeRecebida()) {
            showError("Informe uma quantidade recebida maior que a quantidade já registrada.");
            return;
        }

        if (usuarioAdministrador() && quantidadeRecebida > quantidadeEnviada) {
            showError("A quantidade recebida não pode ser maior que a quantidade enviada pelo produtor.");
            return;
        }

        if (!usuarioAdministrador() && "ENVIADO".equals(statusParaSalvar) && quantidadeEnviada <= item.getQuantidadeEnviada()) {
            showError("Informe uma quantidade enviada maior que a quantidade já registrada.");
            return;
        }

        if (pedidoController.atualizarControleItem(
                pedido.getId(),
                item.getProduto(),
                statusParaSalvar,
                quantidadeEnviada,
                quantidadeRecebida
        )) {
            pedidoController.registrarHistorico(
                    pedido.getId(),
                    autorHistorico(),
                    "Item \"" + item.getProduto().getNome() + "\" atualizado: " + textoAcaoItem(acao, statusParaSalvar)
                            + " (" + textoFluxoItemResumo(quantidadeEnviada, quantidadeRecebida, item.getQuantidade()) + ")",
                    observacaoHistorico()
            );
            registrarFeedbackDivergenciaRecebimento(pedido, item, quantidadeEnviada, quantidadeRecebida);
            observacaoPedidoField.setText("");
            atualizarTela();
            selecionarPedido(pedido.getId());
            showInfo("Status do item atualizado.");
        } else {
            showError("N\u00E3o foi poss\u00EDvel atualizar o item do pedido.");
        }
    }

    private void marcarItemIndisponivel() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();

        if (pedido == null || item == null || item.getProduto() == null) {
            showError("Selecione um item do pedido.");
            return;
        }

        if (usuarioAdministrador() || !item.pertenceAoProdutor(produtorLogado)) {
            showError("A indisponibilidade deve ser informada pelo produtor responsável.");
            return;
        }

        if (item.getQuantidadeEnviada() > 0 || item.getQuantidadeRecebida() > 0) {
            showError("Este item já teve movimentação. Use envio parcial para registrar a quantidade disponível.");
            return;
        }

        if (pedidoController.atualizarControleItem(
                pedido.getId(),
                item.getProduto(),
                "INDISPONIVEL",
                0,
                0
        )) {
            pedidoController.registrarHistorico(
                    pedido.getId(),
                    autorHistorico(),
                    "Item \"" + item.getProduto().getNome() + "\" informado como indisponível",
                    observacaoHistorico()
            );
            observacaoPedidoField.setText("");
            atualizarTela();
            selecionarPedido(pedido.getId());
            showInfo("Item marcado como indisponível.");
        } else {
            showError("Não foi possível atualizar o item do pedido.");
        }
    }

    private String acaoOperacionalSelecionada(ItemPedido item) {
        if (item == null || itemRecebidoOuCancelado(item)) {
            return null;
        }
        if (usuarioAdministrador()) {
            if (item.getQuantidadeEnviada() <= item.getQuantidadeRecebida()) {
                return null;
            }
            if (operacaoCompletaCheckBox.isSelected()) {
                return ACAO_RECEBER_TUDO;
            }
            return permiteRecebimentoParcial(item) ? ACAO_RECEBER_PARCIAL : null;
        }
        if (!item.pertenceAoProdutor(produtorLogado) || item.getQuantidadeEnviada() >= item.getQuantidade()) {
            return null;
        }
        if (operacaoCompletaCheckBox.isSelected()) {
            return ACAO_ENVIAR_TUDO;
        }
        return permiteEnvioParcial(item) ? ACAO_ENVIAR_PARCIAL : null;
    }

    private String mensagemAcaoIndisponivel(ItemPedido item) {
        if (item == null) {
            return "Selecione um item do pedido.";
        }
        if (usuarioAdministrador()) {
            if (item.getQuantidadeRecebida() >= item.getQuantidade()) {
                return "Este item já foi recebido por completo pela cooperativa.";
            }
            return item.getQuantidadeEnviada() <= item.getQuantidadeRecebida()
                    ? "Aguarde o produtor registrar o envio deste item."
                    : "Não há ação disponível para este item.";
        }
        if (!item.pertenceAoProdutor(produtorLogado)) {
            return "Este item pertence a outro produtor.";
        }
        if (item.getQuantidadeRecebida() >= item.getQuantidade()) {
            return "Este item já foi recebido por completo pela cooperativa.";
        }
        if (item.getQuantidadeEnviada() >= item.getQuantidade()) {
            return "Este item já saiu do produtor; aguarde o recebimento pela cooperativa.";
        }
        return "Não há ação disponível para este item.";
    }

    private int quantidadeEnviadaParaAcao(ItemPedido item, String acao) {
        if (item == null || acao == null) {
            return 0;
        }
        if (ACAO_ENVIAR_TUDO.equals(acao)) {
            return item.getQuantidade();
        }
        if (ACAO_ENVIAR_PARCIAL.equals(acao)) {
            return Math.min(item.getQuantidade(), item.getQuantidadeEnviada() + spinnerValue(quantidadeEnviadaSpinner));
        }
        return 0;
    }

    private int quantidadeRecebidaParaAcao(ItemPedido item, String acao) {
        if (item == null || acao == null) {
            return 0;
        }
        if (ACAO_RECEBER_TUDO.equals(acao)) {
            return item.getQuantidadeEnviada();
        }
        if (ACAO_RECEBER_PARCIAL.equals(acao)) {
            return Math.min(item.getQuantidadeEnviada(), item.getQuantidadeRecebida() + spinnerValue(quantidadeRecebidaSpinner));
        }
        return item.getQuantidadeRecebida();
    }

    private void registrarFeedbackDivergenciaRecebimento(
            Pedido pedido,
            ItemPedido item,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) {
        if (!usuarioAdministrador()
                || pedido == null
                || item == null
                || item.getProduto() == null
                || quantidadeRecebida >= quantidadeEnviada) {
            return;
        }

        int diferenca = quantidadeEnviada - quantidadeRecebida;
        pedidoController.registrarHistorico(
                pedido.getId(),
                autorHistorico(),
                "Feedback ao produtor: recebimento parcial do item \"" + item.getProduto().getNome()
                        + "\". Conferir diferenca de " + diferenca + " unidade(s) entre enviado e recebido.",
                null
        );
    }

    private String textoAcaoItem(String acaoSelecionada, String statusSalvo) {
        return switch (acaoSelecionada) {
            case ACAO_RECEBER_TUDO -> "recebimento total confirmado";
            case ACAO_RECEBER_PARCIAL -> "recebimento parcial confirmado";
            case ACAO_ENVIAR_TUDO -> "envio total informado";
            case ACAO_ENVIAR_PARCIAL -> "envio parcial informado";
            default -> formatarStatus(statusSalvo);
        };
    }

    private void cancelarPedido() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        if (!usuarioAdministrador()) {
            showError("Apenas administradores podem cancelar pedidos.");
            return;
        }

        if (pedido == null) {
            showError("Selecione um pedido.");
            return;
        }

        if ("CANCELADO".equals(pedido.getStatus())) {
            showError("Este pedido já está cancelado.");
            return;
        }

        if ("ENTREGUE".equals(pedido.getStatus())) {
            showError("Pedidos já entregues não podem ser cancelados.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Cancelar o pedido #" + pedido.getId() + " e devolver os itens ao estoque?",
                "Confirmar cancelamento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        if (pedidoController.cancelarPedido(pedido.getId())) {
            pedidoController.registrarHistorico(pedido.getId(), autorHistorico(), "Pedido cancelado", observacaoHistorico());
            observacaoPedidoField.setText("");
            atualizarTela();
            selecionarPedido(pedido.getId());
            showInfo("Pedido cancelado e estoque devolvido.");
        } else {
            showError("Não foi possível cancelar o pedido.");
        }
    }

    private void excluirPedido() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        if (!usuarioAdministrador()) {
            showError("Apenas administradores podem excluir pedidos.");
            return;
        }

        if (pedido == null) {
            showError("Selecione um pedido.");
            return;
        }

        boolean pedidoEmAndamento = !"CANCELADO".equals(pedido.getStatus()) && !"ENTREGUE".equals(pedido.getStatus());
        String mensagem = pedidoEmAndamento
                ? "Excluir o pedido #" + pedido.getId() + "? O pedido será cancelado antes para devolver os itens ao estoque."
                : "Excluir definitivamente o pedido #" + pedido.getId() + "?";
        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                mensagem,
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        if (pedidoEmAndamento && !pedidoController.cancelarPedido(pedido.getId())) {
            showError("Não foi possível cancelar o pedido antes da exclusão.");
            return;
        }

        if (pedidoController.removerPedido(pedido.getId())) {
            observacaoPedidoField.setText("");
            atualizarTela();
            showInfo("Pedido excluído.");
        } else {
            showError("Não foi possível excluir o pedido.");
        }
    }

    private void atualizarTela() {
        if (produtorLogado == null) {
            return;
        }

        atualizarCombos();
        atualizarProdutos();
        atualizarUsuarios();
        atualizarClientes();
        atualizarPedidos();
        atualizarResumo();
        atualizarDetalhesPedido();
        atualizarRelatorioOperacional();
        aplicarPermissoesUsuario();
        atualizarNavegacaoPorPerfil();
    }

    private void aplicarPermissoesUsuario() {
        boolean administrador = usuarioAdministrador();
        tipoUsuarioCombo.setEnabled(administrador);
        limparUsuarioButton.setEnabled(administrador);
        excluirUsuarioButton.setEnabled(administrador
                && usuarioSelecionadoId > 0
                && (produtorLogado == null || usuarioSelecionadoId != produtorLogado.getId()));
        if (!administrador && produtorLogado != null && usuarioSelecionadoId != produtorLogado.getId()) {
            carregarUsuarioNoFormulario(produtorLogado);
        }
    }

    private void atualizarNavegacaoPorPerfil() {
        if (navigationTabs == null) {
            return;
        }

        boolean administrador = usuarioAdministrador();
        int usuariosIndex = indiceAba("Usuários");
        if (administrador && usuariosIndex < 0) {
            if (usuariosTabPanel == null) {
                usuariosTabPanel = buildUsuariosTab();
            }
            int clientesIndex = indiceAba("Clientes");
            int insertIndex = clientesIndex < 0 ? navigationTabs.getTabCount() : clientesIndex;
            navigationTabs.insertTab("Usuários", null, usuariosTabPanel, null, insertIndex);
        } else if (!administrador && usuariosIndex >= 0) {
            int selectedIndex = navigationTabs.getSelectedIndex();
            if (selectedIndex >= 0 && "Usuários".equals(navigationTabs.getTitleAt(selectedIndex))) {
                selecionarAba("Resumo");
            }
            navigationTabs.removeTabAt(usuariosIndex);
        }

        int clientesIndex = indiceAba("Clientes");
        if (administrador && clientesIndex < 0) {
            if (clientesTabPanel == null) {
                clientesTabPanel = buildClientesTab();
            }
            int pedidosIndex = indiceAba("Pedidos");
            int insertIndex = pedidosIndex < 0 ? navigationTabs.getTabCount() : pedidosIndex;
            navigationTabs.insertTab("Clientes", null, clientesTabPanel, null, insertIndex);
        } else if (!administrador && clientesIndex >= 0) {
            int selectedIndex = navigationTabs.getSelectedIndex();
            if (selectedIndex >= 0 && "Clientes".equals(navigationTabs.getTitleAt(selectedIndex))) {
                selecionarAba("Resumo");
            }
            navigationTabs.removeTabAt(clientesIndex);
        }

        if (produtoFormPanel != null) {
            produtoFormPanel.setVisible(!administrador);
        }
        if (produtosFiltroPanel != null) {
            produtosFiltroPanel.setVisible(true);
        }
        if (produtorProdutosFiltroLabel != null) {
            produtorProdutosFiltroLabel.setVisible(administrador);
        }
        if (produtorProdutosCombo != null) {
            produtorProdutosCombo.setVisible(administrador);
        }
        if (pedidosFiltroPanel != null) {
            pedidosFiltroPanel.setVisible(true);
        }
        if (produtorPedidosFiltroLabel != null) {
            produtorPedidosFiltroLabel.setVisible(administrador);
        }
        if (produtorPedidosCombo != null) {
            produtorPedidosCombo.setVisible(administrador);
        }
        atualizarFerramentasPedidosPorPerfil(administrador);
        if (novoProdutoQuickButton != null) {
            novoProdutoQuickButton.setVisible(!administrador);
        }
        if (novoPedidoQuickButton != null) {
            novoPedidoQuickButton.setVisible(administrador);
        }
        if (usuariosQuickButton != null) {
            usuariosQuickButton.setVisible(administrador);
        }
        if (clientesQuickButton != null) {
            clientesQuickButton.setVisible(administrador);
        }
        if (acoesRapidasPanel != null) {
            acoesRapidasPanel.revalidate();
            acoesRapidasPanel.repaint();
        }
        revalidate();
        repaint();
    }

    private void atualizarFerramentasPedidosPorPerfil(boolean administrador) {
        if (pedidosToolsTabs == null || novoPedidoPanel == null) {
            return;
        }

        int pedidoIndex = -1;
        for (int index = 0; index < pedidosToolsTabs.getTabCount(); index++) {
            if (TITULO_REGISTRAR_PEDIDO.equals(pedidosToolsTabs.getTitleAt(index))) {
                pedidoIndex = index;
                break;
            }
        }

        if (administrador && pedidoIndex < 0) {
            pedidosToolsTabs.addTab(TITULO_REGISTRAR_PEDIDO, novoPedidoPanel);
        } else if (!administrador && pedidoIndex >= 0) {
            if (pedidosToolsTabs.getSelectedIndex() == pedidoIndex) {
                pedidosToolsTabs.setSelectedIndex(0);
            }
            pedidosToolsTabs.removeTabAt(pedidoIndex);
        }
    }

    private void atualizarCombos() {
        atualizarProdutoresFiltroCombos();
        clienteComboModel.removeAllElements();
        produtoPedidoComboModel.removeAllElements();

        for (Cliente cliente : clienteRepository.listarTodos()) {
            clienteComboModel.addElement(cliente);
        }

        for (Produto produto : produtosDisponiveisParaPedido()) {
            if (produto.getEstoque() > 0) {
                produtoPedidoComboModel.addElement(produto);
            }
        }

        atualizarCombosPedidoStatus();
        atualizarAcoesPedido();
        ajustarQuantidadeMaximaPedido();
    }

    private void atualizarCombosPedidoStatus() {
        Object selecionadoAtual = pedidoStatusCombo.getSelectedItem();
        Integer selecionadoId = selecionadoAtual instanceof Pedido pedido ? pedido.getId() : null;
        pedidoStatusComboModel.removeAllElements();

        Pedido proximoSelecionado = null;
        for (Pedido pedido : pedidosDoProdutor()) {
            pedidoStatusComboModel.addElement(pedido);
            if (selecionadoId != null && pedido.getId() == selecionadoId) {
                proximoSelecionado = pedido;
            }
        }

        if (proximoSelecionado != null) {
            pedidoStatusCombo.setSelectedItem(proximoSelecionado);
        }
        atualizarItensStatusCombo();
        atualizarAcoesPedido();
    }

    private void atualizarItensStatusCombo() {
        Object selecionadoAtual = itemStatusCombo.getSelectedItem();
        Integer produtoSelecionadoId = selecionadoAtual instanceof ItemPedido item && item.getProduto() != null
                ? item.getProduto().getId()
                : null;
        itemStatusComboModel.removeAllElements();

        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        ItemPedido proximoSelecionado = null;
        if (pedido != null) {
            for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                itemStatusComboModel.addElement(item);
                Produto produto = item == null ? null : item.getProduto();
                if (produtoSelecionadoId != null && produto != null && produto.getId() == produtoSelecionadoId) {
                    proximoSelecionado = item;
                }
            }
        }

        if (proximoSelecionado != null) {
            itemStatusCombo.setSelectedItem(proximoSelecionado);
        }
    }

    private void atualizarAcoesPedido() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();
        boolean pedidoAtivo = pedido != null && !"CANCELADO".equals(pedido.getStatus());
        boolean pedidoCancelavel = pedidoAtivo && !"ENTREGUE".equals(pedido.getStatus());
        boolean administrador = usuarioAdministrador();
        cancelarPedidoButton.setEnabled(administrador && pedidoCancelavel);
        excluirPedidoButton.setEnabled(administrador && pedido != null);
        atualizarOpcoesStatusPedido(pedido, administrador, pedidoAtivo);
        boolean podeAtualizarEtapaPedido = administrador && pedidoAtivo && statusPedidoCombo.getItemCount() > 0;
        statusPedidoCombo.setEnabled(podeAtualizarEtapaPedido);
        setCampoControleVisivel(statusPedidoLabel, statusPedidoCombo, podeAtualizarEtapaPedido);
        if (atualizarPedidoStatusButton != null) {
            atualizarPedidoStatusButton.setEnabled(podeAtualizarEtapaPedido);
            atualizarPedidoStatusButton.setVisible(podeAtualizarEtapaPedido);
        }
        cancelarPedidoButton.setVisible(administrador);
        excluirPedidoButton.setVisible(administrador);
        setCampoControleVisivel(quantidadeEnviadaLabel, quantidadeEnviadaSpinner, false);
        setCampoControleVisivel(quantidadeRecebidaLabel, quantidadeRecebidaSpinner, false);
        itemStatusCombo.setEnabled(pedidoAtivo && item != null);
        if (item != null) {
            configurarSpinnerQuantidade(
                    quantidadeEnviadaSpinner,
                    item.getQuantidadeRecebida(),
                    item.getQuantidade(),
                    item.getQuantidadeEnviada() > 0 ? item.getQuantidadeEnviada() : item.getQuantidade()
            );
            configurarSpinnerQuantidade(
                    quantidadeRecebidaSpinner,
                    item.getQuantidadeRecebida(),
                    item.getQuantidadeEnviada(),
                    item.getQuantidadeEnviada()
            );
            fluxoItemPedidoLabel.setText(textoFluxoItem(item));
            fluxoItemPedidoLabel.setToolTipText(textoFluxoItem(item));
        } else {
            configurarSpinnerQuantidade(quantidadeEnviadaSpinner, 0, 0, 0);
            configurarSpinnerQuantidade(quantidadeRecebidaSpinner, 0, 0, 0);
            fluxoItemPedidoLabel.setText("Selecione um item.");
            fluxoItemPedidoLabel.setToolTipText(null);
        }
        boolean produtorPodeAlterar = item != null
                && item.pertenceAoProdutor(produtorLogado)
                && item.getQuantidadeEnviada() < item.getQuantidade()
                && !itemRecebidoOuCancelado(item);
        boolean adminPodeReceber = administrador
                && item != null
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();
        boolean podeAlterarItem = pedidoAtivo && item != null && (adminPodeReceber || produtorPodeAlterar);
        setCampoControleVisivel(operacaoCompletaLabel, operacaoCompletaCheckBox, podeAlterarItem);
        operacaoCompletaCheckBox.setEnabled(podeAlterarItem);
        operacaoCompletaCheckBox.setSelected(true);
        if (podeAlterarItem) {
            operacaoCompletaLabel.setText(administrador ? "Recebimento" : "Envio");
            operacaoCompletaCheckBox.setText(administrador ? "Todos recebidos" : "Todos enviados");
        } else {
            acaoItemMensagemLabel.setText(mensagemAcaoIndisponivel(item));
        }
        if (atualizarItemPedidoButton != null) {
            atualizarItemPedidoButton.setEnabled(podeAlterarItem && acaoOperacionalSelecionada(item) != null);
            atualizarItemPedidoButton.setVisible(podeAlterarItem);
            atualizarItemPedidoButton.setText(administrador ? "Confirmar recebimento" : "Registrar envio");
        }
        boolean podeInformarIndisponivel = pedidoAtivo
                && !administrador
                && item != null
                && item.pertenceAoProdutor(produtorLogado)
                && item.getQuantidadeEnviada() == 0
                && item.getQuantidadeRecebida() == 0
                && !itemRecebidoOuCancelado(item);
        if (itemIndisponivelButton != null) {
            itemIndisponivelButton.setEnabled(podeInformarIndisponivel);
            itemIndisponivelButton.setVisible(podeInformarIndisponivel);
        }
        atualizarCampoQuantidadeEnviada();
        atualizarCampoQuantidadeRecebida();
        atualizarControleAcaoItemSelecionado();
        if (pedidoStatusPanel != null) {
            pedidoStatusPanel.revalidate();
            pedidoStatusPanel.repaint();
        }
    }

    private void atualizarOpcoesStatusPedido(Pedido pedido, boolean administrador, boolean pedidoAtivo) {
        statusPedidoCombo.removeAllItems();
        if (!administrador || !pedidoAtivo || pedido == null) {
            return;
        }

        String statusAtual = pedido.getStatus();
        boolean itensRecebidos = pedidoComItensRecebidos(pedido);
        if (itensRecebidos && !"EM_SEPARACAO".equals(statusAtual) && !"PRONTO".equals(statusAtual)) {
            statusPedidoCombo.addItem("EM_SEPARACAO");
            return;
        }

        if ("EM_SEPARACAO".equals(statusAtual)) {
            statusPedidoCombo.addItem("PRONTO");
            return;
        }

        if ("PRONTO".equals(statusAtual)) {
            statusPedidoCombo.addItem("ENTREGUE");
        }
    }

    private boolean pedidoComItensRecebidos(Pedido pedido) {
        List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
        if (itens.isEmpty()) {
            return false;
        }

        for (ItemPedido item : itens) {
            if (item == null || item.getQuantidadeRecebida() < item.getQuantidade()) {
                return false;
            }
        }
        return true;
    }

    private boolean permiteEnvioParcial(ItemPedido item) {
        if (item == null) {
            return false;
        }
        int pendenteEnvio = item.getQuantidade() - item.getQuantidadeEnviada();
        return pendenteEnvio > 1;
    }

    private boolean permiteRecebimentoParcial(ItemPedido item) {
        if (item == null) {
            return false;
        }
        int pendenteRecebimento = item.getQuantidadeEnviada() - item.getQuantidadeRecebida();
        return pendenteRecebimento > 1;
    }

    private void atualizarCampoQuantidadeEnviada() {
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();
        String acao = acaoOperacionalSelecionada(item);
        boolean produtor = !usuarioAdministrador();
        boolean podeEditar = produtor
                && item != null
                && item.pertenceAoProdutor(produtorLogado)
                && item.getQuantidadeRecebida() < item.getQuantidade()
                && ACAO_ENVIAR_PARCIAL.equals(acao);

        boolean mostrarQuantidade = false;
        if (podeEditar) {
            int maximoParcial = item.getQuantidade() - item.getQuantidadeEnviada() - 1;
            boolean permiteParcial = maximoParcial >= 1;
            if (permiteParcial) {
                int valorAtual = spinnerValue(quantidadeEnviadaSpinner);
                if (valorAtual < 1 || valorAtual > maximoParcial) {
                    valorAtual = 1;
                }
                configurarSpinnerQuantidade(quantidadeEnviadaSpinner, 1, maximoParcial, valorAtual);
                mostrarQuantidade = permiteParcial;
            } else {
                configurarSpinnerQuantidade(quantidadeEnviadaSpinner, 0, 0, 0);
            }
        } else if (item != null) {
            int valor = ACAO_ENVIAR_TUDO.equals(acao) ? item.getQuantidade() : item.getQuantidadeEnviada();
            configurarSpinnerQuantidade(
                    quantidadeEnviadaSpinner,
                    item.getQuantidadeRecebida(),
                    item.getQuantidade(),
                    valor
            );
        } else {
            configurarSpinnerQuantidade(quantidadeEnviadaSpinner, 0, 0, 0);
        }

        quantidadeEnviadaSpinner.setEnabled(podeEditar && mostrarQuantidade);
        setCampoControleVisivel(quantidadeEnviadaLabel, quantidadeEnviadaSpinner, mostrarQuantidade);
    }

    private void atualizarCampoQuantidadeRecebida() {
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();
        String acao = acaoOperacionalSelecionada(item);
        boolean administrador = usuarioAdministrador();
        boolean podeReceber = administrador
                && item != null
                && ACAO_RECEBER_PARCIAL.equals(acao)
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();

        boolean mostrarQuantidade = false;
        if (podeReceber) {
            int enviada = item.getQuantidadeEnviada();
            int recebidaAtual = item.getQuantidadeRecebida();
            int maximoParcial = enviada - recebidaAtual - 1;
            boolean permiteParcial = maximoParcial >= 1;
            if (permiteParcial) {
                int valorAtual = spinnerValue(quantidadeRecebidaSpinner);
                if (valorAtual < 1 || valorAtual > maximoParcial) {
                    valorAtual = 1;
                }
                configurarSpinnerQuantidade(quantidadeRecebidaSpinner, 1, maximoParcial, valorAtual);
                mostrarQuantidade = permiteParcial;
            } else {
                configurarSpinnerQuantidade(quantidadeRecebidaSpinner, 0, 0, 0);
            }
        } else if (item != null) {
            int valor = ACAO_RECEBER_TUDO.equals(acao) ? item.getQuantidadeEnviada() : item.getQuantidadeRecebida();
            configurarSpinnerQuantidade(
                    quantidadeRecebidaSpinner,
                    item.getQuantidadeRecebida(),
                    item.getQuantidadeEnviada(),
                    valor
            );
        } else {
            configurarSpinnerQuantidade(quantidadeRecebidaSpinner, 0, 0, 0);
        }

        quantidadeRecebidaSpinner.setEnabled(podeReceber && mostrarQuantidade);
        setCampoControleVisivel(quantidadeRecebidaLabel, quantidadeRecebidaSpinner, mostrarQuantidade);
    }

    private void atualizarControleAcaoItemSelecionado() {
        Pedido pedido = (Pedido) pedidoStatusCombo.getSelectedItem();
        ItemPedido item = (ItemPedido) itemStatusCombo.getSelectedItem();
        boolean administrador = usuarioAdministrador();
        boolean pedidoAtivo = pedido != null
                && !"CANCELADO".equals(pedido.getStatus())
                && !"ENTREGUE".equals(pedido.getStatus());
        boolean produtorPodeAlterar = pedidoAtivo
                && item != null
                && item.pertenceAoProdutor(produtorLogado)
                && item.getQuantidadeEnviada() < item.getQuantidade()
                && !itemRecebidoOuCancelado(item);
        boolean adminPodeReceber = pedidoAtivo
                && administrador
                && item != null
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();
        boolean podeAlterarItem = adminPodeReceber || (!administrador && produtorPodeAlterar);
        boolean podeAplicar = podeAlterarItem && acaoOperacionalSelecionada(item) != null;

        if (!podeAlterarItem) {
            acaoItemMensagemLabel.setText(mensagemAcaoIndisponivel(item));
        } else if (administrador && operacaoCompletaCheckBox.isSelected()) {
            acaoItemMensagemLabel.setText("Vai confirmar todo o recebimento pendente deste item.");
        } else if (administrador && permiteRecebimentoParcial(item)) {
            acaoItemMensagemLabel.setText("Informe quantas unidades chegaram agora.");
        } else if (administrador) {
            acaoItemMensagemLabel.setText("Não há recebimento parcial possível; use todos recebidos.");
        } else if (operacaoCompletaCheckBox.isSelected()) {
            acaoItemMensagemLabel.setText("Vai registrar todo o envio pendente deste item.");
        } else if (permiteEnvioParcial(item)) {
            acaoItemMensagemLabel.setText("Informe quantas unidades saíram agora.");
        } else {
            acaoItemMensagemLabel.setText("Não há envio parcial possível; use todos enviados.");
        }

        if (atualizarItemPedidoButton != null) {
            atualizarItemPedidoButton.setEnabled(podeAplicar);
        }
    }

    private void setCampoControleVisivel(JLabel label, JComponent component, boolean visible) {
        if (label != null) {
            label.setVisible(visible);
        }
        if (component != null) {
            component.setVisible(visible);
        }
    }

    private String textoFluxoItem(ItemPedido item) {
        if (item == null) {
            return "Selecione um item.";
        }
        return textoFluxoItemResumo(
                item.getQuantidadeEnviada(),
                item.getQuantidadeRecebida(),
                item.getQuantidade()
        );
    }

    private String textoFluxoItemResumo(int quantidadeEnviada, int quantidadeRecebida, int quantidadeTotal) {
        return "Enviado: " + textoQuantidadeSituacao(quantidadeEnviada, quantidadeTotal)
                + " | Recebido: " + textoQuantidadeSituacao(quantidadeRecebida, quantidadeTotal);
    }

    private String textoQuantidadeSituacao(int quantidade, int total) {
        if (total <= 0 || quantidade <= 0) {
            return "não";
        }
        if (quantidade >= total) {
            return "sim";
        }
        return "parcial " + quantidade + "/" + total;
    }

    private void atualizarProdutoresFiltroCombos() {
        Integer produtoSelecionadoIdAtual = idUsuarioSelecionado(produtorProdutosCombo.getSelectedItem());
        Integer pedidoSelecionadoIdAtual = idUsuarioSelecionado(produtorPedidosCombo.getSelectedItem());
        boolean pedidosTodosSelecionado = !(produtorPedidosCombo.getSelectedItem() instanceof Usuario);

        atualizandoFiltrosProdutor = true;
        produtorProdutosModel.removeAllElements();
        produtorPedidosModel.removeAllElements();
        produtorPedidosModel.addElement(TODOS_OS_PRODUTORES);

        Usuario proximoProdutoSelecionado = null;
        Usuario proximoPedidoSelecionado = null;
        for (Usuario usuario : usuarioRepository.listarTodos()) {
            if (usuario == null || !"PRODUTOR".equals(usuario.getTipo())) {
                continue;
            }

            produtorProdutosModel.addElement(usuario);
            produtorPedidosModel.addElement(usuario);
            if (produtoSelecionadoIdAtual != null && usuario.getId() == produtoSelecionadoIdAtual) {
                proximoProdutoSelecionado = usuario;
            }
            if (pedidoSelecionadoIdAtual != null && usuario.getId() == pedidoSelecionadoIdAtual) {
                proximoPedidoSelecionado = usuario;
            }
        }

        if (proximoProdutoSelecionado == null && produtorProdutosModel.getSize() > 0) {
            proximoProdutoSelecionado = produtorProdutosModel.getElementAt(0);
        }

        produtorProdutosCombo.setSelectedItem(proximoProdutoSelecionado);
        produtorPedidosCombo.setSelectedItem(pedidosTodosSelecionado || proximoPedidoSelecionado == null
                ? TODOS_OS_PRODUTORES
                : proximoPedidoSelecionado);
        atualizandoFiltrosProdutor = false;
    }

    private Integer idUsuarioSelecionado(Object value) {
        if (value instanceof Usuario usuario) {
            return usuario.getId();
        }
        return null;
    }

    private void atualizarProdutos() {
        DefaultTableModel model = (DefaultTableModel) produtosTable.getModel();
        model.setRowCount(0);

        for (Produto produto : produtosDoContexto()) {
            if (!produtoPassaFiltros(produto)) {
                continue;
            }
            model.addRow(new Object[] {
                    produto.getId(),
                    produto.getNome(),
                    produto.getCategoria(),
                    currencyFormat.format(produto.getPreco()),
                    produto.getEstoque(),
                    produto.getEstoqueMinimo()
            });
        }
    }

    private void atualizarUsuarios() {
        DefaultTableModel model = (DefaultTableModel) usuariosTable.getModel();
        model.setRowCount(0);

        for (Usuario usuario : usuarioRepository.listarTodos()) {
            if ("CLIENTE".equals(usuario.getTipo())) {
                continue;
            }
            if (!usuarioAdministrador() && (produtorLogado == null || usuario.getId() != produtorLogado.getId())) {
                continue;
            }
            if (!usuarioPassaFiltro(usuario)) {
                continue;
            }
            model.addRow(new Object[] {
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getTipo(),
                    usuario.getLogin()
            });
        }
    }

    private void atualizarClientes() {
        DefaultTableModel model = (DefaultTableModel) clientesTable.getModel();
        model.setRowCount(0);

        for (Cliente cliente : clienteRepository.listarTodos()) {
            if (!clientePassaFiltro(cliente)) {
                continue;
            }
            model.addRow(new Object[] {
                    cliente.getId(),
                    cliente.getNome(),
                    cliente.getEmail(),
                    cliente.getTelefone()
            });
        }
    }

    private void atualizarPedidos() {
        Integer pedidoSelecionadoIdAtual = pedidoSelecionadoIdNaTabela();
        DefaultTableModel model = (DefaultTableModel) pedidosTable.getModel();
        model.setRowCount(0);

        for (Pedido pedido : pedidosDoProdutor()) {
            if (!pedidoPassaFiltros(pedido)) {
                continue;
            }
            model.addRow(new Object[] {
                    pedido.getId(),
                    formatarData(pedido.getDataPedido()),
                    formatarData(pedido.getDataPrevista()),
                    produtoresDoPedido(pedido),
                    nomeCliente(pedido.getCliente()),
                    formatarStatus(pedido.getStatus()),
                    pedido.getTipoEntrega(),
                    resumoItens(pedido),
                    resumoEnvio(pedido),
                    resumoRecebimento(pedido),
                    currencyFormat.format(totalVisivelPedido(pedido))
            });
        }

        if (model.getRowCount() == 0) {
            pedidosTable.clearSelection();
            return;
        }

        if (pedidoSelecionadoIdAtual == null || !selecionarPedido(pedidoSelecionadoIdAtual)) {
            pedidosTable.setRowSelectionInterval(0, 0);
        }
    }

    private void atualizarResumo() {
        List<Produto> produtos = usuarioAdministrador() ? produtoController.listarProdutos() : produtosDoContexto();
        List<Pedido> pedidos = pedidosDoProdutor();

        int estoqueTotal = 0;
        int estoqueBaixo = 0;
        for (Produto produto : produtos) {
            estoqueTotal += produto.getEstoque();
            if (produto.isEstoqueBaixo()) {
                estoqueBaixo++;
            }
        }

        double valorPedidos = 0;
        int pedidosEmOperacao = 0;
        int pedidosParaHoje = 0;
        int pedidosAtrasados = 0;
        LocalDate hoje = LocalDate.now();
        for (Pedido pedido : pedidos) {
            valorPedidos += totalVisivelPedido(pedido);
            if (!"ENTREGUE".equals(pedido.getStatus()) && !"CANCELADO".equals(pedido.getStatus())) {
                pedidosEmOperacao++;
                LocalDate dataPrevista = pedido.getDataPrevista();
                if (dataPrevista != null) {
                    if (dataPrevista.isBefore(hoje)) {
                        pedidosAtrasados++;
                    } else if (dataPrevista.isEqual(hoje)) {
                        pedidosParaHoje++;
                    }
                }
            }
        }

        totalProdutosValue.setText(String.valueOf(produtos.size()));
        estoqueTotalValue.setText(String.valueOf(estoqueTotal));
        estoqueBaixoValue.setText(String.valueOf(estoqueBaixo));
        totalPedidosValue.setText(String.valueOf(pedidos.size()));
        pedidosAbertosValue.setText(String.valueOf(pedidosEmOperacao));
        pedidosHojeValue.setText(String.valueOf(pedidosParaHoje));
        pedidosAtrasadosValue.setText(String.valueOf(pedidosAtrasados));
        valorPedidosValue.setText(currencyFormat.format(valorPedidos));
        divergenciasValue.setText(String.valueOf(contarDivergencias(pedidos)));
        alertasOperacionaisArea.setText(resumoAlertasOperacionais(pedidos));
        produtosAtencaoArea.setText(resumoItensPendentes(pedidos));
        pedidosRecentesArea.setText(resumoPedidosRecentes(pedidos));
    }

    private void atualizarDetalhesPedido() {
        Pedido pedido = pedidoSelecionadoNaTabela();
        if (pedido == null) {
            detalhesPedidoArea.setText("Nenhum pedido selecionado.");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Pedido #").append(pedido.getId()).append("\n");
        details.append("Data do pedido: ").append(formatarData(pedido.getDataPedido())).append("\n");
        details.append("Data prevista: ").append(dataPrevistaTexto(pedido)).append("\n");
        String alertaPrazo = alertaPrazoPedido(pedido);
        if (!alertaPrazo.isBlank()) {
            details.append("Prazo: ").append(alertaPrazo).append("\n");
        }
        details.append("Produtores: ").append(produtoresDoPedido(pedido)).append("\n");
        details.append("Cliente: ").append(nomeCliente(pedido.getCliente())).append("\n");
        String contatoCliente = contatoCliente(pedido.getCliente());
        if (!contatoCliente.isBlank()) {
            details.append("Contato: ").append(contatoCliente).append("\n");
        }
        details.append("Status: ").append(formatarStatus(pedido.getStatus())).append("\n");
        details.append("Entrega: ").append(pedido.getTipoEntrega()).append("\n\n");
        details.append("Itens\n");

        for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
            if (item == null) {
                continue;
            }
            Produto produto = item.getProduto();
            String nomeProduto = produto == null ? "Produto removido" : produto.getNome();
            details.append("- ")
                    .append(nomeProdutor(produto))
                    .append(" | ")
                    .append(nomeProduto)
                    .append(" x")
                    .append(item.getQuantidade())
                    .append(" | ")
                    .append(item.getQuantidadeEnviada())
                    .append("/")
                    .append(item.getQuantidade())
                    .append(" enviados | ")
                    .append(item.getQuantidadeRecebida())
                    .append("/")
                    .append(item.getQuantidade())
                    .append(" recebidos | ")
                    .append(formatarStatus(item.getStatus()))
                    .append(" | ")
                    .append(acaoOperacionalItem(item))
                    .append(" | subtotal ")
                    .append(currencyFormat.format(item.getSubtotal()))
                    .append("\n");
        }

        details.append("\nRecebimento: ").append(resumoRecebimento(pedido));
        details.append("\nTotal: ").append(currencyFormat.format(totalVisivelPedido(pedido)));
        if (!pedido.getHistorico().isEmpty()) {
            details.append("\n\nHistórico\n");
            for (String registro : pedido.getHistorico()) {
                details.append("- ").append(registro).append("\n");
            }
        }
        detalhesPedidoArea.setText(details.toString());
    }

    private void atualizarRelatorioOperacional() {
        if (relatorioOperacionalArea == null) {
            return;
        }

        List<Pedido> pedidos = pedidosFiltradosParaRelatorio();
        String tipo = (String) relatorioOperacionalCombo.getSelectedItem();
        String texto = switch (tipo == null ? "" : tipo) {
            case "Conferência na cooperativa" -> relatorioConferenciaCooperativa(pedidos);
            case "Separação por cliente" -> relatorioSeparacaoCliente(pedidos);
            default -> relatorioListaProdutores(pedidos);
        };

        relatorioOperacionalArea.setText(texto);
        relatorioOperacionalArea.setCaretPosition(0);
        copiarRelatorioButton.setEnabled(texto != null && !texto.isBlank());
    }

    private List<Pedido> pedidosFiltradosParaRelatorio() {
        List<Pedido> pedidos = new ArrayList<>();
        for (Pedido pedido : pedidosDoProdutor()) {
            if (pedidoPassaFiltros(pedido)) {
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    private String relatorioListaProdutores(List<Pedido> pedidos) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("LISTA DO PRODUTOR\n")
                .append(descricaoFiltrosPedidos())
                .append("\n\n");

        int totalItens = 0;
        for (String produtor : produtoresNoRelatorio(pedidos)) {
            StringBuilder bloco = new StringBuilder();
            for (Pedido pedido : pedidos) {
                if (!pedidoAtivo(pedido)) {
                    continue;
                }
                for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                    if (!itemAguardandoEnvioDoProdutor(item) || !produtor.equals(nomeProdutor(item.getProduto()))) {
                        continue;
                    }

                    Produto produto = item.getProduto();
                    int pendenteEnvio = Math.max(0, item.getQuantidade() - item.getQuantidadeEnviada());
                    bloco.append("- Pedido #")
                            .append(pedido.getId())
                            .append(" | ")
                            .append(dataPrevistaTexto(pedido))
                            .append(" | ")
                            .append(nomeCliente(pedido.getCliente()))
                            .append("\n  ")
                            .append(nomeProduto(item))
                            .append(" | enviar ")
                            .append(pendenteEnvio)
                            .append(" de ")
                            .append(item.getQuantidade())
                            .append(" | j\u00E1 enviados ")
                            .append(item.getQuantidadeEnviada())
                            .append(" | recebidos ")
                            .append(item.getQuantidadeRecebida());
                    if (produto != null) {
                        bloco.append(" | ")
                                .append(currencyFormat.format(produto.getPreco()))
                                .append("/un");
                    }
                    bloco.append("\n");
                    totalItens++;
                }
            }

            if (bloco.length() > 0) {
                relatorio.append(produtor).append("\n")
                        .append(bloco)
                        .append("\n");
            }
        }

        if (totalItens == 0) {
            relatorio.append("Nenhum item aguardando envio nos filtros atuais.");
        }
        return relatorio.toString();
    }

    private String relatorioConferenciaCooperativa(List<Pedido> pedidos) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("CONFERÊNCIA NA COOPERATIVA\n")
                .append(descricaoFiltrosPedidos())
                .append("\n\n");

        int totalItens = 0;
        for (String produtor : produtoresNoRelatorio(pedidos)) {
            StringBuilder bloco = new StringBuilder();
            for (Pedido pedido : pedidos) {
                if (!pedidoAtivo(pedido)) {
                    continue;
                }
                for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                    if (!itemAguardandoConferencia(item) || !produtor.equals(nomeProdutor(item.getProduto()))) {
                        continue;
                    }

                    int pendenteReceber = Math.max(0, item.getQuantidadeEnviada() - item.getQuantidadeRecebida());
                    boolean divergencia = itemTemDivergenciaRecebimento(item);
                    bloco.append("- Pedido #")
                            .append(pedido.getId())
                            .append(" | ")
                            .append(dataPrevistaTexto(pedido))
                            .append(" | ")
                            .append(nomeCliente(pedido.getCliente()))
                            .append("\n  ")
                            .append(nomeProduto(item))
                            .append(" | conferir ")
                            .append(pendenteReceber)
                            .append(" | enviados ")
                            .append(item.getQuantidadeEnviada())
                            .append("/")
                            .append(item.getQuantidade())
                            .append(" | recebidos ")
                            .append(item.getQuantidadeRecebida())
                            .append("/")
                            .append(item.getQuantidade());
                    if (divergencia) {
                        bloco.append(" | investigar diferen\u00E7a de ")
                                .append(pendenteReceber);
                    } else {
                        bloco.append(" | conferir chegada");
                    }
                    bloco.append("\n");
                    totalItens++;
                }
            }

            if (bloco.length() > 0) {
                relatorio.append(produtor).append("\n")
                        .append(bloco)
                        .append("\n");
            }
        }

        if (totalItens == 0) {
            relatorio.append("Nenhum item aguardando confer\u00EAncia nos filtros atuais.");
        }
        return relatorio.toString();
    }

    private String relatorioSeparacaoCliente(List<Pedido> pedidos) {
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("SEPARAÇÃO POR CLIENTE\n")
                .append(descricaoFiltrosPedidos())
                .append("\n\n");

        int totalPedidos = 0;
        for (Pedido pedido : pedidos) {
            if (!pedidoAtivo(pedido)) {
                continue;
            }

            List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
            if (itens.isEmpty()) {
                continue;
            }

            boolean pronto = true;
            for (ItemPedido item : itens) {
                if (item == null || !itemProntoParaSeparacao(item)) {
                    pronto = false;
                    break;
                }
            }

            relatorio.append(pronto ? "PRONTO PARA SEPARAR" : "AGUARDAR ITENS")
                    .append("\nPedido #")
                    .append(pedido.getId())
                    .append(" | ")
                    .append(nomeCliente(pedido.getCliente()))
                    .append(" | ")
                    .append(pedido.getTipoEntrega())
                    .append(" | ")
                    .append(dataPrevistaTexto(pedido))
                    .append("\n");

            String contato = contatoCliente(pedido.getCliente());
            if (!contato.isBlank()) {
                relatorio.append("Contato: ").append(contato).append("\n");
            }

            for (ItemPedido item : itens) {
                relatorio.append("- ")
                        .append(nomeProduto(item))
                        .append(" | ")
                        .append(nomeProdutor(item == null ? null : item.getProduto()))
                        .append(" | ");
                if (item == null) {
                    relatorio.append("item indispon\u00EDvel para confer\u00EAncia");
                } else if ("INDISPONIVEL".equals(item.getStatus())) {
                    relatorio.append("indispon\u00EDvel; resolver pend\u00EAncia com produtor/cliente");
                } else if ("CANCELADO".equals(item.getStatus())) {
                    relatorio.append("cancelado");
                } else if (itemProntoParaSeparacao(item)) {
                    relatorio.append("separar ")
                            .append(item.getQuantidade());
                } else {
                    int faltam = Math.max(0, item.getQuantidade() - item.getQuantidadeRecebida());
                    relatorio.append("aguardar ")
                            .append(faltam)
                            .append(" | recebidos ")
                            .append(item.getQuantidadeRecebida())
                            .append("/")
                            .append(item.getQuantidade());
                }
                relatorio.append("\n");
            }
            relatorio.append("\n");
            totalPedidos++;
        }

        if (totalPedidos == 0) {
            relatorio.append("Nenhum pedido em andamento nos filtros atuais.");
        }
        return relatorio.toString();
    }

    private void copiarRelatorioOperacional() {
        String texto = relatorioOperacionalArea == null ? "" : relatorioOperacionalArea.getText();
        if (texto.isBlank()) {
            showError("N\u00E3o h\u00E1 lista para copiar.");
            return;
        }

        try {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(texto), null);
            showInfo("Lista copiada.");
        } catch (IllegalStateException | java.awt.HeadlessException exception) {
            showError("Não foi possível acessar a área de transferência agora.");
        }
    }

    private String descricaoFiltrosPedidos() {
        List<String> filtros = new ArrayList<>();
        Object status = statusPedidoFiltroCombo.getSelectedItem();
        if (status instanceof String statusTexto && !"Todos os status".equals(statusTexto)) {
            filtros.add("status " + formatarStatus(statusTexto).toLowerCase(Locale.ROOT));
        }
        Usuario produtor = produtorPedidosSelecionado();
        if (produtor != null) {
            filtros.add("produtor " + produtor.getNome());
        }
        String busca = pedidoBuscaField.getText().trim();
        if (!busca.isBlank()) {
            filtros.add("busca \"" + busca + "\"");
        }
        if (pedidosSituacaoTabs != null && pedidosSituacaoTabs.getSelectedIndex() >= 0) {
            filtros.add("aba " + pedidosSituacaoTabs.getTitleAt(pedidosSituacaoTabs.getSelectedIndex()).toLowerCase(Locale.ROOT));
        }
        return filtros.isEmpty() ? "Filtros: todos os pedidos vis\u00EDveis" : "Filtros: " + String.join(", ", filtros);
    }

    private List<String> produtoresNoRelatorio(List<Pedido> pedidos) {
        List<String> produtores = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                String produtor = nomeProdutor(item == null ? null : item.getProduto());
                if (!produtores.contains(produtor)) {
                    produtores.add(produtor);
                }
            }
        }
        return produtores;
    }

    private boolean itemAguardandoEnvioDoProdutor(ItemPedido item) {
        return item != null
                && !itemRecebidoOuCancelado(item)
                && item.getQuantidadeEnviada() < item.getQuantidade();
    }

    private boolean itemAguardandoConferencia(ItemPedido item) {
        return item != null
                && !itemRecebidoOuCancelado(item)
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();
    }

    private boolean itemProntoParaSeparacao(ItemPedido item) {
        return item != null
                && !"CANCELADO".equals(item.getStatus())
                && !"INDISPONIVEL".equals(item.getStatus())
                && item.getQuantidadeRecebida() >= item.getQuantidade();
    }

    private String nomeProduto(ItemPedido item) {
        Produto produto = item == null ? null : item.getProduto();
        return produto == null ? "Produto removido" : produto.getNome();
    }

    private boolean produtoPassaFiltros(Produto produto) {
        if (produto == null) {
            return false;
        }

        if (estoqueBaixoCheckBox.isSelected() && !produto.isEstoqueBaixo()) {
            return false;
        }

        String filtro = textoFiltro(produtoBuscaField);
        if (filtro.isBlank()) {
            return true;
        }

        return contemFiltro(produto.getNome(), filtro)
                || contemFiltro(produto.getCategoria(), filtro)
                || contemFiltro(nomeProdutor(produto), filtro);
    }

    private boolean usuarioPassaFiltro(Usuario usuario) {
        String filtro = textoFiltro(usuarioBuscaField);
        if (filtro.isBlank()) {
            return true;
        }

        return usuario != null
                && (contemFiltro(usuario.getNome(), filtro)
                || contemFiltro(usuario.getTipo(), filtro)
                || contemFiltro(usuario.getLogin(), filtro));
    }

    private boolean clientePassaFiltro(Cliente cliente) {
        String filtro = textoFiltro(clienteBuscaField);
        if (filtro.isBlank()) {
            return true;
        }

        return cliente != null
                && (contemFiltro(cliente.getNome(), filtro)
                || contemFiltro(cliente.getEmail(), filtro)
                || contemFiltro(cliente.getTelefone(), filtro));
    }

    private boolean pedidoPassaFiltros(Pedido pedido) {
        if (pedido == null) {
            return false;
        }

        if (!pedidoPassaSituacaoSelecionada(pedido)) {
            return false;
        }

        String statusFiltro = (String) statusPedidoFiltroCombo.getSelectedItem();
        if (statusFiltro != null
                && !"Todos os status".equals(statusFiltro)
                && !pedidoPassaFiltroOperacional(pedido, statusFiltro)) {
            return false;
        }

        String filtro = textoFiltro(pedidoBuscaField);
        if (filtro.isBlank()) {
            return true;
        }

        return contemFiltro(String.valueOf(pedido.getId()), filtro)
                || contemFiltro(formatarData(pedido.getDataPedido()), filtro)
                || contemFiltro(dataPrevistaTexto(pedido), filtro)
                || contemFiltro(produtoresDoPedido(pedido), filtro)
                || contemFiltro(nomeCliente(pedido.getCliente()), filtro)
                || contemFiltro(formatarStatus(pedido.getStatus()), filtro)
                || contemFiltro(pedido.getTipoEntrega(), filtro)
                || contemFiltro(resumoItens(pedido), filtro);
    }

    private boolean pedidoPassaFiltroOperacional(Pedido pedido, String filtro) {
        return switch (filtro) {
            case FILTRO_COM_DIVERGENCIA -> pedidoTemDivergencia(pedido);
            case FILTRO_AGUARDANDO_PRODUTOR -> pedidoAguardandoProdutor(pedido);
            case FILTRO_EM_TRANSITO -> pedidoTemItemEmTransito(pedido);
            case FILTRO_PREVISTO_HOJE -> pedidoPrevistoParaHoje(pedido);
            case FILTRO_ATRASADO -> pedidoAtrasado(pedido);
            case FILTRO_PRONTO_SEPARACAO -> pedidoProntoParaSeparacao(pedido);
            default -> filtro.equals(pedido.getStatus());
        };
    }

    private boolean pedidoPassaSituacaoSelecionada(Pedido pedido) {
        if (pedidosSituacaoTabs == null || pedido == null) {
            return true;
        }

        String status = pedido.getStatus();
        int selectedIndex = pedidosSituacaoTabs.getSelectedIndex();
        if (selectedIndex == 1) {
            return "ENTREGUE".equals(status);
        }
        if (selectedIndex == 2) {
            return "CANCELADO".equals(status);
        }
        return !"ENTREGUE".equals(status) && !"CANCELADO".equals(status);
    }

    private String textoFiltro(JTextField field) {
        return normalizarTexto(field == null ? "" : field.getText());
    }

    private boolean contemFiltro(String text, String filtro) {
        return normalizarTexto(text).contains(filtro);
    }

    private String normalizarTexto(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase(Locale.ROOT);
    }

    private void carregarProdutoSelecionado() {
        int selectedRow = produtosTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        Object value = produtosTable.getValueAt(selectedRow, 0);
        if (!(value instanceof Integer)) {
            return;
        }

        Produto produto = produtoController.buscarProduto((Integer) value);
        if (!produtoPertenceAoProdutor(produto)) {
            limparFormularioProduto();
            return;
        }

        produtoSelecionadoId = produto.getId();
        produtoFormStateLabel.setText("Editando produto #" + produtoSelecionadoId);
        salvarProdutoButton.setText("Salvar");
        excluirProdutoButton.setEnabled(true);
        nomeProdutoField.setText(produto.getNome());
        categoriaProdutoCombo.setSelectedItem(produto.getCategoria());
        precoProdutoField.setText(currencyFormat.format(produto.getPreco()));
        estoqueProdutoSpinner.setValue(produto.getEstoque());
        estoqueMinimoProdutoSpinner.setValue(produto.getEstoqueMinimo());
    }

    private void carregarUsuarioNoFormulario(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        usuarioSelecionadoId = usuario.getId();
        usuarioFormStateLabel.setText(usuarioAdministrador() ? "Editando usuário #" + usuarioSelecionadoId : "Meu cadastro");
        salvarUsuarioButton.setText("Salvar");
        nomeUsuarioField.setText(usuario.getNome());
        tipoUsuarioCombo.setSelectedItem(usuario.getTipo());
        loginUsuarioField.setText(usuario.getLogin());
        senhaUsuarioField.setText("");
        confirmarSenhaUsuarioField.setText("");
        aplicarPermissoesUsuario();
    }

    private void carregarUsuarioSelecionado() {
        int selectedRow = usuariosTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        Object value = usuariosTable.getValueAt(selectedRow, 0);
        if (!(value instanceof Integer)) {
            return;
        }

        Usuario usuario = usuarioRepository.buscarPorId((Integer) value);
        if (usuario == null) {
            limparFormularioUsuario();
            return;
        }
        if (!usuarioAdministrador() && (produtorLogado == null || usuario.getId() != produtorLogado.getId())) {
            limparFormularioUsuario();
            return;
        }

        carregarUsuarioNoFormulario(usuario);
    }

    private void carregarClienteSelecionado() {
        int selectedRow = clientesTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        Object value = clientesTable.getValueAt(selectedRow, 0);
        if (!(value instanceof Integer)) {
            return;
        }

        Cliente cliente = clienteRepository.buscarPorId((Integer) value);
        if (cliente == null) {
            limparFormularioCliente();
            return;
        }

        clienteSelecionadoId = cliente.getId();
        clienteFormStateLabel.setText("Editando cliente #" + clienteSelecionadoId);
        salvarClienteButton.setText("Salvar");
        excluirClienteButton.setEnabled(true);
        nomeClienteField.setText(cliente.getNome());
        emailClienteField.setText(cliente.getEmail());
        telefoneClienteField.setText(cliente.getTelefone());
    }

    private void editarProdutoSelecionado() {
        Integer produtoId = idSelecionadoNaTabela(produtosTable);
        if (produtoId == null) {
            return;
        }

        Produto produto = produtoController.buscarProduto(produtoId);
        if (produto == null) {
            showError("Produto não encontrado.");
            return;
        }

        if (usuarioAdministrador()) {
            showInfo("Administradores visualizam produtos. A alteração do cadastro deve ser feita pelo produtor responsável.");
            return;
        }

        if (produto.getProdutor() == null || produtorLogado == null || produto.getProdutor().getId() != produtorLogado.getId()) {
            showError("Você só pode alterar produtos do seu cadastro.");
            return;
        }

        JTextField nomeField = new JTextField(produto.getNome());
        JComboBox<String> categoriaCombo = new JComboBox<>(CATEGORIAS);
        categoriaCombo.setSelectedItem(produto.getCategoria());
        JTextField precoField = new JTextField(currencyFormat.format(produto.getPreco()));
        JSpinner estoqueSpinner = new JSpinner(new SpinnerNumberModel(produto.getEstoque(), 0, 9999, 1));
        JSpinner estoqueMinimoSpinner = new JSpinner(new SpinnerNumberModel(produto.getEstoqueMinimo(), 0, 9999, 1));

        JPanel form = formPanel();
        addField(form, 0, "Nome", nomeField);
        addField(form, 1, "Categoria", categoriaCombo);
        addField(form, 2, "Preço", precoField);
        addField(form, 3, "Estoque", estoqueSpinner);
        addField(form, 4, "Estoque mínimo", estoqueMinimoSpinner);

        aplicarTemaEm(form);
        exibirDialogoEdicao(
                "Editar produto",
                form,
                () -> salvarEdicaoProduto(produto, nomeField, categoriaCombo, precoField, estoqueSpinner, estoqueMinimoSpinner),
                () -> excluirProdutoEditado(produto)
        );
    }

    private boolean salvarEdicaoProduto(
            Produto produto,
            JTextField nomeField,
            JComboBox<String> categoriaCombo,
            JTextField precoField,
            JSpinner estoqueSpinner,
            JSpinner estoqueMinimoSpinner
    ) {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            showError("Informe o nome do produto.");
            return false;
        }

        double preco;
        try {
            preco = parseMoney(precoField.getText());
        } catch (NumberFormatException exception) {
            showError("Informe um preço válido.");
            return false;
        }

        if (preco <= 0) {
            showError("O preço precisa ser maior que zero.");
            return false;
        }

        boolean sucesso = produtoController.atualizarProduto(
                produto.getId(),
                nome,
                (String) categoriaCombo.getSelectedItem(),
                preco,
                (Integer) estoqueSpinner.getValue(),
                (Integer) estoqueMinimoSpinner.getValue(),
                produtorLogado
        );

        if (!sucesso) {
            showError("Não foi possível atualizar o produto selecionado.");
            return false;
        }

        atualizarTela();
        selecionarProdutoNaTabela(produto.getId());
        showInfo("Produto atualizado com sucesso.");
        return true;
    }

    private boolean excluirProdutoEditado(Produto produto) {
        if (produtoEmPedido(produto.getId())) {
            showError("Este produto já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return false;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o produto \"" + produto.getNome() + "\" do catálogo?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return false;
        }

        if (!produtoController.removerProduto(produto.getId(), produtorLogado)) {
            showError("Não foi possível excluir o produto.");
            return false;
        }

        atualizarTela();
        showInfo("Produto excluído com sucesso.");
        return true;
    }

    private void editarUsuarioSelecionado() {
        Integer usuarioId = idSelecionadoNaTabela(usuariosTable);
        if (usuarioId == null) {
            return;
        }

        Usuario usuario = usuarioRepository.buscarPorId(usuarioId);
        if (usuario == null) {
            showError("Usuário não encontrado.");
            return;
        }

        JTextField nomeField = new JTextField(usuario.getNome());
        JComboBox<String> tipoCombo = new JComboBox<>(TIPOS_USUARIO);
        tipoCombo.setSelectedItem(usuario.getTipo());
        JTextField loginField = new JTextField(usuario.getLogin());
        JPasswordField senhaField = new JPasswordField();
        JPasswordField confirmarSenhaField = new JPasswordField();

        JPanel form = formPanel();
        addField(form, 0, "Nome", nomeField);
        addField(form, 1, "Tipo", tipoCombo);
        addField(form, 2, "Login", loginField);
        addField(form, 3, "Nova senha", senhaField);
        addField(form, 4, "Confirmar", confirmarSenhaField);

        aplicarTemaEm(form);
        exibirDialogoEdicao(
                "Editar usuário",
                form,
                () -> salvarEdicaoUsuario(usuario, nomeField, tipoCombo, loginField, senhaField, confirmarSenhaField),
                () -> excluirUsuarioEditado(usuario)
        );
    }

    private boolean salvarEdicaoUsuario(
            Usuario usuario,
            JTextField nomeField,
            JComboBox<String> tipoCombo,
            JTextField loginField,
            JPasswordField senhaField,
            JPasswordField confirmarSenhaField
    ) {
        String nome = nomeField.getText().trim();
        String tipo = (String) tipoCombo.getSelectedItem();
        String login = loginField.getText().trim();
        String senha = new String(senhaField.getPassword());
        String confirmarSenha = new String(confirmarSenhaField.getPassword());

        if (nome.isEmpty()) {
            showError("Informe o nome do usuário.");
            return false;
        }

        if (login.isEmpty()) {
            showError("Informe o login do usuário.");
            return false;
        }

        if (produtorLogado != null && produtorLogado.getId() == usuario.getId() && !tipo.equals(usuario.getTipo())) {
            showError("O usuário logado não pode alterar o próprio tipo.");
            return false;
        }

        if (senha.isBlank() && !confirmarSenha.isBlank()) {
            showError("Informe a senha antes de confirmar.");
            return false;
        }

        if (!senha.isBlank() && !senha.equals(confirmarSenha)) {
            showError("A confirmação de senha não confere.");
            return false;
        }

        try {
            boolean sucesso = usuarioRepository.atualizar(usuario.getId(), nome, tipo, login, senha);
            if (!sucesso) {
                showError("Usuário não encontrado.");
                return false;
            }

            if (produtorLogado != null && produtorLogado.getId() == usuario.getId()) {
                produtorLogado = usuarioRepository.buscarPorId(usuario.getId());
                atualizarProdutorLogado();
            }

            atualizarTela();
            selecionarUsuarioNaTabela(usuario.getId());
            showInfo("Usuário atualizado com sucesso.");
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
            return false;
        }
    }

    private boolean excluirUsuarioEditado(Usuario usuario) {
        if (!usuarioAdministrador()) {
            showError("Apenas administradores podem excluir usuários.");
            return false;
        }

        if (produtorLogado != null && usuario.getId() == produtorLogado.getId()) {
            showError("Você não pode excluir o próprio usuário logado.");
            return false;
        }

        if ("ADMIN".equals(usuario.getTipo()) && quantidadeAdministradores() <= 1) {
            showError("Não é possível excluir o último administrador.");
            return false;
        }

        if ("PRODUTOR".equals(usuario.getTipo()) && produtorEmPedido(usuario.getId())) {
            showError("Este produtor já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return false;
        }

        if ("PRODUTOR".equals(usuario.getTipo()) && produtorTemProdutos(usuario.getId())) {
            showError("Este produtor ainda possui produtos cadastrados. Remova esses produtos antes de excluir o produtor.");
            return false;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o usuário \"" + usuario.getNome() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return false;
        }

        if (!usuarioRepository.remover(usuario.getId())) {
            showError("Não foi possível excluir o usuário.");
            return false;
        }

        atualizarTela();
        showInfo("Usuário excluído com sucesso.");
        return true;
    }

    private void editarClienteSelecionado() {
        Integer clienteId = idSelecionadoNaTabela(clientesTable);
        if (clienteId == null) {
            return;
        }

        Cliente cliente = clienteRepository.buscarPorId(clienteId);
        if (cliente == null) {
            showError("Cliente não encontrado.");
            return;
        }

        JTextField nomeField = new JTextField(cliente.getNome());
        JTextField emailField = new JTextField(cliente.getEmail());
        JTextField telefoneField = new JTextField(cliente.getTelefone());

        JPanel form = formPanel();
        addField(form, 0, "Nome", nomeField);
        addField(form, 1, "E-mail", emailField);
        addField(form, 2, "Telefone", telefoneField);

        aplicarTemaEm(form);
        exibirDialogoEdicao(
                "Editar cliente",
                form,
                () -> salvarEdicaoCliente(cliente, nomeField, emailField, telefoneField),
                () -> excluirClienteEditado(cliente)
        );
    }

    private boolean salvarEdicaoCliente(Cliente cliente, JTextField nomeField, JTextField emailField, JTextField telefoneField) {
        String nome = nomeField.getText().trim();
        String email = emailField.getText().trim();
        String telefone = telefoneField.getText().trim();

        if (nome.isEmpty()) {
            showError("Informe o nome do cliente.");
            return false;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            showError("Informe um e-mail válido ou deixe o campo em branco.");
            return false;
        }

        boolean sucesso = clienteRepository.atualizar(cliente.getId(), nome, email, telefone);
        if (!sucesso) {
            showError("Cliente não encontrado.");
            return false;
        }

        atualizarTela();
        selecionarClienteNaTabela(cliente.getId());
        showInfo("Cliente atualizado com sucesso.");
        return true;
    }

    private boolean excluirClienteEditado(Cliente cliente) {
        if (clienteEmPedido(cliente.getId())) {
            showError("Este cliente já aparece em pedidos. Mantenha-o para preservar o histórico operacional.");
            return false;
        }

        int option = JOptionPane.showConfirmDialog(
                dialogOwner(),
                "Excluir o cliente \"" + cliente.getNome() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return false;
        }

        if (!clienteRepository.remover(cliente.getId())) {
            showError("Não foi possível excluir o cliente.");
            return false;
        }

        atualizarTela();
        showInfo("Cliente excluído com sucesso.");
        return true;
    }

    private void exibirDialogoEdicao(String title, JPanel form, BooleanSupplier salvarAction, BooleanSupplier excluirAction) {
        javax.swing.JDialog dialog = new javax.swing.JDialog(this, title, true);

        JPanel content = basePanel(new BorderLayout(14, 14));
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        content.add(form, BorderLayout.CENTER);

        JButton cancelarButton = secondaryButton("Cancelar");
        JButton excluirButton = secondaryButton("Excluir");
        JButton salvarButton = primaryButton("Salvar");
        cancelarButton.addActionListener(event -> dialog.dispose());
        excluirButton.addActionListener(event -> {
            if (excluirAction.getAsBoolean()) {
                dialog.dispose();
            }
        });
        salvarButton.addActionListener(event -> {
            if (salvarAction.getAsBoolean()) {
                dialog.dispose();
            }
        });

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.setOpaque(false);
        buttons.add(excluirButton);
        buttons.add(cancelarButton);
        buttons.add(salvarButton);
        content.add(buttons, BorderLayout.SOUTH);

        aplicarTemaEm(content);
        dialog.setContentPane(content);
        dialog.setIconImages(getIconImages());
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(dialogOwner());
        dialog.setVisible(true);
    }

    private Integer idSelecionadoNaTabela(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        Object value = table.getModel().getValueAt(modelRow, 0);
        return value instanceof Integer ? (Integer) value : null;
    }

    private void selecionarProdutoNaTabela(int produtoId) {
        selecionarLinhaPorId(produtosTable, produtoId);
    }

    private void selecionarUsuarioNaTabela(int usuarioId) {
        selecionarLinhaPorId(usuariosTable, usuarioId);
    }

    private void selecionarClienteNaTabela(int clienteId) {
        selecionarLinhaPorId(clientesTable, clienteId);
    }

    private void selecionarLinhaPorId(JTable table, int id) {
        for (int row = 0; row < table.getRowCount(); row++) {
            Object value = table.getValueAt(row, 0);
            if (value instanceof Integer && (Integer) value == id) {
                table.setRowSelectionInterval(row, row);
                table.scrollRectToVisible(table.getCellRect(row, 0, true));
                return;
            }
        }
    }

    private int contarDivergencias(List<Pedido> pedidos) {
        int total = 0;
        for (Pedido pedido : pedidos) {
            if (!pedidoAtivo(pedido)) {
                continue;
            }
            for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                if (itemTemDivergenciaRecebimento(item)) {
                    total++;
                }
            }
        }
        return total;
    }

    private String resumoAlertasOperacionais(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return "Nenhum pedido cadastrado.";
        }

        StringBuilder resumo = new StringBuilder();
        int totalAlertas = 0;
        int limite = 8;
        for (Pedido pedido : pedidos) {
            if (!pedidoAtivo(pedido)) {
                continue;
            }

            boolean atrasado = pedidoAtrasado(pedido);
            for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                if (item == null || item.getProduto() == null || itemRecebidoOuCancelado(item)) {
                    continue;
                }

                String alerta = alertaOperacionalItem(item, atrasado);
                if (alerta.isBlank()) {
                    continue;
                }

                totalAlertas++;
                if (totalAlertas > limite) {
                    continue;
                }

                if (resumo.length() > 0) {
                    resumo.append("\n");
                }
                Produto produto = item.getProduto();
                resumo.append(alerta)
                        .append("\nPedido #")
                        .append(pedido.getId())
                        .append(" | ")
                        .append(dataPrevistaTexto(pedido))
                        .append(" | ")
                        .append(nomeCliente(pedido.getCliente()))
                        .append("\n")
                        .append(nomeProdutor(produto))
                        .append(" | ")
                        .append(produto.getNome())
                        .append(" | ")
                        .append(textoFluxoItem(item))
                        .append("\n");
            }
        }

        if (totalAlertas == 0) {
            return "Nenhum alerta operacional no momento.";
        }
        if (totalAlertas > limite) {
            resumo.append("\n+ ")
                    .append(totalAlertas - limite)
                    .append(" alerta(s) nos filtros de pedidos.");
        }
        return resumo.toString();
    }

    private String alertaOperacionalItem(ItemPedido item, boolean pedidoAtrasado) {
        if (itemTemDivergenciaRecebimento(item)) {
            return "Investigar diferença entre enviado e recebido";
        }
        if (pedidoAtrasado) {
            return "Prazo previsto atrasado";
        }
        if (itemEmTransito(item)) {
            return "Conferir chegada na cooperativa";
        }
        if (itemAguardandoProdutor(item)) {
            return "Aguardar envio do produtor";
        }
        return "";
    }

    private boolean pedidoAtivo(Pedido pedido) {
        if (pedido == null) {
            return false;
        }
        String status = pedido.getStatus();
        return !"ENTREGUE".equals(status) && !"CANCELADO".equals(status);
    }

    private boolean pedidoAtrasado(Pedido pedido) {
        return pedidoAtivo(pedido)
                && pedido.getDataPrevista() != null
                && pedido.getDataPrevista().isBefore(LocalDate.now());
    }

    private boolean pedidoPrevistoParaHoje(Pedido pedido) {
        return pedidoAtivo(pedido)
                && pedido.getDataPrevista() != null
                && pedido.getDataPrevista().isEqual(LocalDate.now());
    }

    private boolean pedidoProntoParaSeparacao(Pedido pedido) {
        return pedidoAtivo(pedido) && pedidoComItensRecebidos(pedido);
    }

    private boolean pedidoTemDivergencia(Pedido pedido) {
        if (!pedidoAtivo(pedido)) {
            return false;
        }
        for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
            if (itemTemDivergenciaRecebimento(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean pedidoAguardandoProdutor(Pedido pedido) {
        if (!pedidoAtivo(pedido)) {
            return false;
        }
        for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
            if (itemAguardandoProdutor(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean pedidoTemItemEmTransito(Pedido pedido) {
        if (!pedidoAtivo(pedido)) {
            return false;
        }
        for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
            if (itemEmTransito(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean itemTemDivergenciaRecebimento(ItemPedido item) {
        return item != null
                && !itemRecebidoOuCancelado(item)
                && item.getQuantidadeRecebida() > 0
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();
    }

    private boolean itemEmTransito(ItemPedido item) {
        return item != null
                && !itemRecebidoOuCancelado(item)
                && !itemTemDivergenciaRecebimento(item)
                && item.getQuantidadeEnviada() > item.getQuantidadeRecebida();
    }

    private boolean itemAguardandoProdutor(ItemPedido item) {
        return item != null
                && !itemRecebidoOuCancelado(item)
                && item.getQuantidadeEnviada() <= item.getQuantidadeRecebida();
    }

    private String acaoOperacionalItem(ItemPedido item) {
        if (item == null) {
            return "";
        }
        String status = item.getStatus();
        if ("CANCELADO".equals(status)) {
            return "sem ação";
        }
        if ("INDISPONIVEL".equals(status)) {
            return "indisponível";
        }
        if (item.getQuantidadeRecebida() >= item.getQuantidade()) {
            return "conferido";
        }
        if (itemTemDivergenciaRecebimento(item)) {
            return "investigar diferença";
        }
        if (itemEmTransito(item)) {
            return "conferir recebimento";
        }
        if (item.getQuantidadeEnviada() == 0) {
            return "aguardar envio do produtor";
        }
        return "aguardar complemento do produtor";
    }

    private String resumoItensPendentes(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return "Nenhum pedido cadastrado.";
        }

        StringBuilder resumo = new StringBuilder();
        int total = 0;
        for (Pedido pedido : pedidos) {
            if (pedido == null || "CANCELADO".equals(pedido.getStatus()) || "ENTREGUE".equals(pedido.getStatus())) {
                continue;
            }

            for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
                if (item == null || item.getProduto() == null || itemRecebidoOuCancelado(item)) {
                    continue;
                }

                if (total > 0) {
                    resumo.append("\n");
                }
                Produto produto = item.getProduto();
                resumo.append("Pedido #")
                        .append(pedido.getId())
                        .append(" | ")
                        .append(dataPrevistaTexto(pedido))
                        .append("\n")
                        .append(nomeCliente(pedido.getCliente()))
                        .append(" | ")
                        .append(nomeProdutor(produto))
                        .append("\n")
                        .append("- ")
                        .append(produto.getNome())
                        .append(" x")
                        .append(item.getQuantidade())
                        .append(" | ")
                        .append(item.getQuantidadeEnviada())
                        .append(" enviados, ")
                        .append(item.getQuantidadeRecebida())
                        .append(" recebidos, faltam ")
                        .append(item.getQuantidadePendenteRecebimento())
                        .append(" | ")
                        .append(formatarStatus(item.getStatus()))
                        .append("\n");
                total++;
            }
        }

        if (total == 0) {
            return "Nenhum item pendente de recebimento.";
        }
        return resumo.toString();
    }

    private boolean itemRecebidoOuCancelado(ItemPedido item) {
        String status = item == null ? "" : item.getStatus();
        return "CANCELADO".equals(status)
                || "INDISPONIVEL".equals(status)
                || "ENTREGUE_COOPERATIVA".equals(status)
                || (item != null && item.getQuantidadeRecebida() >= item.getQuantidade());
    }

    private String resumoProdutosEmAtencao(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            return "Nenhum produto cadastrado.";
        }

        StringBuilder resumo = new StringBuilder();
        int baixos = 0;
        for (Produto produto : produtos) {
            if (produto.isEstoqueBaixo()) {
                resumo.append("- ")
                        .append(produto.getNome())
                        .append(": ")
                        .append(produto.getEstoque())
                        .append(" em estoque, mínimo ")
                        .append(produto.getEstoqueMinimo())
                        .append("\n");
                baixos++;
            }
        }
        if (baixos == 0) {
            resumo.append("Nenhum produto com estoque baixo.");
        }

        return resumo.toString();
    }

    private String resumoProdutosDoProdutor(List<Produto> produtos) {
        Usuario produtor = produtorProdutosSelecionado();
        if (produtor == null) {
            return "Selecione um produtor para visualizar os produtos.";
        }

        if (produtos.isEmpty()) {
            return "Nenhum produto cadastrado para " + produtor.getNome() + ".";
        }

        StringBuilder resumo = new StringBuilder();
        resumo.append("Produtos de ").append(produtor.getNome()).append("\n\n");
        for (Produto produto : produtos) {
            resumo.append("- ")
                    .append(produto.getNome())
                    .append(" | ")
                    .append(produto.getCategoria())
                    .append(" | ")
                    .append(currencyFormat.format(produto.getPreco()))
                    .append(" | Estoque: ")
                    .append(produto.getEstoque())
                    .append(" | Mínimo: ")
                    .append(produto.getEstoqueMinimo());
            if (produto.isEstoqueBaixo()) {
                resumo.append(" | baixo");
            }
            resumo.append("\n");
        }
        return resumo.toString();
    }

    private String resumoProdutosPorProdutor(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            return "Nenhum produto cadastrado.";
        }

        List<String> produtores = new ArrayList<>();
        for (Produto produto : produtos) {
            String produtor = nomeProdutor(produto);
            if (!produtores.contains(produtor)) {
                produtores.add(produtor);
            }
        }

        StringBuilder resumo = new StringBuilder();
        for (String produtor : produtores) {
            resumo.append(produtor).append("\n");
            for (Produto produto : produtos) {
                if (!produtor.equals(nomeProdutor(produto))) {
                    continue;
                }
                resumo.append("  - ")
                        .append(produto.getNome())
                        .append(" | ")
                        .append(currencyFormat.format(produto.getPreco()))
                        .append(" | Estoque: ")
                        .append(produto.getEstoque());
                if (produto.isEstoqueBaixo()) {
                    resumo.append(" | baixo");
                }
                resumo.append("\n");
            }
            resumo.append("\n");
        }
        return resumo.toString();
    }

    private String resumoPedidosRecentes(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return "Nenhum pedido cadastrado.";
        }

        StringBuilder resumo = new StringBuilder();
        int limit = Math.min(6, pedidos.size());
        for (int index = 0; index < limit; index++) {
            Pedido pedido = pedidos.get(index);
            if (resumo.length() > 0) {
                resumo.append("\n");
            }
            resumo.append("Pedido #")
                    .append(pedido.getId())
                    .append(" | ")
                    .append(formatarData(pedido.getDataPedido()))
                    .append(" | ")
                    .append(currencyFormat.format(totalVisivelPedido(pedido)))
                    .append("\n")
                    .append(nomeCliente(pedido.getCliente()))
                    .append("\n")
                    .append(produtoresDoPedido(pedido))
                    .append(" | ")
                    .append(formatarStatus(pedido.getStatus()))
                    .append("\n");
        }

        return resumo.toString();
    }

    private List<Pedido> pedidosDoProdutor() {
        List<Pedido> pedidos = new ArrayList<>();
        boolean todos = usuarioAdministrador() && pedidosTodosProdutoresSelecionado();
        for (Pedido pedido : pedidoController.listarPedidos()) {
            if (todos || pedidoPertenceAoProdutor(pedido)) {
                pedidos.add(pedido);
            }
        }
        if (todos) {
            pedidos.sort((first, second) -> {
                int producerCompare = produtoresDoPedido(first).compareToIgnoreCase(produtoresDoPedido(second));
                return producerCompare != 0 ? producerCompare : compararPedidosPorData(second, first);
            });
        } else {
            pedidos.sort((first, second) -> compararPedidosPorData(second, first));
        }
        return pedidos;
    }

    private int compararPedidosPorData(Pedido first, Pedido second) {
        int dateCompare = first.getDataPedido().compareTo(second.getDataPedido());
        return dateCompare != 0 ? dateCompare : Integer.compare(first.getId(), second.getId());
    }

    private boolean pedidoPertenceAoProdutor(Pedido pedido) {
        Usuario produtor = produtorPedidosSelecionado();
        if (pedido == null || produtor == null) {
            return false;
        }

        for (ItemPedido item : pedido.getItens()) {
            if (item != null && item.pertenceAoProdutor(produtor)) {
                return true;
            }
        }
        return false;
    }

    private List<ItemPedido> itensVisiveisDoPedido(Pedido pedido) {
        List<ItemPedido> itens = new ArrayList<>();
        if (pedido == null) {
            return itens;
        }

        Usuario produtor = produtorPedidosSelecionado();
        boolean todos = usuarioAdministrador() && pedidosTodosProdutoresSelecionado();
        for (ItemPedido item : pedido.getItens()) {
            if (item == null) {
                continue;
            }
            if (todos || produtor == null || item.pertenceAoProdutor(produtor)) {
                itens.add(item);
            }
        }
        return itens;
    }

    private double totalVisivelPedido(Pedido pedido) {
        double total = 0;
        for (ItemPedido item : itensVisiveisDoPedido(pedido)) {
            total += item.getSubtotal();
        }
        return total;
    }

    private List<Produto> produtosDoContexto() {
        Usuario produtor = produtorProdutosSelecionado();
        if (produtor == null) {
            return new ArrayList<>();
        }
        return produtoController.listarProdutosDoProdutor(produtor);
    }

    private List<Produto> produtosDisponiveisParaPedido() {
        if (usuarioAdministrador()) {
            return produtoController.listarProdutos();
        }
        return produtoController.listarProdutosDoProdutor(produtorLogado);
    }

    private Usuario produtorProdutosSelecionado() {
        if (!usuarioAdministrador()) {
            return produtorLogado;
        }

        Object selecionado = produtorProdutosCombo.getSelectedItem();
        return selecionado instanceof Usuario usuario ? usuario : null;
    }

    private Usuario produtorPedidosSelecionado() {
        if (!usuarioAdministrador()) {
            return produtorLogado;
        }

        Object selecionado = produtorPedidosCombo.getSelectedItem();
        return selecionado instanceof Usuario usuario ? usuario : null;
    }

    private boolean pedidosTodosProdutoresSelecionado() {
        return usuarioAdministrador() && !(produtorPedidosCombo.getSelectedItem() instanceof Usuario);
    }

    private String produtoresDoPedido(Pedido pedido) {
        List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
        if (pedido == null || itens.isEmpty()) {
            return "Sem produtor";
        }

        List<String> nomes = new ArrayList<>();
        for (ItemPedido item : itens) {
            if (item == null || item.getProduto() == null || item.getProduto().getProdutor() == null) {
                if (!nomes.contains("Sem produtor")) {
                    nomes.add("Sem produtor");
                }
                continue;
            }

            String nome = item.getProduto().getProdutor().getNome();
            if (!nomes.contains(nome)) {
                nomes.add(nome);
            }
        }
        return nomes.isEmpty() ? "Sem produtor" : String.join(", ", nomes);
    }

    private String nomeProdutor(Produto produto) {
        if (produto == null || produto.getProdutor() == null) {
            return "Sem produtor";
        }
        return produto.getProdutor().getNome();
    }

    private boolean selecionarPedido(int pedidoId) {
        for (int row = 0; row < pedidosTable.getRowCount(); row++) {
            Object value = pedidosTable.getValueAt(row, 0);
            if (value instanceof Integer && (Integer) value == pedidoId) {
                pedidosTable.setRowSelectionInterval(row, row);
                selecionarPedidoNoFormulario(pedidoId);
                return true;
            }
        }
        return false;
    }

    private void selecionarPedidoNoFormulario(int pedidoId) {
        for (int index = 0; index < pedidoStatusComboModel.getSize(); index++) {
            Pedido pedido = pedidoStatusComboModel.getElementAt(index);
            if (pedido != null && pedido.getId() == pedidoId) {
                pedidoStatusCombo.setSelectedItem(pedido);
                atualizarItensStatusCombo();
                atualizarAcoesPedido();
                return;
            }
        }
    }

    private void selecionarAba(String titulo) {
        int index = indiceAba(titulo);
        if (index >= 0) {
            navigationTabs.setSelectedIndex(index);
        }
    }

    private int indiceAba(String titulo) {
        if (navigationTabs == null || titulo == null) {
            return -1;
        }

        for (int index = 0; index < navigationTabs.getTabCount(); index++) {
            if (titulo.equals(navigationTabs.getTitleAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private Pedido pedidoSelecionadoNaTabela() {
        Integer pedidoId = pedidoSelecionadoIdNaTabela();
        return pedidoId == null ? null : pedidoController.buscarPedido(pedidoId);
    }

    private Integer pedidoSelecionadoIdNaTabela() {
        int selectedRow = pedidosTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = pedidosTable.convertRowIndexToModel(selectedRow);
        Object value = pedidosTable.getModel().getValueAt(modelRow, 0);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    private String resumoItens(Pedido pedido) {
        List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
        if (itens.isEmpty()) {
            return "Sem itens";
        }

        StringBuilder resumo = new StringBuilder();
        for (ItemPedido item : itens) {
            if (item == null) {
                continue;
            }
            if (resumo.length() > 0) {
                resumo.append(", ");
            }
            Produto produto = item.getProduto();
            String nomeProduto = produto == null ? "Produto removido" : produto.getNome();
            resumo.append(nomeProduto)
                    .append(" x")
                    .append(item.getQuantidade())
                    .append(" (")
                    .append(formatarStatus(item.getStatus()))
                    .append(", rec. ")
                    .append(item.getQuantidadeRecebida())
                    .append("/")
                    .append(item.getQuantidade())
                    .append(")");
        }
        return resumo.length() == 0 ? "Sem itens" : resumo.toString();
    }

    private String resumoEnvio(Pedido pedido) {
        List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
        if (itens.isEmpty()) {
            return "0/0";
        }

        int total = 0;
        int enviados = 0;
        for (ItemPedido item : itens) {
            if (item == null) {
                continue;
            }
            total += item.getQuantidade();
            enviados += item.getQuantidadeEnviada();
        }

        return enviados + "/" + total + " enviados";
    }

    private String resumoRecebimento(Pedido pedido) {
        List<ItemPedido> itens = itensVisiveisDoPedido(pedido);
        if (itens.isEmpty()) {
            return "0/0";
        }

        int total = 0;
        int recebidos = 0;
        int indisponiveis = 0;
        for (ItemPedido item : itens) {
            if (item == null) {
                continue;
            }
            total += item.getQuantidade();
            recebidos += item.getQuantidadeRecebida();
            if ("INDISPONIVEL".equals(item.getStatus())) {
                indisponiveis++;
            }
        }

        String resumo = recebidos + "/" + total + " recebidos";
        if (indisponiveis > 0) {
            resumo += " | " + indisponiveis + " indispon.";
        }
        return resumo;
    }

    private String formatarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        return switch (status) {
            case "AGUARDANDO_PRODUTORES" -> "AGUARDANDO PRODUTORES";
            case "AGUARDANDO_RECEBIMENTO" -> "AGUARDANDO RECEBIMENTO";
            case FILTRO_COM_DIVERGENCIA -> "COM DIVERGÊNCIA";
            case FILTRO_AGUARDANDO_PRODUTOR -> "AGUARDANDO PRODUTOR";
            case FILTRO_EM_TRANSITO -> "EM TRÂNSITO";
            case FILTRO_PREVISTO_HOJE -> "PREVISTO HOJE";
            case FILTRO_ATRASADO -> "ATRASADO";
            case FILTRO_PRONTO_SEPARACAO -> "PRONTO PARA SEPARAÇÃO";
            case "PARCIALMENTE_RECEBIDO" -> "PARCIALMENTE RECEBIDO";
            case "PENDENCIA" -> "PENDÊNCIA";
            case "EM_SEPARACAO" -> "EM SEPARAÇÃO";
            case "ENVIADO" -> "ENVIADO À COOPERATIVA";
            case ACAO_ENVIAR_TUDO -> "ENVIAR TUDO";
            case ACAO_ENVIAR_PARCIAL -> "ENVIAR PARCIAL";
            case ACAO_RECEBER_TUDO -> "RECEBER TUDO";
            case ACAO_RECEBER_PARCIAL -> "RECEBER PARCIAL";
            case "ENTREGUE_COOPERATIVA" -> "ENTREGUE À COOPERATIVA";
            case "INDISPONIVEL" -> "INDISPONÍVEL";
            default -> status.replace('_', ' ');
        };
    }

    private void limparFormularioProduto() {
        produtoSelecionadoId = -1;
        produtoFormStateLabel.setText("Novo produto");
        salvarProdutoButton.setText("Cadastrar");
        excluirProdutoButton.setEnabled(false);
        nomeProdutoField.setText("");
        precoProdutoField.setText("");
        estoqueProdutoSpinner.setValue(0);
        estoqueMinimoProdutoSpinner.setValue(3);
        categoriaProdutoCombo.setSelectedIndex(0);
        produtosTable.clearSelection();
    }

    private void limparFormularioUsuario() {
        if (!usuarioAdministrador() && produtorLogado != null) {
            carregarUsuarioNoFormulario(produtorLogado);
            aplicarPermissoesUsuario();
            return;
        }

        usuarioSelecionadoId = -1;
        usuarioFormStateLabel.setText("Novo usuário");
        salvarUsuarioButton.setText("Cadastrar");
        nomeUsuarioField.setText("");
        tipoUsuarioCombo.setSelectedIndex(0);
        loginUsuarioField.setText("");
        senhaUsuarioField.setText("");
        confirmarSenhaUsuarioField.setText("");
        usuariosTable.clearSelection();
        excluirUsuarioButton.setEnabled(false);
        aplicarPermissoesUsuario();
    }

    private void limparFormularioCliente() {
        clienteSelecionadoId = -1;
        clienteFormStateLabel.setText("Novo cliente");
        salvarClienteButton.setText("Cadastrar");
        excluirClienteButton.setEnabled(false);
        nomeClienteField.setText("");
        emailClienteField.setText("");
        telefoneClienteField.setText("");
        clientesTable.clearSelection();
    }

    private void atualizarProdutorLogado() {
        if (produtorLogado == null) {
            produtorLogadoLabel.setText("Usuário: -");
            setTitle("CoopManager");
            return;
        }

        produtorLogadoLabel.setText("Usuário: " + produtorLogado.getNome() + " (" + produtorLogado.getTipo() + ")");
        setTitle("CoopManager - " + produtorLogado.getNome());
    }

    private double parseMoney(String value) {
        String normalized = value.trim()
                .replace("R$", "")
                .replace("\u00A0", "")
                .replace(" ", "");
        if (normalized.contains(",")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        }
        if (normalized.isEmpty()) {
            throw new NumberFormatException("empty");
        }
        return Double.parseDouble(normalized);
    }

    private LocalDate parseDataOpcional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(value.trim(), dateFormat);
    }

    private String formatarData(LocalDate date) {
        return date == null ? "" : dateFormat.format(date);
    }

    private String dataPrevistaTexto(Pedido pedido) {
        String data = pedido == null ? "" : formatarData(pedido.getDataPrevista());
        return data.isBlank() ? "Sem previsão" : data;
    }

    private String alertaPrazoPedido(Pedido pedido) {
        if (pedido == null
                || pedido.getDataPrevista() == null
                || "ENTREGUE".equals(pedido.getStatus())
                || "CANCELADO".equals(pedido.getStatus())) {
            return "";
        }

        LocalDate hoje = LocalDate.now();
        if (pedido.getDataPrevista().isBefore(hoje)) {
            return "Atrasado";
        }
        if (pedido.getDataPrevista().isEqual(hoje)) {
            return "Previsto para hoje";
        }
        return "";
    }

    private void formatarPrecoProduto() {
        String value = precoProdutoField.getText().trim();
        if (value.isEmpty()) {
            return;
        }

        try {
            precoProdutoField.setText(currencyFormat.format(parseMoney(value)));
        } catch (NumberFormatException exception) {
            showError("Informe um preço válido.");
            precoProdutoField.requestFocusInWindow();
        }
    }

    private void atualizarPaleta() {
        if (darkMode) {
            backgroundColor = new Color(1, 6, 4);
            panelColor = new Color(5, 18, 12);
            accentColor = new Color(0, 184, 89);
            textColor = Color.WHITE;
            mutedTextColor = new Color(219, 244, 226);
            borderColor = new Color(22, 104, 61);
            fieldColor = new Color(0, 12, 8);
            secondaryButtonColor = new Color(9, 42, 25);
            tableHeaderColor = new Color(0, 37, 22);
        } else {
            backgroundColor = new Color(242, 248, 244);
            panelColor = Color.WHITE;
            accentColor = new Color(0, 115, 58);
            textColor = new Color(28, 43, 35);
            mutedTextColor = new Color(82, 101, 90);
            borderColor = new Color(212, 226, 218);
            fieldColor = Color.WHITE;
            secondaryButtonColor = new Color(229, 240, 233);
            tableHeaderColor = new Color(233, 242, 236);
        }
    }

    private void aplicarTema() {
        atualizarPaleta();
        configurarControlesDoTema();
        temaButton.setText(temaSymbol());
        temaButton.setToolTipText(temaTooltip());
        getContentPane().setBackground(backgroundColor);
        aplicarTemaEm(getContentPane());
        estilizarTabela(produtosTable);
        estilizarTabela(pedidosTable);
        estilizarTabela(usuariosTable);
        estilizarTabela(clientesTable);
        estilizarTabela(itensNovoPedidoTable);
        totalProdutosValue.setForeground(accentColor);
        estoqueTotalValue.setForeground(accentColor);
        estoqueBaixoValue.setForeground(accentColor);
        totalPedidosValue.setForeground(accentColor);
        pedidosAbertosValue.setForeground(accentColor);
        pedidosHojeValue.setForeground(accentColor);
        pedidosAtrasadosValue.setForeground(accentColor);
        divergenciasValue.setForeground(accentColor);
        valorPedidosValue.setForeground(accentColor);
        produtoFormStateLabel.setForeground(accentColor);
        usuarioFormStateLabel.setForeground(accentColor);
        clienteFormStateLabel.setForeground(accentColor);
        repaint();
    }

    private void aplicarTemaEm(Component component) {
        if (component instanceof JComponent jComponent) {
            Object surface = jComponent.getClientProperty("surface");
            if ("base".equals(surface)) {
                component.setBackground(backgroundColor);
            } else if ("card".equals(surface)) {
                component.setBackground(panelColor);
                jComponent.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            } else if ("metric".equals(surface)) {
                component.setBackground(panelColor);
                jComponent.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            } else if ("header".equals(surface)) {
                component.setBackground(accentColor);
            }

            Object role = jComponent.getClientProperty("role");
            if ("headerText".equals(role)) {
                component.setForeground(Color.WHITE);
            } else if ("headerSubtle".equals(role)) {
                component.setForeground(new Color(226, 238, 232));
            } else if ("accent".equals(role)) {
                component.setForeground(accentColor);
            } else if ("helper".equals(role)) {
                component.setForeground(mutedTextColor);
                jComponent.setBorder(helperBorder());
            } else if ("muted".equals(role)) {
                component.setForeground(mutedTextColor);
            } else if (component instanceof JLabel) {
                component.setForeground(textColor);
            }

            if (component instanceof JTextComponent textComponent) {
                estilizarTexto(textComponent);
            }

            if (component instanceof JComboBox<?> comboBox) {
                estilizarCombo(comboBox);
            }

            if (component instanceof JSpinner spinner) {
                estilizarSpinner(spinner);
            }

            if (component instanceof JCheckBox checkBox) {
                checkBox.setBackground(panelColor);
                checkBox.setForeground(textColor);
                checkBox.setOpaque(false);
                checkBox.setFocusPainted(false);
            }

            if (component instanceof javax.swing.JTabbedPane) {
                ((javax.swing.JTabbedPane) component).setUI(new ThemedTabbedPaneUI());
                component.setBackground(backgroundColor);
                component.setForeground(textColor);
            }

            if (component instanceof JScrollPane scrollPane) {
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
                scrollPane.getViewport().setBackground(fieldColor);
                scrollPane.setBackground(panelColor);
            }

            if (component instanceof JSplitPane splitPane) {
                splitPane.setBackground(panelColor);
                splitPane.setBorder(BorderFactory.createEmptyBorder());
            }

            if (component instanceof RoundedButton roundedButton) {
                roundedButton.atualizarTema();
            }
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                aplicarTemaEm(child);
            }
        }
    }

    private void configurarControlesDoTema() {
        javax.swing.UIManager.put("TabbedPane.background", backgroundColor);
        javax.swing.UIManager.put("TabbedPane.foreground", textColor);
        javax.swing.UIManager.put("TabbedPane.selected", panelColor);
        javax.swing.UIManager.put("TabbedPane.contentAreaColor", backgroundColor);
        javax.swing.UIManager.put("TabbedPane.focus", accentColor);
        javax.swing.UIManager.put("TableHeader.background", tableHeaderColor);
        javax.swing.UIManager.put("TableHeader.foreground", Color.WHITE);
        javax.swing.UIManager.put("ComboBox.background", fieldColor);
        javax.swing.UIManager.put("ComboBox.foreground", textColor);
        javax.swing.UIManager.put("ComboBox.selectionBackground", accentColor);
        javax.swing.UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        javax.swing.UIManager.put("Spinner.background", fieldColor);
        javax.swing.UIManager.put("Spinner.foreground", textColor);
        javax.swing.UIManager.put("TextField.background", fieldColor);
        javax.swing.UIManager.put("TextField.foreground", textColor);
        javax.swing.UIManager.put("TextField.caretForeground", textColor);
        javax.swing.UIManager.put("FormattedTextField.background", fieldColor);
        javax.swing.UIManager.put("FormattedTextField.foreground", textColor);
        javax.swing.UIManager.put("FormattedTextField.caretForeground", textColor);
        javax.swing.UIManager.put("FormattedTextField.inactiveBackground", fieldColor);
    }

    private void estilizarTexto(JTextComponent textComponent) {
        textComponent.setBackground(fieldColor);
        textComponent.setForeground(textColor);
        textComponent.setCaretColor(textColor);
        textComponent.setSelectedTextColor(Color.WHITE);
        textComponent.setSelectionColor(accentColor.darker());
        textComponent.setDisabledTextColor(mutedTextColor);
        textComponent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        textComponent.setOpaque(true);
    }

    private void estilizarCombo(JComboBox<?> comboBox) {
        comboBox.setBackground(fieldColor);
        comboBox.setForeground(textColor);
        comboBox.setFont(comboBox.getFont().deriveFont(Font.PLAIN, 12f));
        comboBox.setOpaque(false);
        comboBox.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));
        comboBox.setRenderer(new ThemedListCellRenderer());
        configurarLarguraDoCombo(comboBox);
        comboBox.setUI(new RoundedComboBoxUI());
    }

    @SuppressWarnings("unchecked")
    private void configurarLarguraDoCombo(JComboBox<?> comboBox) {
        if (comboBox == categoriaProdutoCombo) {
            ((JComboBox<String>) comboBox).setPrototypeDisplayValue("AGRICULTURA");
        } else if (comboBox == tipoUsuarioCombo) {
            ((JComboBox<String>) comboBox).setPrototypeDisplayValue("PRODUTOR");
        } else if (comboBox == produtorProdutosCombo) {
            ((JComboBox<Usuario>) comboBox).setPrototypeDisplayValue(new Usuario(0, "Produtor da Agricultura", "PRODUTOR"));
        } else if (comboBox == produtorPedidosCombo) {
            ((JComboBox<Object>) comboBox).setPrototypeDisplayValue(TODOS_OS_PRODUTORES);
        } else if (comboBox == produtoPedidoCombo) {
            ((JComboBox<Produto>) comboBox).setPrototypeDisplayValue(
                    new Produto(0, "Produto com nome longo", "AGRICULTURA", 45, 0, new Usuario(0, "Produtor", "PRODUTOR"))
            );
        } else if (comboBox == pedidoStatusCombo) {
            ((JComboBox<Pedido>) comboBox).setPrototypeDisplayValue(new Pedido(999, new Cliente(0, "Cliente com nome longo"), "ENTREGA"));
        } else if (comboBox == itemStatusCombo) {
            ((JComboBox<ItemPedido>) comboBox).setPrototypeDisplayValue(new ItemPedido(
                    new Produto(0, "Produto com nome longo", "AGRICULTURA", 0, 0, new Usuario(0, "Produtor", "PRODUTOR")),
                    999
            ));
        } else if (comboBox == statusPedidoCombo) {
            ((JComboBox<String>) comboBox).setPrototypeDisplayValue("EM_SEPARACAO");
        } else if (comboBox == statusPedidoFiltroCombo) {
            ((JComboBox<String>) comboBox).setPrototypeDisplayValue("PARCIALMENTE_RECEBIDO");
        }
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setUI(new ThemedSpinnerUI());
        spinner.setBackground(fieldColor);
        spinner.setForeground(textColor);
        spinner.setOpaque(false);
        spinner.setBorder(BorderFactory.createLineBorder(borderColor));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JFormattedTextField textField = defaultEditor.getTextField();
            textField.setUI(new BasicFormattedTextFieldUI());
            estilizarTexto(textField);
            textField.setBackground(fieldColor);
            textField.setForeground(textColor);
            textField.setCaretColor(textColor);
            textField.setDisabledTextColor(mutedTextColor);
            textField.setHorizontalAlignment(SwingConstants.RIGHT);
            textField.setOpaque(true);
            textField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }
        estilizarSpinnerChildren(spinner);
    }

    private void estilizarSpinnerChildren(Component component) {
        if (component instanceof JComponent jComponent) {
            jComponent.setBackground(fieldColor);
            jComponent.setForeground(textColor);
        }

        if (component instanceof JButton button) {
            button.setBackground(fieldColor);
            button.setForeground(textColor);
            button.setBorder(BorderFactory.createLineBorder(borderColor));
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                estilizarSpinnerChildren(child);
            }
        }
    }

    private void estilizarTabela(JTable table) {
        table.setBackground(fieldColor);
        table.setForeground(textColor);
        table.setGridColor(borderColor);
        table.setSelectionBackground(darkMode ? new Color(0, 126, 72) : new Color(207, 235, 216));
        table.setSelectionForeground(darkMode ? Color.WHITE : textColor);
        table.getTableHeader().setBackground(tableHeaderColor);
        table.getTableHeader().setForeground(darkMode ? Color.WHITE : textColor);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setDefaultRenderer(new ThemedTableHeaderRenderer());
    }

    private JPanel basePanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.putClientProperty("surface", "base");
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return panel;
    }

    private JPanel wrapPanel(String title, Component component) {
        JPanel panel = new RoundedPanel(new BorderLayout(), 22);
        panel.putClientProperty("surface", "card");
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel label = sectionTitle(title);
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane scrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        return scrollPane;
    }

    private JPanel leftColumn(Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(component, BorderLayout.NORTH);
        return panel;
    }

    private JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setMinimumSize(new Dimension(280, 0));
        return form;
    }

    private JPanel compactField(String labelText, java.awt.Component component) {
        prepararCampoFormulario(component);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.putClientProperty("role", "muted");
        label.setForeground(textColor);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JLabel helperLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:300px;'>" + escapeHtml(text) + "</div></html>");
        label.putClientProperty("role", "helper");
        label.setForeground(mutedTextColor);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
        label.setBorder(helperBorder());
        return label;
    }

    private void addInstruction(JPanel form, int row, String text) {
        JLabel label = helperLabel(text);
        GridBagConstraints constraints = fieldConstraints(row);
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(label, constraints);
    }

    private JLabel addField(JPanel form, int row, String labelText, java.awt.Component component) {
        prepararCampoFormulario(component);

        JLabel label = new JLabel(labelText);
        label.putClientProperty("role", "muted");
        label.setForeground(textColor);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));

        GridBagConstraints labelConstraints = fieldConstraints(row);
        labelConstraints.gridx = 0;
        labelConstraints.weightx = 0;
        labelConstraints.fill = GridBagConstraints.NONE;
        form.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = fieldConstraints(row);
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(component, fieldConstraints);
        return label;
    }

    private javax.swing.border.Border helperBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(2, 8, 6, 4)
        );
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void addFormFiller(JPanel form, int row) {
        GridBagConstraints fillerConstraints = fieldConstraints(row);
        fillerConstraints.gridx = 0;
        fillerConstraints.gridwidth = 2;
        fillerConstraints.weighty = 1;
        fillerConstraints.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        form.add(filler, fillerConstraints);
    }

    private void prepararCampoFormulario(java.awt.Component component) {
        if (!(component instanceof JComponent jComponent)) {
            return;
        }

        Dimension preferred = jComponent.getPreferredSize();
        int height = component instanceof JComboBox<?> || component instanceof JSpinner ? 34 : Math.max(30, preferred.height);
        int width = Math.max(180, preferred.width);
        Dimension fieldSize = new Dimension(width, height);
        jComponent.setPreferredSize(fieldSize);
        jComponent.setMinimumSize(new Dimension(150, height));
    }

    private void configurarSpinnerQuantidade(JSpinner spinner, int minimo, int maximo, int valor) {
        int min = Math.max(0, minimo);
        int max = Math.max(min, maximo);
        int value = Math.max(min, Math.min(valor, max));
        spinner.setModel(new SpinnerNumberModel(value, min, max, 1));
        prepararCampoFormulario(spinner);
        estilizarSpinner(spinner);
    }

    private int spinnerValue(JSpinner spinner) {
        Object value = spinner.getValue();
        return value instanceof Number number ? number.intValue() : 0;
    }

    private GridBagConstraints fieldConstraints(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(6, 0, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private JPanel metricPanel(String title, JLabel value) {
        JPanel panel = new RoundedPanel(new BorderLayout(0, 8), 20);
        panel.putClientProperty("surface", "metric");
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty("role", "muted");
        titleLabel.setForeground(mutedTextColor);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JLabel metricValue() {
        JLabel label = new JLabel("0");
        label.putClientProperty("role", "accent");
        label.setForeground(accentColor);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        return label;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.putClientProperty("role", "title");
        label.setForeground(textColor);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionBackground(new Color(213, 232, 224));
        table.setSelectionForeground(textColor);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setDefaultRenderer(Object.class, new ThemedTableCellRenderer());
        ajustarColunas(table);

        return table;
    }

    private void ajustarColunas(JTable table) {
        for (int index = 0; index < table.getColumnCount(); index++) {
            String name = table.getColumnName(index);
            int width = switch (name) {
                case "ID" -> 54;
                case "Data", "Prevista" -> 96;
                case "Produto", "Itens" -> 220;
                case "Nome", "Cliente", "Produtor", "Produtores" -> 180;
                case "Categoria", "Status" -> 118;
                case "Login", "E-mail" -> 150;
                case "Telefone" -> 120;
                case "Preço", "Total" -> 96;
                case "Envio", "Recebimento" -> 150;
                case "Estoque", "Mínimo" -> 82;
                default -> 110;
            };
            table.getColumnModel().getColumn(index).setPreferredWidth(width);
        }
    }

    private JButton primaryButton(String text) {
        return new RoundedButton(text, true);
    }

    private JButton secondaryButton(String text) {
        return new RoundedButton(text, false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(dialogOwner(), message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(dialogOwner(), message, "CoopManager", JOptionPane.INFORMATION_MESSAGE);
    }

    private Component dialogOwner() {
        return isShowing() ? this : null;
    }

    private boolean usuarioAdministrador() {
        return produtorLogado != null && "ADMIN".equals(produtorLogado.getTipo());
    }

    private int quantidadeAdministradores() {
        int total = 0;
        for (Usuario usuario : usuarioRepository.listarTodos()) {
            if (usuario != null && "ADMIN".equals(usuario.getTipo())) {
                total++;
            }
        }
        return total;
    }

    private boolean produtoPertenceAoProdutor(Produto produto) {
        Usuario produtor = produtorProdutosSelecionado();
        if (produto == null || produtor == null) {
            return false;
        }

        return produto.getProdutor() != null
                && produto.getProdutor().getId() == produtor.getId();
    }

    private boolean produtoEmPedido(int produtoId) {
        for (Pedido pedido : pedidoController.listarPedidos()) {
            if (pedido == null) {
                continue;
            }

            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item == null ? null : item.getProduto();
                if (produto != null && produto.getId() == produtoId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean clienteEmPedido(int clienteId) {
        for (Pedido pedido : pedidoController.listarPedidos()) {
            if (pedido == null) {
                continue;
            }

            Usuario cliente = pedido.getCliente();
            if (cliente != null && cliente.getId() == clienteId) {
                return true;
            }
        }
        return false;
    }

    private boolean produtorEmPedido(int produtorId) {
        for (Pedido pedido : pedidoController.listarPedidos()) {
            if (pedido == null) {
                continue;
            }

            for (ItemPedido item : pedido.getItens()) {
                Produto produto = item == null ? null : item.getProduto();
                Usuario produtor = produto == null ? null : produto.getProdutor();
                if (produtor != null && produtor.getId() == produtorId) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean produtorTemProdutos(int produtorId) {
        for (Produto produto : produtoController.listarProdutos()) {
            Usuario produtor = produto == null ? null : produto.getProdutor();
            if (produtor != null && produtor.getId() == produtorId) {
                return true;
            }
        }
        return false;
    }

    private String nomeCliente(Usuario cliente) {
        return cliente == null ? "Sem cliente" : cliente.getNome();
    }

    private String contatoCliente(Usuario cliente) {
        if (!(cliente instanceof Cliente clienteComContato)) {
            return "";
        }

        List<String> contatos = new ArrayList<>();
        if (!clienteComContato.getEmail().isBlank()) {
            contatos.add(clienteComContato.getEmail());
        }
        if (!clienteComContato.getTelefone().isBlank()) {
            contatos.add(clienteComContato.getTelefone());
        }
        return String.join(" | ", contatos);
    }

    private String autorHistorico() {
        return produtorLogado == null ? "Sistema" : produtorLogado.getNome();
    }

    private String observacaoHistorico() {
        return observacaoPedidoField == null ? "" : observacaoPedidoField.getText().trim();
    }

    private String textoCombo(Object value) {
        if (value instanceof Pedido pedido) {
            return "Pedido #" + pedido.getId() + " | " + formatarData(pedido.getDataPedido())
                    + " | " + nomeCliente(pedido.getCliente());
        }
        if (value instanceof Produto produto) {
            return produto.getNome()
                    + " | "
                    + currencyFormat.format(produto.getPreco())
                    + " | "
                    + nomeProdutor(produto);
        }
        if (value instanceof ItemPedido item) {
            Produto produto = item.getProduto();
            String nomeProduto = produto == null ? "Produto removido" : produto.getNome();
            return nomeProdutor(produto)
                    + " | "
                    + nomeProduto
                    + " x"
                    + item.getQuantidade()
                    + " | env. "
                    + item.getQuantidadeEnviada()
                    + "/"
                    + item.getQuantidade()
                    + " | rec. "
                    + item.getQuantidadeRecebida()
                    + "/"
                    + item.getQuantidade();
        }
        if (value instanceof Usuario usuario) {
            return usuario.getNome();
        }
        if (value instanceof String text) {
            return textoStatusOuValor(text);
        }
        return value == null ? "" : value.toString();
    }

    private String textoStatusOuValor(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("_")
                || "PENDENCIA".equals(value)
                || "INDISPONIVEL".equals(value)
                || "ENVIADO".equals(value)) {
            return formatarStatus(value);
        }
        return value;
    }

    private Color corPrazoLinha(JTable table, int viewRow) {
        if (table != pedidosTable || viewRow < 0) {
            return null;
        }

        try {
            int previstaColumn = table.getColumnModel().getColumnIndex("Prevista");
            int statusColumn = table.getColumnModel().getColumnIndex("Status");
            Object previstaValue = table.getValueAt(viewRow, previstaColumn);
            Object statusValue = table.getValueAt(viewRow, statusColumn);
            String prevista = previstaValue == null ? "" : previstaValue.toString().trim();
            String status = statusValue == null ? "" : statusValue.toString().trim();
            if (prevista.isBlank() || "ENTREGUE".equals(status) || "CANCELADO".equals(status)) {
                return null;
            }

            LocalDate dataPrevista = parseDataOpcional(prevista);
            LocalDate hoje = LocalDate.now();
            if (dataPrevista.isBefore(hoje)) {
                return darkMode ? new Color(58, 14, 14) : new Color(255, 236, 236);
            }
            if (dataPrevista.isEqual(hoje)) {
                return darkMode ? new Color(53, 42, 5) : new Color(255, 248, 218);
            }
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            return null;
        }
        return null;
    }

    private class ThemedListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private boolean selected;

        @Override
        public Component getListCellRendererComponent(
                javax.swing.JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            selected = isSelected && index >= 0;
            setText(textoCombo(value));
            setOpaque(false);
            setFont(getFont().deriveFont(Font.PLAIN, 12f));
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            setBackground(selected ? accentColor : fieldColor);
            setForeground(selected ? Color.WHITE : textColor);
            component.setBackground(getBackground());
            component.setForeground(getForeground());
            return component;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (selected) {
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(accentColor);
                graphics2D.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);
                graphics2D.dispose();
            } else {
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setColor(fieldColor);
                graphics2D.fillRect(0, 0, getWidth(), getHeight());
                graphics2D.dispose();
            }

            super.paintComponent(graphics);
        }
    }

    private class ThemedTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                setBackground(darkMode ? new Color(0, 126, 72) : new Color(198, 232, 210));
                setForeground(darkMode ? Color.WHITE : textColor);
            } else {
                Color deadlineColor = corPrazoLinha(table, row);
                setBackground(deadlineColor != null
                        ? deadlineColor
                        : row % 2 == 0
                        ? fieldColor
                        : (darkMode ? new Color(3, 15, 10) : new Color(248, 252, 249)));
                setForeground(textColor);
            }

            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, borderColor),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
            ));
            setHorizontalAlignment(column == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            return this;
        }
    }

    private class ThemedTableHeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        ThemedTableHeaderRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, borderColor),
                    BorderFactory.createEmptyBorder(5, 7, 5, 7)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(tableHeaderColor);
            setForeground(darkMode ? Color.WHITE : textColor);
            setFont(table.getFont().deriveFont(Font.BOLD, 12f));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    private class ThemedTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets.left = 0;
            tabAreaInsets.right = 0;
            tabAreaInsets.top = 0;
            tabAreaInsets.bottom = 8;
            tabInsets.left = 18;
            tabInsets.right = 18;
            tabInsets.top = 8;
            tabInsets.bottom = 8;
        }

        @Override
        protected void paintTabBackground(
                Graphics graphics,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int width,
                int height,
                boolean isSelected
        ) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = isSelected ? accentColor : (darkMode ? new Color(4, 20, 13) : new Color(226, 238, 230));
            graphics2D.setColor(color);
            graphics2D.fillRoundRect(x + 2, y + 2, width - 4, height - 4, 14, 14);
            graphics2D.dispose();
        }

        @Override
        protected void paintTabBorder(
                Graphics graphics,
                int tabPlacement,
                int tabIndex,
                int x,
                int y,
                int width,
                int height,
                boolean isSelected
        ) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(isSelected ? accentColor : borderColor);
            graphics2D.drawRoundRect(x + 2, y + 2, width - 5, height - 5, 14, 14);
            graphics2D.dispose();
        }

        @Override
        protected void paintText(
                Graphics graphics,
                int tabPlacement,
                Font font,
                java.awt.FontMetrics metrics,
                int tabIndex,
                String title,
                Rectangle textRect,
                boolean isSelected
        ) {
            graphics.setFont(font.deriveFont(Font.PLAIN, 12f));
            graphics.setColor(isSelected ? selectedTabTextColor() : mutedTextColor);
            graphics.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }

        private Color selectedTabTextColor() {
            return darkMode ? Color.WHITE : accentColor;
        }

        @Override
        protected void paintFocusIndicator(
                Graphics graphics,
                int tabPlacement,
                Rectangle[] rectangles,
                int tabIndex,
                Rectangle iconRect,
                Rectangle textRect,
                boolean isSelected
        ) {
            // Sem retângulo de foco padrão do Nimbus.
        }

        @Override
        protected void paintContentBorder(Graphics graphics, int tabPlacement, int selectedIndex) {
            // Os painéis internos já pintam o próprio fundo. Pintar a área inteira aqui
            // cobre as abas em alguns temas do Swing.
        }
    }

    private class RoundedComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            return new ComboArrowButton();
        }

        @Override
        public void paint(Graphics graphics, JComponent component) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(fieldColor);
            graphics2D.fillRoundRect(0, 0, component.getWidth(), component.getHeight(), 12, 12);
            graphics2D.setColor(borderColor);
            graphics2D.drawRoundRect(0, 0, component.getWidth() - 1, component.getHeight() - 1, 12, 12);
            graphics2D.dispose();
            super.paint(graphics, component);
        }

        @Override
        public void paintCurrentValueBackground(Graphics graphics, Rectangle bounds, boolean hasFocus) {
            // O fundo arredondado do combo já é pintado em paint().
        }

        @Override
        public void paintCurrentValue(Graphics graphics, Rectangle bounds, boolean hasFocus) {
            Object value = comboBox.getSelectedItem();
            if (value == null) {
                return;
            }

            Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setFont(comboBox.getFont());
                graphics2D.setColor(textColor);
                java.awt.FontMetrics metrics = graphics2D.getFontMetrics();
                String text = clippedText(textoCombo(value), metrics, Math.max(0, bounds.width - 10));
                int x = bounds.x + 6;
                int y = bounds.y + ((bounds.height - metrics.getHeight()) / 2) + metrics.getAscent();
                graphics2D.drawString(text, x, y);
            } finally {
                graphics2D.dispose();
            }
        }

        private String clippedText(String text, java.awt.FontMetrics metrics, int availableWidth) {
            if (text == null || metrics.stringWidth(text) <= availableWidth) {
                return text == null ? "" : text;
            }

            String suffix = "...";
            int suffixWidth = metrics.stringWidth(suffix);
            StringBuilder clipped = new StringBuilder();
            for (int index = 0; index < text.length(); index++) {
                String next = clipped.toString() + text.charAt(index);
                if (metrics.stringWidth(next) + suffixWidth > availableWidth) {
                    break;
                }
                clipped.append(text.charAt(index));
            }
            return clipped + suffix;
        }
    }

    private class ThemedSpinnerUI extends BasicSpinnerUI {
        @Override
        protected Component createNextButton() {
            Component button = new SpinnerArrowButton(true);
            installNextButtonListeners(button);
            return button;
        }

        @Override
        protected Component createPreviousButton() {
            Component button = new SpinnerArrowButton(false);
            installPreviousButtonListeners(button);
            return button;
        }
    }

    private class ComboArrowButton extends JButton {
        private static final long serialVersionUID = 1L;

        ComboArrowButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(28, 24));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2 + 1;
            Polygon arrow = new Polygon(
                    new int[] {centerX - 5, centerX + 5, centerX},
                    new int[] {centerY - 3, centerY - 3, centerY + 4},
                    3
            );
            graphics2D.setColor(textColor);
            graphics2D.fill(arrow);
            graphics2D.dispose();
        }
    }

    private class SpinnerArrowButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final boolean up;

        SpinnerArrowButton(boolean up) {
            this.up = up;
            setOpaque(true);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createLineBorder(borderColor));
            setFocusPainted(false);
            setPreferredSize(new Dimension(18, 10));
            setBackground(fieldColor);
            setForeground(textColor);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(fieldColor);
            graphics2D.fillRect(0, 0, getWidth(), getHeight());

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            Polygon arrow;
            if (up) {
                arrow = new Polygon(
                        new int[] {centerX - 4, centerX + 4, centerX},
                        new int[] {centerY + 2, centerY + 2, centerY - 3},
                        3
                );
            } else {
                arrow = new Polygon(
                        new int[] {centerX - 4, centerX + 4, centerX},
                        new int[] {centerY - 2, centerY - 2, centerY + 3},
                        3
                );
            }

            graphics2D.setColor(textColor);
            graphics2D.fill(arrow);
            graphics2D.dispose();
        }
    }

    private class HeaderPanel extends JPanel {
        private final int radius;

        HeaderPanel(LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color start = darkMode ? new Color(0, 78, 40) : new Color(0, 128, 64);
            Color end = darkMode ? new Color(0, 12, 8) : new Color(0, 67, 38);
            graphics2D.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(LayoutManager layout, int radius) {
            super(layout);
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(getBackground());
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }

    private class RoundedButton extends JButton {
        private final boolean primary;

        RoundedButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
            atualizarTema();
        }

        void atualizarTema() {
            setBackground(primary ? accentColor : secondaryButtonColor);
            setForeground(primary ? Color.WHITE : textColor);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setColor(getModel().isPressed() ? getBackground().darker() : getBackground());
            graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            graphics2D.dispose();
            super.paintComponent(graphics);
        }
    }
}
