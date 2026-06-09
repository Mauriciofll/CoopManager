package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private Usuario cliente;
    private ArrayList<ItemPedido> itens;
    private String status; // AGUARDANDO_PRODUTORES, AGUARDANDO_RECEBIMENTO, PARCIALMENTE_RECEBIDO, PENDENCIA, EM_SEPARACAO, PRONTO, ENTREGUE, CANCELADO
    private String tipoEntrega; // RETIRADA ou ENTREGA
    private LocalDate dataPedido;
    private LocalDate dataPrevista;
    private ArrayList<String> historico;

    public Pedido(int id, Usuario cliente, String tipoEntrega) {
        this(id, cliente, tipoEntrega, LocalDate.now(), null);
    }

    public Pedido(int id, Usuario cliente, String tipoEntrega, LocalDate dataPedido, LocalDate dataPrevista) {
        this.id = id;
        this.cliente = cliente;
        this.tipoEntrega = tipoEntrega;
        this.status = "AGUARDANDO_PRODUTORES";
        this.dataPedido = dataPedido == null ? LocalDate.now() : dataPedido;
        this.dataPrevista = dataPrevista;
        this.itens = new ArrayList<>();
        this.historico = new ArrayList<>();
    }

    public int getId() { return id; }
    public Usuario getCliente() { return cliente; }
    public String getStatus() {
        if (status == null || status.isBlank() || "PENDENTE".equals(status)) {
            return "AGUARDANDO_PRODUTORES";
        }
        return status;
    }
    public String getTipoEntrega() { return tipoEntrega; }
    public LocalDate getDataPedido() {
        return dataPedido == null ? LocalDate.now() : dataPedido;
    }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public List<ItemPedido> getItens() {
        return new ArrayList<>(getItensInternos());
    }
    public List<String> getHistorico() {
        return new ArrayList<>(getHistoricoInterno());
    }

    public void adicionarItem(ItemPedido item) {
        if (item == null || item.getQuantidade() <= 0 || item.getProduto() == null) {
            return;
        }
        getItensInternos().add(item);
        item.getProduto().reduzirEstoque(item.getQuantidade());
        recalcularStatusOperacional();
    }

    public void adicionarItemSalvo(ItemPedido item) {
        if (item == null) {
            return;
        }
        item.normalizarFluxoOperacional();
        getItensInternos().add(item);
    }

    public void atualizarStatus(String novoStatus) {
        this.status = novoStatus;
    }

    public boolean atualizarStatusItem(int produtoId, String novoStatus) {
        boolean atualizado = false;
        for (ItemPedido item : getItensInternos()) {
            Produto produto = item == null ? null : item.getProduto();
            if (produto != null && produto.getId() == produtoId) {
                item.atualizarStatus(novoStatus);
                atualizado = true;
            }
        }
        if (atualizado) {
            recalcularStatusOperacional();
        }
        return atualizado;
    }

    public boolean atualizarControleItem(int produtoId, String novoStatus, int quantidadeEnviada, int quantidadeRecebida) {
        boolean atualizado = false;
        for (ItemPedido item : getItensInternos()) {
            Produto produto = item == null ? null : item.getProduto();
            if (produto != null && produto.getId() == produtoId) {
                item.atualizarControleLogistico(novoStatus, quantidadeEnviada, quantidadeRecebida);
                atualizado = true;
            }
        }
        if (atualizado) {
            recalcularStatusOperacional();
        }
        return atualizado;
    }

    public void marcarItensCancelados() {
        for (ItemPedido item : getItensInternos()) {
            if (item != null) {
                item.atualizarStatus("CANCELADO");
            }
        }
    }

    public void registrarHistorico(String autor, String acao, String observacao) {
        String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String nomeAutor = autor == null || autor.isBlank() ? "Sistema" : autor.trim();
        String textoAcao = acao == null || acao.isBlank() ? "Atualização" : acao.trim();
        String texto = dataHora + " | " + nomeAutor + " | " + textoAcao;
        if (observacao != null && !observacao.isBlank()) {
            texto += " | " + observacao.trim();
        }
        getHistoricoInterno().add(texto);
    }

    public void adicionarHistoricoSalvo(String registro) {
        if (registro != null && !registro.isBlank()) {
            getHistoricoInterno().add(registro);
        }
    }

    public void atualizarCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public void removerClienteSeId(int clienteId) {
        if (cliente != null && cliente.getId() == clienteId) {
            cliente = null;
        }
    }

    public double getTotal() {
        double total = 0;
        for (ItemPedido item : getItens()) {
            if (item != null) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    public void normalizarFluxoOperacional() {
        status = getStatus();
        if (dataPedido == null) {
            dataPedido = LocalDate.now();
        }
        getHistoricoInterno();
        for (ItemPedido item : getItensInternos()) {
            if (item != null) {
                item.normalizarFluxoOperacional();
            }
        }
    }

    public void recalcularStatusOperacional() {
        String statusAtual = getStatus();
        if ("CANCELADO".equals(statusAtual) || "ENTREGUE".equals(statusAtual)) {
            return;
        }

        List<ItemPedido> itensAtuais = getItensInternos();
        if (itensAtuais.isEmpty()) {
            status = "AGUARDANDO_PRODUTORES";
            return;
        }

        boolean algumRecebido = false;
        boolean todosRecebidos = true;
        boolean algumIndisponivel = false;
        boolean algumEnviado = false;
        for (ItemPedido item : itensAtuais) {
            String statusItem = item == null ? "PENDENTE" : item.getStatus();
            if ("INDISPONIVEL".equals(statusItem)) {
                algumIndisponivel = true;
            }
            int quantidade = item == null ? 0 : item.getQuantidade();
            int quantidadeEnviada = item == null ? 0 : item.getQuantidadeEnviada();
            int quantidadeRecebida = item == null ? 0 : item.getQuantidadeRecebida();
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
            status = "PENDENCIA";
        } else if (todosRecebidos) {
            status = "EM_SEPARACAO";
        } else if (algumRecebido) {
            status = "PARCIALMENTE_RECEBIDO";
        } else if (algumEnviado) {
            status = "AGUARDANDO_RECEBIMENTO";
        } else {
            status = "AGUARDANDO_PRODUTORES";
        }
    }

    @Override
    public String toString() {
        String nomeCliente = cliente == null ? "Sem cliente" : cliente.getNome();
        return "Pedido #" + id + " | Cliente: " + nomeCliente + " | Status: " + statusFormatado()
                + " | " + tipoEntrega + " | Data: " + getDataPedido() + " | Total: R$ " + getTotal();
    }

    private String statusFormatado() {
        return switch (getStatus()) {
            case "AGUARDANDO_PRODUTORES" -> "AGUARDANDO PRODUTORES";
            case "AGUARDANDO_RECEBIMENTO" -> "AGUARDANDO RECEBIMENTO";
            case "PARCIALMENTE_RECEBIDO" -> "PARCIALMENTE RECEBIDO";
            case "PENDENCIA" -> "PENDÊNCIA";
            case "EM_SEPARACAO" -> "EM SEPARAÇÃO";
            case "ENVIADO" -> "ENVIADO À COOPERATIVA";
            case "ENTREGUE_COOPERATIVA" -> "ENTREGUE À COOPERATIVA";
            case "INDISPONIVEL" -> "INDISPONÍVEL";
            default -> getStatus().replace('_', ' ');
        };
    }

    private List<ItemPedido> getItensInternos() {
        if (itens == null) {
            itens = new ArrayList<>();
        }
        return itens;
    }

    private List<String> getHistoricoInterno() {
        if (historico == null) {
            historico = new ArrayList<>();
        }
        return historico;
    }
}
