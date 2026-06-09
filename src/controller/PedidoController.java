package controller;

import model.Cliente;
import model.Pedido;
import model.Produto;
import repository.PedidoRepository;
import java.time.LocalDate;
import java.util.List;

public class PedidoController {
    private PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido criarPedido(Cliente cliente, String tipoEntrega) {
        return criarPedido(cliente, tipoEntrega, null);
    }

    public Pedido criarPedido(Cliente cliente, String tipoEntrega, LocalDate dataPrevista) {
        return pedidoRepository.criarPedido(cliente, tipoEntrega, dataPrevista);
    }

    public boolean adicionarProdutoAoPedido(Pedido pedido, Produto produto, int quantidade) {
        if (produto == null || pedido == null) return false;
        if (quantidade <= 0 || quantidade > produto.getEstoque()) return false;

        return pedidoRepository.adicionarProdutoAoPedido(pedido, produto, quantidade);
    }

    public boolean atualizarStatus(int pedidoId, String novoStatus) {
        return pedidoRepository.atualizarStatus(pedidoId, novoStatus);
    }

    public boolean atualizarStatusItem(int pedidoId, Produto produto, String novoStatus) {
        if (pedidoId <= 0 || produto == null || novoStatus == null || novoStatus.isBlank()) {
            return false;
        }

        return pedidoRepository.atualizarStatusItem(pedidoId, produto.getId(), novoStatus);
    }

    public boolean atualizarControleItem(
            int pedidoId,
            Produto produto,
            String novoStatus,
            int quantidadeEnviada,
            int quantidadeRecebida
    ) {
        if (pedidoId <= 0 || produto == null || novoStatus == null || novoStatus.isBlank()) {
            return false;
        }
        return pedidoRepository.atualizarControleItem(
                pedidoId,
                produto.getId(),
                novoStatus,
                quantidadeEnviada,
                quantidadeRecebida
        );
    }

    public boolean cancelarPedido(int pedidoId) {
        return pedidoRepository.cancelar(pedidoId);
    }

    public boolean removerPedido(int pedidoId) {
        if (pedidoId <= 0) {
            return false;
        }
        return pedidoRepository.remover(pedidoId);
    }

    public boolean registrarHistorico(int pedidoId, String autor, String acao, String observacao) {
        if (pedidoId <= 0 || acao == null || acao.isBlank()) {
            return false;
        }
        return pedidoRepository.registrarHistorico(pedidoId, autor, acao, observacao);
    }

    public Pedido buscarPedido(int id) {
        return pedidoRepository.buscarPorId(id);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.listarTodos();
    }

}
