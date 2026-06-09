package repository;

import database.DatabaseGateway;
import database.DatabaseProvider;
import model.Cliente;
import model.Pedido;
import model.Produto;

import java.time.LocalDate;
import java.util.List;

public class PedidoRepository {
    private final DatabaseGateway database = DatabaseProvider.getDatabase();

    public Pedido criarPedido(Cliente cliente, String tipoEntrega) {
        return criarPedido(cliente, tipoEntrega, null);
    }

    public Pedido criarPedido(Cliente cliente, String tipoEntrega, LocalDate dataPrevista) {
        return database.criarPedido(cliente, tipoEntrega, dataPrevista);
    }

    public boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade) {
        return database.adicionarProdutoAoPedido(pedido, produto, quantidade);
    }

    public boolean remover(int id) {
        return database.removerPedido(id);
    }

    public boolean atualizarStatus(int pedidoId, String novoStatus) {
        return database.atualizarStatusPedido(pedidoId, novoStatus);
    }

    public boolean atualizarStatusItem(int pedidoId, int produtoId, String novoStatus) {
        return database.atualizarStatusItemPedido(pedidoId, produtoId, novoStatus);
    }

    public boolean atualizarControleItem(int pedidoId, int produtoId, String novoStatus, int quantidadeEnviada, int quantidadeRecebida) {
        return database.atualizarControleItemPedido(pedidoId, produtoId, novoStatus, quantidadeEnviada, quantidadeRecebida);
    }

    public boolean cancelar(int pedidoId) {
        return database.cancelarPedido(pedidoId);
    }

    public boolean registrarHistorico(int pedidoId, String autor, String acao, String observacao) {
        return database.registrarHistoricoPedido(pedidoId, autor, acao, observacao);
    }

    public List<Pedido> listarTodos() {
        return database.listarPedidos();
    }

    public Pedido buscarPorId(int id) {
        return database.buscarPedidoPorId(id);
    }
}
