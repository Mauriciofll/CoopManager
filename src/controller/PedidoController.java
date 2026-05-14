package controller;

import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import repository.PedidoRepository;
import java.util.List;

public class PedidoController {
    private PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido criarPedido(Usuario cliente, String tipoEntrega) {
        return pedidoRepository.criarPedido(cliente, tipoEntrega);
    }

    public boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade) {
        if (produto == null || pedido == null) return false;
        if (quantidade <= 0 || quantidade > produto.getEstoque()) return false;

        pedido.adicionarItem(new ItemPedido(produto, quantidade));
        return true;
    }

    public boolean atualizarStatus(int pedidoId, String novoStatus) {
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId);
        if (pedido == null) return false;
        pedido.atualizarStatus(novoStatus);
        return true;
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.listarTodos();
    }
}
