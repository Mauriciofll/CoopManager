package database;

import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import model.Cliente;
import security.PasswordHasher;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PostgresDatabase implements DatabaseGateway {
    private final DatabaseConfig config;

    public PostgresDatabase(DatabaseConfig config) {
        this.config = config;
        carregarDriver();
        criarSchema();
    }

    @Override
    public Usuario salvarUsuario(String nome, String tipo, String login, String senha) {
        String sql = """
                INSERT INTO usuarios (nome, tipo, login, senha_hash)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        String senhaHash = PasswordHasher.hash(senha);

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, tipo);
            setNullableString(statement, 3, blankToNull(login));
            statement.setString(4, senhaHash);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Usuario(result.getInt("id"), nome, tipo, nullToEmpty(login), senhaHash);
                }
            }
            throw new IllegalStateException("Não foi possível salvar usuário no PostgreSQL.");
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarUsuario(int id, String nome, String tipo, String login, String senha) {
        boolean atualizarSenha = senha != null && !senha.isBlank();
        String sql = atualizarSenha
                ? """
                UPDATE usuarios
                SET nome = ?, tipo = ?, login = ?, senha_hash = ?
                WHERE id = ?
                """
                : """
                UPDATE usuarios
                SET nome = ?, tipo = ?, login = ?
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, tipo);
            setNullableString(statement, 3, blankToNull(login));

            if (atualizarSenha) {
                statement.setString(4, PasswordHasher.hash(senha));
                statement.setInt(5, id);
            } else {
                statement.setInt(4, id);
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarTemaUsuario(int id, boolean temaEscuro) {
        String sql = """
                UPDATE usuarios
                SET tema_escuro = ?
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, temaEscuro);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<Usuario> listarUsuarios() {
        String sql = """
                SELECT id, nome, tipo, login, senha_hash, tema_escuro
                FROM usuarios
                WHERE tipo <> 'CLIENTE'
                ORDER BY id
                """;
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                usuarios.add(usuarioFromResult(result, ""));
            }
            return usuarios;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Usuario buscarUsuarioPorId(int id) {
        String sql = """
                SELECT id, nome, tipo, login, senha_hash, tema_escuro
                FROM usuarios
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return usuarioFromResult(result, "");
                }
            }
            return null;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean usuariosVazio() {
        String sql = "SELECT COUNT(*) AS total FROM usuarios WHERE tipo <> 'CLIENTE'";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next() && result.getInt("total") == 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean removerUsuario(int id) {
        String sql = """
                DELETE FROM usuarios u
                WHERE u.id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM produtos p
                      WHERE p.produtor_id = u.id
                  )
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Cliente salvarCliente(String nome, String email, String telefone) {
        String sql = """
                INSERT INTO clientes (nome, email, telefone)
                VALUES (?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, blankToEmpty(email));
            statement.setString(3, blankToEmpty(telefone));
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Cliente(result.getInt("id"), nome, email, telefone);
                }
            }
            throw new IllegalStateException("Não foi possível salvar cliente no PostgreSQL.");
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarCliente(int id, String nome, String email, String telefone) {
        String sql = "UPDATE clientes SET nome = ?, email = ?, telefone = ? WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, blankToEmpty(email));
            statement.setString(3, blankToEmpty(telefone));
            statement.setInt(4, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<Cliente> listarClientes() {
        String sql = """
                SELECT id, nome, email, telefone
                FROM clientes
                ORDER BY id
                """;
        List<Cliente> clientes = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                clientes.add(clienteFromResult(result, ""));
            }
            return clientes;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Cliente buscarClientePorId(int id) {
        String sql = """
                SELECT id, nome, email, telefone
                FROM clientes
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return clienteFromResult(result, "");
                }
            }
            return null;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean removerCliente(int id) {
        String sql = """
                DELETE FROM clientes c
                WHERE c.id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM pedidos pe
                      WHERE pe.cliente_id = c.id
                  )
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Produto salvarProduto(String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        String sql = """
                INSERT INTO produtos (nome, categoria, preco, estoque, estoque_minimo, produtor_id)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, categoria);
            statement.setDouble(3, preco);
            statement.setInt(4, estoque);
            statement.setInt(5, estoqueMinimo);
            setNullableInt(statement, 6, produtor == null ? null : produtor.getId());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Produto(result.getInt("id"), nome, categoria, preco, estoque, estoqueMinimo, produtor);
                }
            }
            throw new IllegalStateException("Não foi possível salvar produto no PostgreSQL.");
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<Produto> listarProdutos() {
        String sql = produtoSelect() + " ORDER BY p.id";
        return listarProdutosPorSql(sql, null);
    }

    @Override
    public List<Produto> listarProdutosPorProdutor(Usuario produtor) {
        if (produtor == null) {
            return new ArrayList<>();
        }

        String sql = produtoSelect() + " WHERE p.produtor_id = ? ORDER BY p.id";
        return listarProdutosPorSql(sql, produtor.getId());
    }

    @Override
    public Produto buscarProdutoPorId(int id) {
        String sql = produtoSelect() + " WHERE p.id = ?";
        List<Produto> produtos = listarProdutosPorSql(sql, id);
        return produtos.isEmpty() ? null : produtos.get(0);
    }

    @Override
    public boolean atualizarProduto(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo) {
        String sql = """
                UPDATE produtos
                SET nome = ?, categoria = ?, preco = ?, estoque = ?, estoque_minimo = ?
                WHERE id = ?
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, categoria);
            statement.setDouble(3, preco);
            statement.setInt(4, estoque);
            statement.setInt(5, estoqueMinimo);
            statement.setInt(6, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean removerProduto(int id) {
        String sql = """
                DELETE FROM produtos p
                WHERE p.id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM pedido_itens pi
                      WHERE pi.produto_id = p.id
                  )
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Pedido criarPedido(Cliente cliente, String tipoEntrega, LocalDate dataPrevista) {
        String sql = """
                INSERT INTO pedidos (cliente_id, status, tipo_entrega, data_pedido, data_prevista)
                VALUES (?, 'AGUARDANDO_PRODUTORES', ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setNullableInt(statement, 1, cliente == null ? null : cliente.getId());
            statement.setString(2, tipoEntrega);
            statement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            setNullableDate(statement, 4, dataPrevista);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new Pedido(result.getInt("id"), cliente, tipoEntrega, LocalDate.now(), dataPrevista);
                }
            }
            throw new IllegalStateException("Não foi possível criar pedido no PostgreSQL.");
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade) {
        if (pedido == null || produto == null || quantidade <= 0) {
            return false;
        }

        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                String status = statusPedido(connection, pedido.getId());
                if (status == null || "CANCELADO".equals(status)) {
                    connection.rollback();
                    return false;
                }

                int estoque = estoqueProduto(connection, produto.getId());
                if (quantidade > estoque) {
                    connection.rollback();
                    return false;
                }

                atualizarEstoque(connection, produto.getId(), estoque - quantidade);
                inserirItemPedido(connection, pedido.getId(), produto.getId(), quantidade);
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarStatusPedido(int pedidoId, String novoStatus) {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ? AND status <> 'CANCELADO'";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, novoStatus);
            statement.setInt(2, pedidoId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarStatusItemPedido(int pedidoId, int produtoId, String novoStatus) {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                ItemFluxo fluxo = fluxoItemPedido(connection, pedidoId, produtoId);
                if (fluxo == null) {
                    connection.rollback();
                    return false;
                }
                boolean atualizado = atualizarControleItemPedido(
                        connection,
                        pedidoId,
                        produtoId,
                        novoStatus,
                        fluxo.quantidadeEnviada,
                        fluxo.quantidadeRecebida
                );
                connection.commit();
                return atualizado;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean atualizarControleItemPedido(
            int pedidoId,
            int produtoId,
            String novoStatus,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                boolean atualizado = atualizarControleItemPedido(
                        connection,
                        pedidoId,
                        produtoId,
                        novoStatus,
                        quantidadeEnviada,
                        quantidadeRecebida
                );
                connection.commit();
                return atualizado;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean cancelarPedido(int pedidoId) {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                String status = statusPedido(connection, pedidoId);
                if (status == null || "CANCELADO".equals(status) || "ENTREGUE".equals(status)) {
                    connection.rollback();
                    return false;
                }

                devolverItensAoEstoque(connection, pedidoId);
                atualizarStatusCancelado(connection, pedidoId);
                atualizarStatusItensCancelados(connection, pedidoId);
                connection.commit();
                return true;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean registrarHistoricoPedido(int pedidoId, String autor, String acao, String observacao) {
        String sql = """
                INSERT INTO pedido_historico (pedido_id, autor, acao, observacao)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            statement.setString(2, autor == null || autor.isBlank() ? "Sistema" : autor.trim());
            statement.setString(3, acao == null || acao.isBlank() ? "Atualização" : acao.trim());
            setNullableString(statement, 4, blankToNull(observacao));
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public List<Pedido> listarPedidos() {
        String sql = pedidoSelect() + " ORDER BY pe.data_pedido DESC, pe.id DESC";
        List<Pedido> pedidos = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Pedido pedido = pedidoFromResult(connection, result);
                pedidos.add(pedido);
            }
            return pedidos;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public Pedido buscarPedidoPorId(int id) {
        String sql = pedidoSelect() + " WHERE pe.id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return pedidoFromResult(connection, result);
                }
            }
            return null;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    @Override
    public boolean removerPedido(int id) {
        String sql = "DELETE FROM pedidos WHERE id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    private List<Produto> listarProdutosPorSql(String sql, Integer parameter) {
        List<Produto> produtos = new ArrayList<>();

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setInt(1, parameter);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Produto produto = produtoFromResult(result);
                    if (produto != null) {
                        produtos.add(produto);
                    }
                }
            }
            return produtos;
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    private Pedido pedidoFromResult(Connection connection, ResultSet result) throws SQLException {
        Cliente cliente = clienteFromResult(result, "cliente_");
        Pedido pedido = new Pedido(
                result.getInt("pedido_id"),
                cliente,
                result.getString("pedido_tipo_entrega"),
                nullableLocalDate(result, "pedido_data_pedido"),
                nullableLocalDate(result, "pedido_data_prevista")
        );
        pedido.atualizarStatus(result.getString("pedido_status"));

        for (ItemPedido item : itensDoPedido(connection, pedido.getId())) {
            pedido.adicionarItemSalvo(item);
        }
        for (String registro : historicoDoPedido(connection, pedido.getId())) {
            pedido.adicionarHistoricoSalvo(registro);
        }
        return pedido;
    }

    private List<String> historicoDoPedido(Connection connection, int pedidoId) throws SQLException {
        String sql = """
                SELECT registrado_em, autor, acao, observacao
                FROM pedido_historico
                WHERE pedido_id = ?
                ORDER BY registrado_em, id
                """;
        List<String> historico = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String registro = result.getTimestamp("registrado_em").toLocalDateTime().format(formatter)
                            + " | " + result.getString("autor")
                            + " | " + result.getString("acao");
                    String observacao = nullToEmpty(result.getString("observacao"));
                    if (!observacao.isBlank()) {
                        registro += " | " + observacao;
                    }
                    historico.add(registro);
                }
            }
        }
        return historico;
    }

    private List<ItemPedido> itensDoPedido(Connection connection, int pedidoId) throws SQLException {
        String sql = """
                SELECT
                    pi.quantidade AS item_quantidade,
                    pi.quantidade_enviada AS item_quantidade_enviada,
                    pi.quantidade_recebida AS item_quantidade_recebida,
                    pi.status AS item_status,
                    p.id AS produto_id,
                    p.nome AS produto_nome,
                    p.categoria AS produto_categoria,
                    p.preco AS produto_preco,
                    p.estoque AS produto_estoque,
                    p.estoque_minimo AS produto_estoque_minimo,
                    u.id AS produtor_id,
                    u.nome AS produtor_nome,
                    u.tipo AS produtor_tipo,
                    u.login AS produtor_login,
                    u.senha_hash AS produtor_senha_hash,
                    u.tema_escuro AS produtor_tema_escuro
                FROM pedido_itens pi
                LEFT JOIN produtos p ON p.id = pi.produto_id
                LEFT JOIN usuarios u ON u.id = p.produtor_id
                WHERE pi.pedido_id = ?
                ORDER BY pi.id
                """;
        List<ItemPedido> itens = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Produto produto = produtoFromItemResult(result);
                    itens.add(new ItemPedido(
                            produto,
                            result.getInt("item_quantidade"),
                            result.getString("item_status"),
                            result.getInt("item_quantidade_enviada"),
                            result.getInt("item_quantidade_recebida")
                    ));
                }
            }
        }
        return itens;
    }

    private ItemFluxo fluxoItemPedido(Connection connection, int pedidoId, int produtoId) throws SQLException {
        String sql = """
                SELECT quantidade, quantidade_enviada, quantidade_recebida, status
                FROM pedido_itens
                WHERE pedido_id = ? AND produto_id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            statement.setInt(2, produtoId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new ItemFluxo(
                            result.getInt("quantidade"),
                            result.getInt("quantidade_enviada"),
                            result.getInt("quantidade_recebida"),
                            result.getString("status")
                    );
                }
            }
        }
        return null;
    }

    private boolean atualizarControleItemPedido(
            Connection connection,
            int pedidoId,
            int produtoId,
            String novoStatus,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) throws SQLException {
        String statusPedido = statusPedido(connection, pedidoId);
        if (statusPedido == null || "CANCELADO".equals(statusPedido)) {
            return false;
        }

        ItemFluxo fluxo = fluxoItemPedido(connection, pedidoId, produtoId);
        if (fluxo == null) {
            return false;
        }

        ItemFluxo normalizado = normalizarFluxoItem(
                fluxo.quantidade,
                novoStatus,
                quantidadeEnviada,
                quantidadeRecebida
        );
        String sql = """
                UPDATE pedido_itens
                SET status = ?, quantidade_enviada = ?, quantidade_recebida = ?
                WHERE pedido_id = ? AND produto_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizado.status);
            statement.setInt(2, normalizado.quantidadeEnviada);
            statement.setInt(3, normalizado.quantidadeRecebida);
            statement.setInt(4, pedidoId);
            statement.setInt(5, produtoId);
            boolean atualizado = statement.executeUpdate() > 0;
            if (atualizado) {
                recalcularStatusPedido(connection, pedidoId);
            }
            return atualizado;
        }
    }

    private ItemFluxo normalizarFluxoItem(
            int quantidade,
            String status,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) {
        int total = Math.max(0, quantidade);
        String statusNormalizado = status == null || status.isBlank() ? "PENDENTE" : status;
        int recebida = Math.max(0, Math.min(quantidadeRecebida, total));
        int enviada = Math.max(Math.max(0, Math.min(quantidadeEnviada, total)), recebida);

        if ("ENTREGUE_COOPERATIVA".equals(statusNormalizado)) {
            enviada = total;
            recebida = total;
        } else if ("ENVIADO".equals(statusNormalizado) && enviada == 0) {
            enviada = total;
        } else if ("INDISPONIVEL".equals(statusNormalizado) || "CANCELADO".equals(statusNormalizado)) {
            enviada = 0;
            recebida = 0;
        } else if (recebida >= total && total > 0) {
            statusNormalizado = "ENTREGUE_COOPERATIVA";
            enviada = total;
            recebida = total;
        } else if (enviada > 0) {
            statusNormalizado = "ENVIADO";
        }

        return new ItemFluxo(total, enviada, recebida, statusNormalizado);
    }

    private boolean pedidoExiste(Connection connection, int pedidoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM pedidos WHERE id = ?")) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String statusPedido(Connection connection, int pedidoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT status FROM pedidos WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("status");
                }
            }
        }
        return null;
    }

    private int estoqueProduto(Connection connection, int produtoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT estoque FROM produtos WHERE id = ? FOR UPDATE")) {
            statement.setInt(1, produtoId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("estoque");
                }
            }
        }
        return -1;
    }

    private void atualizarEstoque(Connection connection, int produtoId, int novoEstoque) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE produtos SET estoque = ? WHERE id = ?")) {
            statement.setInt(1, novoEstoque);
            statement.setInt(2, produtoId);
            statement.executeUpdate();
        }
    }

    private void devolverItensAoEstoque(Connection connection, int pedidoId) throws SQLException {
        String sql = """
                SELECT produto_id, quantidade
                FROM pedido_itens
                WHERE pedido_id = ? AND produto_id IS NOT NULL
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    somarEstoque(connection, result.getInt("produto_id"), result.getInt("quantidade"));
                }
            }
        }
    }

    private void somarEstoque(Connection connection, int produtoId, int quantidade) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE produtos SET estoque = estoque + ? WHERE id = ?")) {
            statement.setInt(1, quantidade);
            statement.setInt(2, produtoId);
            statement.executeUpdate();
        }
    }

    private void atualizarStatusCancelado(Connection connection, int pedidoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pedidos SET status = 'CANCELADO' WHERE id = ?")) {
            statement.setInt(1, pedidoId);
            statement.executeUpdate();
        }
    }

    private void atualizarStatusItensCancelados(Connection connection, int pedidoId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pedido_itens SET status = 'CANCELADO' WHERE pedido_id = ?")) {
            statement.setInt(1, pedidoId);
            statement.executeUpdate();
        }
    }

    private void recalcularStatusPedido(Connection connection, int pedidoId) throws SQLException {
        String statusAtual = statusPedido(connection, pedidoId);
        if (statusAtual == null || "CANCELADO".equals(statusAtual) || "ENTREGUE".equals(statusAtual)) {
            return;
        }

        List<ItemFluxo> itens = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT quantidade, quantidade_enviada, quantidade_recebida, status
                FROM pedido_itens
                WHERE pedido_id = ?
                """)) {
            statement.setInt(1, pedidoId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    itens.add(new ItemFluxo(
                            result.getInt("quantidade"),
                            result.getInt("quantidade_enviada"),
                            result.getInt("quantidade_recebida"),
                            nullToEmpty(result.getString("status"))
                    ));
                }
            }
        }

        String novoStatus = statusOperacional(itens);
        try (PreparedStatement statement = connection.prepareStatement("UPDATE pedidos SET status = ? WHERE id = ? AND status <> 'CANCELADO'")) {
            statement.setString(1, novoStatus);
            statement.setInt(2, pedidoId);
            statement.executeUpdate();
        }
    }

    private String statusOperacional(List<ItemFluxo> itens) {
        if (itens.isEmpty()) {
            return "AGUARDANDO_PRODUTORES";
        }

        boolean algumRecebido = false;
        boolean todosRecebidos = true;
        boolean algumIndisponivel = false;
        boolean algumEnviado = false;
        for (ItemFluxo item : itens) {
            String statusItem = item == null ? "PENDENTE" : nullToEmpty(item.status);
            if ("INDISPONIVEL".equals(statusItem)) {
                algumIndisponivel = true;
            }
            int quantidade = item == null ? 0 : item.quantidade;
            int quantidadeEnviada = item == null ? 0 : item.quantidadeEnviada;
            int quantidadeRecebida = item == null ? 0 : item.quantidadeRecebida;
            if (quantidadeRecebida > 0 || "ENTREGUE_COOPERATIVA".equals(statusItem)) {
                algumRecebido = true;
            }
            if (quantidadeEnviada > 0 || "ENVIADO".equals(statusItem) || "CONFIRMADO".equals(statusItem)) {
                algumEnviado = true;
            }
            if (quantidade <= 0 || quantidadeRecebida < quantidade) {
                todosRecebidos = false;
            }
        }

        if (algumIndisponivel) {
            return "PENDENCIA";
        }
        if (todosRecebidos) {
            return "EM_SEPARACAO";
        }
        if (algumRecebido) {
            return "PARCIALMENTE_RECEBIDO";
        }
        if (algumEnviado) {
            return "AGUARDANDO_RECEBIMENTO";
        }
        return "AGUARDANDO_PRODUTORES";
    }

    private void inserirItemPedido(Connection connection, int pedidoId, int produtoId, int quantidade) throws SQLException {
        String sql = """
                INSERT INTO pedido_itens (pedido_id, produto_id, quantidade, status)
                VALUES (?, ?, ?, 'PENDENTE')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pedidoId);
            statement.setInt(2, produtoId);
            statement.setInt(3, quantidade);
            statement.executeUpdate();
        }
    }

    private String produtoSelect() {
        return """
                SELECT
                    p.id AS produto_id,
                    p.nome AS produto_nome,
                    p.categoria AS produto_categoria,
                    p.preco AS produto_preco,
                    p.estoque AS produto_estoque,
                    p.estoque_minimo AS produto_estoque_minimo,
                    u.id AS produtor_id,
                    u.nome AS produtor_nome,
                    u.tipo AS produtor_tipo,
                    u.login AS produtor_login,
                    u.senha_hash AS produtor_senha_hash,
                    u.tema_escuro AS produtor_tema_escuro
                FROM produtos p
                LEFT JOIN usuarios u ON u.id = p.produtor_id
                """;
    }

    private String pedidoSelect() {
        return """
                SELECT
                    pe.id AS pedido_id,
                    pe.status AS pedido_status,
                    pe.tipo_entrega AS pedido_tipo_entrega,
                    pe.data_pedido AS pedido_data_pedido,
                    pe.data_prevista AS pedido_data_prevista,
                    c.id AS cliente_id,
                    c.nome AS cliente_nome,
                    c.email AS cliente_email,
                    c.telefone AS cliente_telefone
                FROM pedidos pe
                LEFT JOIN clientes c ON c.id = pe.cliente_id
                """;
    }

    private Produto produtoFromResult(ResultSet result) throws SQLException {
        int id = result.getInt("produto_id");
        if (result.wasNull()) {
            return null;
        }

        Usuario produtor = usuarioFromResult(result, "produtor_");
        return new Produto(
                id,
                result.getString("produto_nome"),
                result.getString("produto_categoria"),
                result.getDouble("produto_preco"),
                result.getInt("produto_estoque"),
                result.getInt("produto_estoque_minimo"),
                produtor
        );
    }

    private Produto produtoFromItemResult(ResultSet result) throws SQLException {
        return produtoFromResult(result);
    }

    private Usuario usuarioFromResult(ResultSet result, String prefix) throws SQLException {
        int id = result.getInt(prefix + "id");
        if (result.wasNull()) {
            return null;
        }

        return new Usuario(
                id,
                result.getString(prefix + "nome"),
                result.getString(prefix + "tipo"),
                nullToEmpty(result.getString(prefix + "login")),
                nullToEmpty(result.getString(prefix + "senha_hash")),
                result.getBoolean(prefix + "tema_escuro")
        );
    }

    private Cliente clienteFromResult(ResultSet result, String prefix) throws SQLException {
        int id = result.getInt(prefix + "id");
        if (result.wasNull()) {
            return null;
        }

        return new Cliente(
                id,
                result.getString(prefix + "nome"),
                nullToEmpty(result.getString(prefix + "email")),
                nullToEmpty(result.getString(prefix + "telefone"))
        );
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    private void criarSchema() {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id SERIAL PRIMARY KEY,
                        nome VARCHAR(120) NOT NULL,
                        tipo VARCHAR(30) NOT NULL,
                        login VARCHAR(80),
                        senha_hash TEXT,
                        tema_escuro BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);
            statement.execute("""
                    ALTER TABLE usuarios
                    ADD COLUMN IF NOT EXISTS tema_escuro BOOLEAN NOT NULL DEFAULT FALSE
                    """);
            statement.execute("""
                    CREATE UNIQUE INDEX IF NOT EXISTS usuarios_login_unico
                    ON usuarios (LOWER(login))
                    WHERE login IS NOT NULL
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS clientes (
                        id SERIAL PRIMARY KEY,
                        nome VARCHAR(120) NOT NULL,
                        email VARCHAR(160) NOT NULL DEFAULT '',
                        telefone VARCHAR(40) NOT NULL DEFAULT ''
                    )
                    """);
            statement.execute("""
                    ALTER TABLE clientes
                    ADD COLUMN IF NOT EXISTS email VARCHAR(160) NOT NULL DEFAULT ''
                    """);
            statement.execute("""
                    ALTER TABLE clientes
                    ADD COLUMN IF NOT EXISTS telefone VARCHAR(40) NOT NULL DEFAULT ''
                    """);
            statement.execute("""
                    INSERT INTO clientes (id, nome, email, telefone)
                    SELECT id, nome, '', ''
                    FROM usuarios
                    WHERE tipo = 'CLIENTE'
                    ON CONFLICT (id) DO NOTHING
                    """);
            statement.execute("""
                    SELECT setval(
                        pg_get_serial_sequence('clientes', 'id'),
                        GREATEST(COALESCE((SELECT MAX(id) FROM clientes), 0), 1),
                        true
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS produtos (
                        id SERIAL PRIMARY KEY,
                        nome VARCHAR(160) NOT NULL,
                        categoria VARCHAR(60) NOT NULL,
                        preco NUMERIC(12, 2) NOT NULL,
                        estoque INTEGER NOT NULL DEFAULT 0,
                        estoque_minimo INTEGER NOT NULL DEFAULT 0,
                        produtor_id INTEGER REFERENCES usuarios(id) ON DELETE SET NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pedidos (
                        id SERIAL PRIMARY KEY,
                        cliente_id INTEGER REFERENCES clientes(id) ON DELETE SET NULL,
                        status VARCHAR(40) NOT NULL,
                        tipo_entrega VARCHAR(40) NOT NULL,
                        data_pedido DATE NOT NULL DEFAULT CURRENT_DATE,
                        data_prevista DATE,
                        criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            statement.execute("""
                    ALTER TABLE pedidos
                    ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    """);
            statement.execute("""
                    ALTER TABLE pedidos
                    ADD COLUMN IF NOT EXISTS data_pedido DATE
                    """);
            statement.execute("""
                    UPDATE pedidos
                    SET data_pedido = COALESCE(data_pedido, criado_em::date, CURRENT_DATE)
                    WHERE data_pedido IS NULL
                    """);
            statement.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN data_pedido SET DEFAULT CURRENT_DATE
                    """);
            statement.execute("""
                    ALTER TABLE pedidos
                    ALTER COLUMN data_pedido SET NOT NULL
                    """);
            statement.execute("""
                    ALTER TABLE pedidos
                    ADD COLUMN IF NOT EXISTS data_prevista DATE
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pedido_itens (
                        id SERIAL PRIMARY KEY,
                        pedido_id INTEGER NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
                        produto_id INTEGER REFERENCES produtos(id) ON DELETE SET NULL,
                        quantidade INTEGER NOT NULL CHECK (quantidade > 0),
                        status VARCHAR(40) NOT NULL DEFAULT 'PENDENTE'
                    )
                    """);
            statement.execute("""
                    ALTER TABLE pedido_itens
                    ADD COLUMN IF NOT EXISTS status VARCHAR(40) NOT NULL DEFAULT 'PENDENTE'
                    """);
            statement.execute("""
                    ALTER TABLE pedido_itens
                    ADD COLUMN IF NOT EXISTS quantidade_enviada INTEGER NOT NULL DEFAULT 0
                    """);
            statement.execute("""
                    ALTER TABLE pedido_itens
                    ADD COLUMN IF NOT EXISTS quantidade_recebida INTEGER NOT NULL DEFAULT 0
                    """);
            statement.execute("""
                    UPDATE pedido_itens
                    SET quantidade_enviada = quantidade,
                        quantidade_recebida = quantidade
                    WHERE status = 'ENTREGUE_COOPERATIVA'
                      AND quantidade_recebida = 0
                    """);
            statement.execute("""
                    UPDATE pedido_itens
                    SET quantidade_enviada = quantidade
                    WHERE status = 'ENVIADO'
                      AND quantidade_enviada = 0
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS pedido_historico (
                        id SERIAL PRIMARY KEY,
                        pedido_id INTEGER NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
                        registrado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        autor VARCHAR(120) NOT NULL,
                        acao VARCHAR(160) NOT NULL,
                        observacao TEXT
                    )
                    """);
            statement.execute("""
                    UPDATE pedidos
                    SET status = 'AGUARDANDO_PRODUTORES'
                    WHERE status = 'PENDENTE'
                    """);
        } catch (SQLException exception) {
            throw databaseException(exception);
        }
    }

    private void carregarDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            String origem = config.getSourcePath() == null ? "config/database.properties" : config.getSourcePath().toString();
            throw new IllegalStateException(
                    "Driver PostgreSQL não encontrado. Coloque o postgresql-*.jar na pasta lib/ e confira " + origem + ".",
                    exception
            );
        }
    }

    private IllegalStateException databaseException(SQLException exception) {
        return new IllegalStateException("Erro ao acessar o PostgreSQL: " + exception.getMessage(), exception);
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DATE);
        } else {
            statement.setDate(index, java.sql.Date.valueOf(value));
        }
    }

    private LocalDate nullableLocalDate(ResultSet result, String column) throws SQLException {
        java.sql.Date date = result.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class ItemFluxo {
        private final int quantidade;
        private final int quantidadeEnviada;
        private final int quantidadeRecebida;
        private final String status;

        private ItemFluxo(int quantidade, int quantidadeEnviada, int quantidadeRecebida, String status) {
            this.quantidade = quantidade;
            this.quantidadeEnviada = quantidadeEnviada;
            this.quantidadeRecebida = quantidadeRecebida;
            this.status = status;
        }
    }
}
