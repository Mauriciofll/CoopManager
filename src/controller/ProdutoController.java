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

    public Produto cadastrarProduto(String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        return produtoRepository.salvar(nome, categoria, preco, estoque, estoqueMinimo, produtor);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.listarTodos();
    }

    public List<Produto> listarProdutosDoProdutor(Usuario produtor) {
        return produtoRepository.listarPorProdutor(produtor);
    }

    public Produto buscarProduto(int id) {
        return produtoRepository.buscarPorId(id);
    }

    public boolean atualizarProduto(int id, String nome, String categoria, double preco, int estoque, Usuario produtor) {
        Produto produto = produtoRepository.buscarPorId(id);
        if (!produtoPertenceAoProdutor(produto, produtor)) {
            return false;
        }

        return produtoRepository.atualizar(id, nome, categoria, preco, estoque);
    }

    public boolean atualizarProduto(int id, String nome, String categoria, double preco, int estoque, int estoqueMinimo, Usuario produtor) {
        Produto produto = produtoRepository.buscarPorId(id);
        if (!produtoPertenceAoProdutor(produto, produtor)) {
            return false;
        }

        return produtoRepository.atualizar(id, nome, categoria, preco, estoque, estoqueMinimo);
    }

    public boolean removerProduto(int id, Usuario produtor) {
        Produto produto = produtoRepository.buscarPorId(id);
        if (!produtoPertenceAoProdutor(produto, produtor)) {
            return false;
        }

        return produtoRepository.remover(id);
    }

    private boolean produtoPertenceAoProdutor(Produto produto, Usuario produtor) {
        return produto != null
                && produtor != null
                && produto.getProdutor() != null
                && produto.getProdutor().getId() == produtor.getId();
    }
}
