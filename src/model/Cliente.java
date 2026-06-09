package model;

public class Cliente extends Usuario {
    private static final long serialVersionUID = 1L;

    private String email;
    private String telefone;

    public Cliente(int id, String nome) {
        this(id, nome, "", "");
    }

    public Cliente(int id, String nome, String email, String telefone) {
        super(id, nome, "CLIENTE", "", "");
        this.email = normalizar(email);
        this.telefone = normalizar(telefone);
    }

    public String getEmail() {
        return normalizar(email);
    }

    public String getTelefone() {
        return normalizar(telefone);
    }

    public void atualizarContato(String email, String telefone) {
        this.email = normalizar(email);
        this.telefone = normalizar(telefone);
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public String toString() {
        return getId() + " - " + getNome();
    }
}
