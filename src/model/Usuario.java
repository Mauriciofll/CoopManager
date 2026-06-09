package model;

import security.PasswordHasher;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String tipo; // ADMIN, PRODUTOR
    private String login;
    private String senha;
    private boolean temaEscuro;

    public Usuario(int id, String nome, String tipo) {
        this(id, nome, tipo, "", "");
    }

    public Usuario(int id, String nome, String tipo, String login, String senha) {
        this(id, nome, tipo, login, senha, false);
    }

    public Usuario(int id, String nome, String tipo, String login, String senha, boolean temaEscuro) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.login = login;
        this.senha = senha;
        this.temaEscuro = temaEscuro;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getLogin() { return login; }
    public boolean isTemaEscuro() { return temaEscuro; }

    public boolean possuiSenha() {
        return senha != null && !senha.isBlank();
    }

    public void atualizarDados(String nome, String tipo, String login) {
        this.nome = nome;
        this.tipo = tipo;
        this.login = login;
    }

    public void atualizarSenha(String senhaHash) {
        this.senha = senhaHash;
    }

    public void atualizarTemaEscuro(boolean temaEscuro) {
        this.temaEscuro = temaEscuro;
    }

    public boolean autenticar(String login, String senha) {
        return this.login != null
                && this.senha != null
                && this.login.equalsIgnoreCase(login)
                && PasswordHasher.verify(senha, this.senha);
    }

    @Override
    public String toString() {
        return id + " - " + nome + " (" + tipo + ")";
    }
}
