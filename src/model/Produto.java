package model;

import java.io.Serializable;

public class Produto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String categoria; // AGRICULTURA, ARTESANATO
    private double preco;
    private int estoque;
    private int estoqueMinimo;
    private Usuario produtor;

    public Produto(int id, String nome, String categoria, double preco, int estoque, Usuario produtor) {
        this(id, nome, categoria, preco, estoque, 3, produtor);
    }

    public Produto(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.estoque = estoque;
        this.estoqueMinimo = estoqueMinimo;
        this.produtor = produtor;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }
    public int getEstoque() { return estoque; }
    public int getEstoqueMinimo() { return estoqueMinimo; }
    public Usuario getProdutor() { return produtor; }

    public void atualizarDados(String nome, String categoria, double preco, int estoque) {
        atualizarDados(nome, categoria, preco, estoque, estoqueMinimo);
    }

    public void atualizarDados(String nome, String categoria, double preco, int estoque, int estoqueMinimo) {
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.estoque = estoque;
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isEstoqueBaixo() {
        return estoque <= estoqueMinimo;
    }

    public void reduzirEstoque(int quantidade) {
        if (quantidade > 0 && quantidade <= estoque) {
            estoque -= quantidade;
        }
    }

    public void aumentarEstoque(int quantidade) {
        if (quantidade > 0) {
            estoque += quantidade;
        }
    }

    public void removerProdutorSeId(int produtorId) {
        if (produtor != null && produtor.getId() == produtorId) {
            produtor = null;
        }
    }

    @Override
    public String toString() {
        String nomeProdutor = produtor == null ? "Sem produtor" : produtor.getNome();
        return id + " - " + nome + " | " + categoria + " | R$ " + preco + " | Estoque: " + estoque + " | Mínimo: " + estoqueMinimo + " | Produtor: " + nomeProdutor;
    }
}
