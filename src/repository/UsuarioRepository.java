package repository;

import model.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {
    private List<Usuario> usuarios = new ArrayList<>();
    private int proximoId = 1;

    public Usuario salvar(String nome, String tipo) {
        Usuario usuario = new Usuario(proximoId++, nome, tipo);
        usuarios.add(usuario);
        return usuario;
    }

    public List<Usuario> listarTodos() {
        return usuarios;
    }

    public Usuario buscarPorId(int id) {
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) return usuario;
        }
        return null;
    }
}
