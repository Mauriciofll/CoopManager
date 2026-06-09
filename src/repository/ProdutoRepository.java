package repository;

import database.DatabaseGateway;
import database.DatabaseProvider;
import model.Produto;
import model.Usuario;

import java.util.List;

public class ProdutoRepository {
    private final DatabaseGateway database = DatabaseProvider.getDatabase();

    public Produto salvar(String nome, String categoria, double preco, int estoque, Usuario produtor) {
        return salvar(nome, categoria, preco, estoque, 3, produtor);
    }

    public Produto salvar(String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        return database.salvarProduto(nome, categoria, preco, estoque, estoqueMinimo, produtor);
    }

    public List<Produto> listarTodos() {
        return database.listarProdutos();
    }

    public List<Produto> listarPorProdutor(Usuario produtor) {
        return database.listarProdutosPorProdutor(produtor);
    }

    public Produto buscarPorId(int id) {
        return database.buscarProdutoPorId(id);
    }

    public boolean atualizar(int id, String nome, String categoria, double preco, int estoque) {
        Produto produto = buscarPorId(id);
        if (produto == null) {
            return false;
        }

        return atualizar(id, nome, categoria, preco, estoque, produto.getEstoqueMinimo());
    }

    public boolean atualizar(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo) {
        return database.atualizarProduto(id, nome, categoria, preco, estoque, estoqueMinimo);
    }

    public boolean remover(int id) {
        return database.removerProduto(id);
    }
}
