package database;

import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import model.Cliente;
import security.PasswordHasher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LocalDatabase implements DatabaseGateway, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String APPLICATION_DIRECTORY = "CoopManager";
    private static final String LEGACY_DATABASE_DIRECTORY = "data";
    private static final String DATABASE_FILE = "coopmanager.dat";
    private static LocalDatabase instance;

    private ArrayList<Usuario> usuarios = new ArrayList<>();
    private ArrayList<Cliente> clientes = new ArrayList<>();
    private ArrayList<Produto> produtos = new ArrayList<>();
    private ArrayList<Pedido> pedidos = new ArrayList<>();
    private int proximoUsuarioId = 1;
    private int proximoClienteId = 1;
    private int proximoProdutoId = 1;
    private int proximoPedidoId = 1;

    public static synchronized LocalDatabase getInstance() {
        if (instance == null) {
            instance = carregar();
        }
        return instance;
    }

    public synchronized Usuario salvarUsuario(String nome, String tipo, String login, String senha) {
        String loginNormalizado = normalizarLogin(login);
        validarLoginDisponivel(0, loginNormalizado);

        Usuario usuario = new Usuario(proximoUsuarioId++, nome, tipo, loginNormalizado, PasswordHasher.hash(senha));
        usuarios.add(usuario);
        salvar();
        return usuario;
    }

    public synchronized boolean atualizarUsuario(int id, String nome, String tipo, String login, String senha) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (usuario == null) {
            return false;
        }

        String loginNormalizado = normalizarLogin(login);
        validarLoginDisponivel(id, loginNormalizado);

        usuario.atualizarDados(nome, tipo, loginNormalizado);
        if (senha != null && !senha.isBlank()) {
            usuario.atualizarSenha(PasswordHasher.hash(senha));
        }

        salvar();
        return true;
    }

    public synchronized boolean atualizarTemaUsuario(int id, boolean temaEscuro) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (usuario == null) {
            return false;
        }

        usuario.atualizarTemaEscuro(temaEscuro);
        salvar();
        return true;
    }

    public synchronized List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios);
    }

    public synchronized Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }
        return null;
    }

    public synchronized boolean usuariosVazio() {
        return usuarios.isEmpty();
    }

    public synchronized boolean removerUsuario(int id) {
        Usuario usuario = buscarUsuarioPorId(id);
        if (usuario == null) {
            return false;
        }

        if ("PRODUTOR".equals(usuario.getTipo()) && produtorPossuiProdutoOuPedido(id)) {
            return false;
        }

        boolean removido = usuarios.remove(usuario);
        if (removido) {
            salvar();
        }
        return removido;
    }

    public synchronized Cliente salvarCliente(String nome, String email, String telefone) {
        Cliente cliente = new Cliente(proximoClienteId++, nome, email, telefone);
        clientes.add(cliente);
        salvar();
        return cliente;
    }

    public synchronized boolean atualizarCliente(int id, String nome, String email, String telefone) {
        Cliente cliente = buscarClientePorId(id);
        if (cliente == null) {
            return false;
        }

        cliente.atualizarDados(nome, "CLIENTE", "");
        cliente.atualizarContato(email, telefone);
        salvar();
        return true;
    }

    public synchronized List<Cliente> listarClientes() {
        return new ArrayList<>(clientes);
    }

    public synchronized Cliente buscarClientePorId(int id) {
        for (Cliente cliente : clientes) {
            if (cliente != null && cliente.getId() == id) {
                return cliente;
            }
        }
        return null;
    }

    public synchronized boolean removerCliente(int id) {
        Cliente cliente = buscarClientePorId(id);
        if (cliente == null) {
            return false;
        }

        if (clientePossuiPedido(id)) {
            return false;
        }

        boolean removido = clientes.remove(cliente);
        if (removido) {
            salvar();
        }
        return removido;
    }

    public synchronized Produto salvarProduto(String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        Produto produto = new Produto(proximoProdutoId++, nome, categoria, preco, estoque, estoqueMinimo, produtor);
        produtos.add(produto);
        salvar();
        return produto;
    }

    public synchronized List<Produto> listarProdutos() {
        return new ArrayList<>(produtos);
    }

    public synchronized List<Produto> listarProdutosPorProdutor(Usuario produtor) {
        List<Produto> produtosDoProdutor = new ArrayList<>();
        if (produtor == null) {
            return produtosDoProdutor;
        }

        for (Produto produto : produtos) {
            if (produto != null && produto.getProdutor() != null && produto.getProdutor().getId() == produtor.getId()) {
                produtosDoProdutor.add(produto);
            }
        }
        return produtosDoProdutor;
    }

    public synchronized Produto buscarProdutoPorId(int id) {
        for (Produto produto : produtos) {
            if (produto != null && produto.getId() == id) {
                return produto;
            }
        }
        return null;
    }

    public synchronized boolean atualizarProduto(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo) {
        Produto produto = buscarProdutoPorId(id);
        if (produto == null) {
            return false;
        }

        produto.atualizarDados(nome, categoria, preco, estoque, estoqueMinimo);
        salvar();
        return true;
    }

    public synchronized boolean removerProduto(int id) {
        Produto produto = buscarProdutoPorId(id);
        if (produto == null) {
            return false;
        }

        if (produtoPossuiPedido(id)) {
            return false;
        }

        boolean removido = produtos.remove(produto);
        if (removido) {
            salvar();
        }
        return removido;
    }

    public synchronized Pedido criarPedido(Cliente cliente, String tipoEntrega, LocalDate dataPrevista) {
        Pedido pedido = new Pedido(proximoPedidoId++, cliente, tipoEntrega, LocalDate.now(), dataPrevista);
        pedidos.add(pedido);
        salvar();
        return pedido;
    }

    public synchronized boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade) {
        if (pedido == null || produto == null) {
            return false;
        }

        Pedido pedidoSalvo = buscarPedidoPorId(pedido.getId());
        Produto produtoSalvo = buscarProdutoPorId(produto.getId());
        if (pedidoSalvo == null
                || "CANCELADO".equals(pedidoSalvo.getStatus())
                || produtoSalvo == null
                || quantidade <= 0
                || quantidade > produtoSalvo.getEstoque()) {
            return false;
        }

        pedidoSalvo.adicionarItem(new ItemPedido(produtoSalvo, quantidade));
        salvar();
        return true;
    }

    public synchronized boolean atualizarStatusPedido(int pedidoId, String novoStatus) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null) {
            return false;
        }

        pedido.atualizarStatus(novoStatus);
        salvar();
        return true;
    }

    public synchronized boolean atualizarStatusItemPedido(int pedidoId, int produtoId, String novoStatus) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null || "CANCELADO".equals(pedido.getStatus())) {
            return false;
        }

        boolean atualizado = pedido.atualizarStatusItem(produtoId, novoStatus);
        if (atualizado) {
            salvar();
        }
        return atualizado;
    }

    public synchronized boolean atualizarControleItemPedido(
            int pedidoId,
            int produtoId,
            String novoStatus,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null || "CANCELADO".equals(pedido.getStatus())) {
            return false;
        }

        boolean atualizado = pedido.atualizarControleItem(produtoId, novoStatus, quantidadeEnviada, quantidadeRecebida);
        if (atualizado) {
            salvar();
        }
        return atualizado;
    }

    public synchronized boolean cancelarPedido(int pedidoId) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null || "CANCELADO".equals(pedido.getStatus()) || "ENTREGUE".equals(pedido.getStatus())) {
            return false;
        }

        for (ItemPedido item : pedido.getItens()) {
            if (item == null || item.getProduto() == null) {
                continue;
            }

            Produto produtoSalvo = buscarProdutoPorId(item.getProduto().getId());
            if (produtoSalvo != null) {
                produtoSalvo.aumentarEstoque(item.getQuantidade());
            }
        }

        pedido.atualizarStatus("CANCELADO");
        pedido.marcarItensCancelados();
        salvar();
        return true;
    }

    public synchronized boolean registrarHistoricoPedido(int pedidoId, String autor, String acao, String observacao) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        if (pedido == null) {
            return false;
        }

        pedido.registrarHistorico(autor, acao, observacao);
        salvar();
        return true;
    }

    public synchronized List<Pedido> listarPedidos() {
        return new ArrayList<>(pedidos);
    }

    public synchronized Pedido buscarPedidoPorId(int id) {
        for (Pedido pedido : pedidos) {
            if (pedido != null && pedido.getId() == id) {
                return pedido;
            }
        }
        return null;
    }

    public synchronized boolean removerPedido(int id) {
        Pedido pedido = buscarPedidoPorId(id);
        if (pedido == null) {
            return false;
        }

        boolean removido = pedidos.remove(pedido);
        if (removido) {
            salvar();
        }
        return removido;
    }

    public static String getDatabasePath() {
        return getDatabaseFile().getPath();
    }

    private static LocalDatabase carregar() {
        File file = getDatabaseFile();
        migrarBancoLegado(file);

        if (!file.exists()) {
            return new LocalDatabase();
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            Object object = input.readObject();
            if (object instanceof LocalDatabase database) {
                database.normalizarDados();
                return database;
            }
        } catch (IOException | ClassNotFoundException exception) {
            System.err.println("Não foi possível carregar o banco local. Um novo arquivo será criado.");
        }
        return new LocalDatabase();
    }

    private synchronized void salvar() {
        File file = getDatabaseFile();
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(this);
        } catch (IOException exception) {
            throw new IllegalStateException("Erro ao salvar banco local.", exception);
        }
    }

    private static File getDatabaseFile() {
        return getDatabaseDirectory().resolve(DATABASE_FILE).toFile();
    }

    private static Path getDatabaseDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, APPLICATION_DIRECTORY);
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            return Path.of(userHome, "." + APPLICATION_DIRECTORY.toLowerCase());
        }

        return Path.of(LEGACY_DATABASE_DIRECTORY);
    }

    private static void migrarBancoLegado(File targetFile) {
        File legacyFile = new File(LEGACY_DATABASE_DIRECTORY, DATABASE_FILE);
        if (targetFile.exists() || !legacyFile.exists() || isSameFile(targetFile, legacyFile)) {
            return;
        }

        File directory = targetFile.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        try {
            Files.copy(legacyFile.toPath(), targetFile.toPath());
        } catch (IOException exception) {
            System.err.println("Não foi possível migrar o banco local antigo. O arquivo atual será mantido.");
        }
    }

    private static boolean isSameFile(File first, File second) {
        try {
            return first.getCanonicalFile().equals(second.getCanonicalFile());
        } catch (IOException exception) {
            return first.getAbsolutePath().equals(second.getAbsolutePath());
        }
    }

    private void normalizarDados() {
        if (usuarios == null) {
            usuarios = new ArrayList<>();
        }
        if (clientes == null) {
            clientes = new ArrayList<>();
        }
        if (produtos == null) {
            produtos = new ArrayList<>();
        }
        if (pedidos == null) {
            pedidos = new ArrayList<>();
        }

        for (Pedido pedido : pedidos) {
            if (pedido != null) {
                pedido.normalizarFluxoOperacional();
            }
        }

        migrarClientesAntigos();
        proximoUsuarioId = Math.max(proximoUsuarioId, proximoIdUsuario());
        proximoClienteId = Math.max(proximoClienteId, proximoIdCliente());
        proximoProdutoId = Math.max(proximoProdutoId, proximoIdProduto());
        proximoPedidoId = Math.max(proximoPedidoId, proximoIdPedido());
    }

    private void migrarClientesAntigos() {
        ArrayList<Usuario> usuariosDeAcesso = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            if (usuario == null) {
                continue;
            }

            if ("CLIENTE".equals(usuario.getTipo())) {
                garantirCliente(usuario.getId(), usuario.getNome());
            } else {
                usuariosDeAcesso.add(usuario);
            }
        }
        usuarios = usuariosDeAcesso;

        for (Pedido pedido : pedidos) {
            if (pedido == null || pedido.getCliente() == null) {
                continue;
            }

            Usuario clienteAntigo = pedido.getCliente();
            Cliente cliente = clienteAntigo instanceof Cliente
                    ? (Cliente) clienteAntigo
                    : garantirCliente(clienteAntigo.getId(), clienteAntigo.getNome());
            pedido.atualizarCliente(cliente);
        }
    }

    private Cliente garantirCliente(int id, String nome) {
        Cliente cliente = buscarClientePorId(id);
        if (cliente != null) {
            return cliente;
        }

        Cliente novoCliente = new Cliente(id, nome);
        clientes.add(novoCliente);
        return novoCliente;
    }

    private int proximoIdUsuario() {
        int maiorId = 0;
        for (Usuario usuario : usuarios) {
            if (usuario != null && usuario.getId() > maiorId) {
                maiorId = usuario.getId();
            }
        }
        return maiorId + 1;
    }

    private int proximoIdProduto() {
        int maiorId = 0;
        for (Produto produto : produtos) {
            if (produto != null && produto.getId() > maiorId) {
                maiorId = produto.getId();
            }
        }
        return maiorId + 1;
    }

    private int proximoIdCliente() {
        int maiorId = 0;
        for (Cliente cliente : clientes) {
            if (cliente != null && cliente.getId() > maiorId) {
                maiorId = cliente.getId();
            }
        }
        return maiorId + 1;
    }

    private int proximoIdPedido() {
        int maiorId = 0;
        for (Pedido pedido : pedidos) {
            if (pedido != null && pedido.getId() > maiorId) {
                maiorId = pedido.getId();
            }
        }
        return maiorId + 1;
    }

    private boolean produtorPossuiProdutoOuPedido(int produtorId) {
        for (Produto produto : produtos) {
            Usuario produtor = produto == null ? null : produto.getProdutor();
            if (produtor != null && produtor.getId() == produtorId) {
                return true;
            }
        }

        for (Pedido pedido : pedidos) {
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

    private boolean clientePossuiPedido(int clienteId) {
        for (Pedido pedido : pedidos) {
            Usuario cliente = pedido == null ? null : pedido.getCliente();
            if (cliente != null && cliente.getId() == clienteId) {
                return true;
            }
        }
        return false;
    }

    private boolean produtoPossuiPedido(int produtoId) {
        for (Pedido pedido : pedidos) {
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

    private String normalizarLogin(String login) {
        return login == null ? "" : login.trim();
    }

    private void validarLoginDisponivel(int usuarioId, String login) {
        if (login == null || login.isBlank()) {
            return;
        }

        for (Usuario usuario : usuarios) {
            if (usuario != null
                    && usuario.getId() != usuarioId
                    && login.equalsIgnoreCase(usuario.getLogin())) {
                throw new IllegalArgumentException("Já existe um usuário com este login.");
            }
        }
    }
}
