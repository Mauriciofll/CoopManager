package model;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int id;
    private Usuario cliente;
    private List<ItemPedido> itens;
    private String status; // PENDENTE, EM_SEPARACAO, PRONTO, FINALIZADO
    private String tipoEntrega; // RETIRADA ou ENTREGA

    public Pedido(int id, Usuario cliente, String tipoEntrega) {
        this.id = id;
        this.cliente = cliente;
        this.tipoEntrega = tipoEntrega;
        this.status = "PENDENTE";
        this.itens = new ArrayList<>();
    }

    public int getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public String getStatus() { return status; }
    public String getTipoEntrega() { return tipoEntrega; }
    public List<ItemPedido> getItens() { return itens; }

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.getProduto().reduzirEstoque(item.getQuantidade());
    }

    public void atualizarStatus(String novoStatus) {
        this.status = novoStatus;
    }

    public double getTotal() {
        double total = 0;
        for (ItemPedido item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Pedido #" + id + " | Cliente: " + cliente.getNome() + " | Status: " + status + " | " + tipoEntrega + " | Total: R$ " + getTotal();
    }
}
