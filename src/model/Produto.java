package model;

public class Produto {
    private int id;
    private String nome;
    private String categoria; // AGRICULTURA, ARTESANATO
    private double preco;
    private int estoque;
    private Usuario produtor;

    public Produto(int id, String nome, String categoria, double preco, int estoque, Usuario produtor) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.preco = preco;
        this.estoque = estoque;
        this.produtor = produtor;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }
    public int getEstoque() { return estoque; }
    public Usuario getProdutor() { return produtor; }

    public void reduzirEstoque(int quantidade) {
        if (quantidade <= estoque) {
            estoque -= quantidade;
        }
    }

    @Override
    public String toString() {
        return id + " - " + nome + " | " + categoria + " | R$ " + preco + " | Estoque: " + estoque + " | Produtor: " + produtor.getNome();
    }
}
