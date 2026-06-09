package repository;

import database.DatabaseGateway;
import database.DatabaseProvider;
import model.Usuario;

import java.util.List;

public class UsuarioRepository {
    private final DatabaseGateway database = DatabaseProvider.getDatabase();

    public Usuario salvar(String nome, String tipo) {
        return salvar(nome, tipo, "", "");
    }

    public Usuario salvar(String nome, String tipo, String login, String senha) {
        validarLoginDisponivel(0, login);
        return database.salvarUsuario(nome, tipo, login, senha);
    }

    public boolean atualizar(int id, String nome, String tipo, String login, String senha) {
        validarLoginDisponivel(id, login);
        return database.atualizarUsuario(id, nome, tipo, login, senha);
    }

    public boolean atualizarTema(int id, boolean temaEscuro) {
        return database.atualizarTemaUsuario(id, temaEscuro);
    }

    public List<Usuario> listarTodos() {
        return database.listarUsuarios();
    }

    public Usuario buscarPorId(int id) {
        return database.buscarUsuarioPorId(id);
    }

    public Usuario autenticarProdutor(String login, String senha) {
        return autenticarUsuario(login, senha);
    }

    public Usuario autenticarUsuario(String login, String senha) {
        for (Usuario usuario : database.listarUsuarios()) {
            if (usuario != null
                    && ("PRODUTOR".equals(usuario.getTipo()) || "ADMIN".equals(usuario.getTipo()))
                    && usuario.autenticar(login, senha)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean estaVazio() {
        return database.usuariosVazio();
    }

    public boolean remover(int id) {
        return database.removerUsuario(id);
    }

    private void validarLoginDisponivel(int usuarioId, String login) {
        if (login == null || login.isBlank()) {
            return;
        }

        for (Usuario usuario : database.listarUsuarios()) {
            if (usuario != null
                    && usuario.getId() != usuarioId
                    && login.trim().equalsIgnoreCase(usuario.getLogin())) {
                throw new IllegalArgumentException("Já existe um usuário com este login.");
            }
        }
    }
}
