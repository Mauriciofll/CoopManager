package repository;

import model.Produto;
import model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {
    private List<Produto> produtos = new ArrayList<>();
    private int proximoId = 1;

    public Produto salvar(String nome, String categoria, double preco, int estoque, Usuario produtor) {
        Produto produto = new Produto(proximoId++, nome, categoria, preco, estoque, produtor);
        produtos.add(produto);
        return produto;
    }

    public List<Produto> listarTodos() {
        return produtos;
    }

    public Produto buscarPorId(int id) {
        for (Produto produto : produtos) {
            if (produto.getId() == id) return produto;
        }
        return null;
    }
}
