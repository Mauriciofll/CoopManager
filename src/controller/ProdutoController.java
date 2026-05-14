package controller;

import model.Produto;
import model.Usuario;
import repository.ProdutoRepository;
import java.util.List;

public class ProdutoController {
    private ProdutoRepository produtoRepository;

    public ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto cadastrarProduto(String nome, String categoria, double preco, int estoque, Usuario produtor) {
        return produtoRepository.salvar(nome, categoria, preco, estoque, produtor);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.listarTodos();
    }

    public Produto buscarProduto(int id) {
        return produtoRepository.buscarPorId(id);
    }
}
