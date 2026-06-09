package view;

import controller.PedidoController;
import controller.ProdutoController;
import model.Cliente;
import model.Pedido;
import model.Produto;
import model.Usuario;
import repository.ClienteRepository;
import repository.PedidoRepository;
import repository.ProdutoRepository;
import repository.UsuarioRepository;

import java.util.Scanner;

public class MainConsoleView {
    private Scanner scanner = new Scanner(System.in);

    private UsuarioRepository usuarioRepository = new UsuarioRepository();
    private ClienteRepository clienteRepository = new ClienteRepository();
    private ProdutoRepository produtoRepository = new ProdutoRepository();
    private PedidoRepository pedidoRepository = new PedidoRepository();

    private ProdutoController produtoController = new ProdutoController(produtoRepository);
    private PedidoController pedidoController = new PedidoController(pedidoRepository);

    public void iniciar() {
        carregarDadosExemplo();

        int opcao;
        do {
            System.out.println("\n=== Sistema de Gestão Cooperativa ===");
            System.out.println("1 - Listar catálogo de produtos");
            System.out.println("2 - Cadastrar produto");
            System.out.println("3 - Criar pedido");
            System.out.println("4 - Listar pedidos");
            System.out.println("5 - Atualizar status de pedido");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");
            opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> listarProdutos();
                case 2 -> cadastrarProduto();
                case 3 -> criarPedido();
                case 4 -> listarPedidos();
                case 5 -> atualizarStatusPedido();
                case 0 -> System.out.println("Encerrando...");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void carregarDadosExemplo() {
        if (!usuarioRepository.estaVazio()) {
            return;
        }

        usuarioRepository.salvar("Cassiano", "ADMIN");
        Usuario produtor1 = usuarioRepository.salvar("Produtor da Agricultura", "PRODUTOR", "agricultura", "agro123");
        Usuario produtor2 = usuarioRepository.salvar("Artesão Associado", "PRODUTOR", "artesao", "arte123");
        clienteRepository.salvar("Cliente Exemplo");

        produtoController.cadastrarProduto("Cesta de verduras", "AGRICULTURA", 35.00, 10, produtor1);
        produtoController.cadastrarProduto("Pano de prato artesanal", "ARTESANATO", 22.50, 8, produtor2);
    }

    private void listarProdutos() {
        System.out.println("\n--- Catálogo Digital ---");
        for (Produto produto : produtoController.listarProdutos()) {
            System.out.println(produto);
        }
    }

    private void cadastrarProduto() {
        System.out.print("Nome do produto: ");
        String nome = scanner.nextLine();

        System.out.print("Categoria: ");
        String categoria = scanner.nextLine();

        System.out.print("Preço: ");
        double preco = scanner.nextDouble();

        System.out.print("Estoque: ");
        int estoque = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Produtores cadastrados:");
        for (Usuario usuario : usuarioRepository.listarTodos()) {
            if (usuario.getTipo().equals("PRODUTOR")) {
                System.out.println(usuario);
            }
        }

        System.out.print("ID do produtor: ");
        int produtorId = scanner.nextInt();
        scanner.nextLine();

        Usuario produtor = usuarioRepository.buscarPorId(produtorId);
        if (produtor == null || !produtor.getTipo().equals("PRODUTOR")) {
            System.out.println("Produtor inválido.");
            return;
        }

        produtoController.cadastrarProduto(nome, categoria, preco, estoque, produtor);
        System.out.println("Produto cadastrado com sucesso.");
    }

    private void criarPedido() {
        Cliente cliente = clienteRepository.listarTodos().isEmpty()
                ? clienteRepository.salvar("Cliente Exemplo")
                : clienteRepository.listarTodos().get(0);

        System.out.print("Tipo de entrega (RETIRADA/ENTREGA): ");
        String tipoEntrega = scanner.nextLine();

        Pedido pedido = pedidoController.criarPedido(cliente, tipoEntrega);

        listarProdutos();
        System.out.print("ID do produto: ");
        int produtoId = scanner.nextInt();

        System.out.print("Quantidade: ");
        int quantidade = scanner.nextInt();
        scanner.nextLine();

        Produto produto = produtoController.buscarProduto(produtoId);
        boolean sucesso = pedidoController.adicionarProdutoAoPedido(pedido, produto, quantidade);

        if (sucesso) {
            System.out.println("Pedido criado com sucesso: " + pedido);
        } else {
            System.out.println("Não foi possível criar o pedido. Verifique o estoque.");
        }
    }

    private void listarPedidos() {
        System.out.println("\n--- Pedidos ---");
        for (Pedido pedido : pedidoController.listarPedidos()) {
            System.out.println(pedido);
            for (var item : pedido.getItens()) {
                System.out.println("  - " + item);
            }
        }
    }

    private void atualizarStatusPedido() {
        listarPedidos();
        System.out.print("ID do pedido: ");
        int pedidoId = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Status disponíveis: AGUARDANDO_PRODUTORES, PARCIALMENTE_RECEBIDO, PENDENCIA, EM_SEPARACAO, PRONTO, ENTREGUE");
        System.out.print("Novo status: ");
        String novoStatus = scanner.nextLine();

        if (pedidoController.atualizarStatus(pedidoId, novoStatus)) {
            System.out.println("Status atualizado.");
        } else {
            System.out.println("Pedido não encontrado.");
        }
    }
}
