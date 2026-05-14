package repository;

import model.Pedido;
import model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class PedidoRepository {
    private List<Pedido> pedidos = new ArrayList<>();
    private int proximoId = 1;

    public Pedido criarPedido(Usuario cliente, String tipoEntrega) {
        Pedido pedido = new Pedido(proximoId++, cliente, tipoEntrega);
        pedidos.add(pedido);
        return pedido;
    }

    public List<Pedido> listarTodos() {
        return pedidos;
    }

    public Pedido buscarPorId(int id) {
        for (Pedido pedido : pedidos) {
            if (pedido.getId() == id) return pedido;
        }
        return null;
    }
}
