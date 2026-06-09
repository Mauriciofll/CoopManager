package repository;

import database.DatabaseGateway;
import database.DatabaseProvider;
import model.Cliente;

import java.util.List;

public class ClienteRepository {
    private final DatabaseGateway database = DatabaseProvider.getDatabase();

    public Cliente salvar(String nome) {
        return salvar(nome, "", "");
    }

    public Cliente salvar(String nome, String email, String telefone) {
        return database.salvarCliente(nome, email, telefone);
    }

    public boolean atualizar(int id, String nome) {
        return atualizar(id, nome, "", "");
    }

    public boolean atualizar(int id, String nome, String email, String telefone) {
        return database.atualizarCliente(id, nome, email, telefone);
    }

    public boolean remover(int id) {
        return database.removerCliente(id);
    }

    public List<Cliente> listarTodos() {
        return database.listarClientes();
    }

    public Cliente buscarPorId(int id) {
        return database.buscarClientePorId(id);
    }
}
