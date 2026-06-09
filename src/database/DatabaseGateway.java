package database;

import model.Pedido;
import model.Produto;
import model.Usuario;
import model.Cliente;

import java.time.LocalDate;
import java.util.List;

public interface DatabaseGateway {
    Usuario salvarUsuario(String nome, String tipo, String login, String senha);

    boolean atualizarUsuario(int id, String nome, String tipo, String login, String senha);

    boolean atualizarTemaUsuario(int id, boolean temaEscuro);

    List<Usuario> listarUsuarios();

    Usuario buscarUsuarioPorId(int id);

    boolean usuariosVazio();

    boolean removerUsuario(int id);

    Cliente salvarCliente(String nome, String email, String telefone);

    boolean atualizarCliente(int id, String nome, String email, String telefone);

    List<Cliente> listarClientes();

    Cliente buscarClientePorId(int id);

    boolean removerCliente(int id);

    Produto salvarProduto(String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor);

    List<Produto> listarProdutos();

    List<Produto> listarProdutosPorProdutor(Usuario produtor);

    Produto buscarProdutoPorId(int id);

    boolean atualizarProduto(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo);

    boolean removerProduto(int id);

    Pedido criarPedido(Cliente cliente, String tipoEntrega, LocalDate dataPrevista);

    boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade);

    boolean atualizarStatusPedido(int pedidoId, String novoStatus);

    boolean atualizarStatusItemPedido(int pedidoId, int produtoId, String novoStatus);

    boolean atualizarControleItemPedido(int pedidoId, int produtoId, String novoStatus, int quantidadeEnviada, int quantidadeRecebida);

    boolean cancelarPedido(int pedidoId);

    boolean registrarHistoricoPedido(int pedidoId, String autor, String acao, String observacao);

    List<Pedido> listarPedidos();

    Pedido buscarPedidoPorId(int id);

    boolean removerPedido(int id);
}
