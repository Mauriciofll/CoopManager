package model;

import java.io.Serializable;

public class ItemPedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private Produto produto;
    private int quantidade;
    private int quantidadeEnviada;
    private int quantidadeRecebida;
    private String status;

    public ItemPedido(Produto produto, int quantidade) {
        this(produto, quantidade, "PENDENTE");
    }

    public ItemPedido(Produto produto, int quantidade, String status) {
        this(produto, quantidade, status, 0, 0);
    }

    public ItemPedido(Produto produto, int quantidade, String status, int quantidadeEnviada, int quantidadeRecebida) {
        this.produto = produto;
        this.quantidade = Math.max(0, quantidade);
        this.status = normalizarStatus(status);
        this.quantidadeEnviada = limitarQuantidade(quantidadeEnviada);
        this.quantidadeRecebida = limitarQuantidade(quantidadeRecebida);
        normalizarValores();
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public int getQuantidadeEnviada() { return quantidadeEnviada; }
    public int getQuantidadeRecebida() { return quantidadeRecebida; }
    public int getQuantidadePendenteEnvio() { return Math.max(0, quantidade - quantidadeEnviada); }
    public int getQuantidadeEmTransito() { return Math.max(0, quantidadeEnviada - quantidadeRecebida); }
    public int getQuantidadePendenteRecebimento() { return Math.max(0, quantidade - quantidadeRecebida); }
    public String getStatus() { return normalizarStatus(status); }

    public void atualizarStatus(String status) {
        this.status = normalizarStatus(status);
        if ("ENTREGUE_COOPERATIVA".equals(this.status)) {
            quantidadeEnviada = quantidade;
            quantidadeRecebida = quantidade;
        }
        if ("ENVIADO".equals(this.status) && quantidadeEnviada == 0) {
            quantidadeEnviada = quantidade;
        }
    }

    public void atualizarControleLogistico(String status, int quantidadeEnviada, int quantidadeRecebida) {
        this.status = normalizarStatus(status);
        this.quantidadeRecebida = limitarQuantidade(quantidadeRecebida);
        this.quantidadeEnviada = Math.max(limitarQuantidade(quantidadeEnviada), this.quantidadeRecebida);

        if ("ENTREGUE_COOPERATIVA".equals(this.status)) {
            this.quantidadeEnviada = quantidade;
            this.quantidadeRecebida = quantidade;
        } else if ("ENVIADO".equals(this.status) && this.quantidadeEnviada == 0) {
            this.quantidadeEnviada = quantidade;
        } else if ("INDISPONIVEL".equals(this.status) || "CANCELADO".equals(this.status)) {
            this.quantidadeEnviada = 0;
            this.quantidadeRecebida = 0;
        } else if (this.quantidadeRecebida >= quantidade && quantidade > 0) {
            this.status = "ENTREGUE_COOPERATIVA";
        } else if (this.quantidadeEnviada > 0) {
            this.status = "ENVIADO";
        }
    }

    public double getSubtotal() {
        if (produto == null || quantidade <= 0) {
            return 0;
        }
        return produto.getPreco() * quantidade;
    }

    public boolean pertenceAoProdutor(Usuario produtor) {
        return produtor != null
                && produto != null
                && produto.getProdutor() != null
                && produto.getProdutor().getId() == produtor.getId();
    }

    public void normalizarFluxoOperacional() {
        normalizarValores();
    }

    private void normalizarValores() {
        status = normalizarStatus(status);
        quantidade = Math.max(0, quantidade);
        quantidadeEnviada = limitarQuantidade(quantidadeEnviada);
        quantidadeRecebida = Math.min(limitarQuantidade(quantidadeRecebida), quantidadeEnviada);

        if ("ENTREGUE_COOPERATIVA".equals(status) && quantidadeRecebida == 0 && quantidade > 0) {
            quantidadeEnviada = quantidade;
            quantidadeRecebida = quantidade;
        }
        if ("ENVIADO".equals(status) && quantidadeEnviada == 0 && quantidade > 0) {
            quantidadeEnviada = quantidade;
        }
    }

    @Override
    public String toString() {
        String nomeProduto = produto == null ? "Produto removido" : produto.getNome();
        return nomeProduto + " x" + quantidade
                + " | enviado " + quantidadeEnviada + "/" + quantidade
                + " | recebido " + quantidadeRecebida + "/" + quantidade
                + " | " + getStatus();
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PENDENTE";
        }
        return status;
    }

    private int limitarQuantidade(int valor) {
        return Math.max(0, Math.min(valor, quantidade));
    }
}
