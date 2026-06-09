package view;

import database.DatabaseConfig;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfigDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JRadioButton localRadio = new JRadioButton("Banco local neste computador");
    private final JRadioButton postgresRadio = new JRadioButton("Banco compartilhado PostgreSQL");
    private final JTextField hostField = new JTextField("localhost", 20);
    private final JTextField portField = new JTextField("5432", 6);
    private final JTextField databaseField = new JTextField("coopmanager", 16);
    private final JTextField userField = new JTextField("coopmanager", 16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private boolean saved;

    public DatabaseConfigDialog(Component owner) {
        super(JOptionPane.getFrameForComponent(owner), "CoopManager - Banco de dados", true);
        build();
        loadCurrentConfig();
        updateFields();
    }

    public static boolean showDialog(Component owner) {
        DatabaseConfigDialog dialog = new DatabaseConfigDialog(owner);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return dialog.saved;
    }

    private void build() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Configurar banco de dados");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 17f));
        content.add(title, BorderLayout.NORTH);

        ButtonGroup group = new ButtonGroup();
        group.add(localRadio);
        group.add(postgresRadio);

        JPanel form = new JPanel(new GridBagLayout());
        addFull(form, 0, localRadio);
        addFull(form, 1, postgresRadio);
        addField(form, 2, "Servidor/IP", hostField);
        addField(form, 3, "Porta", portField);
        addField(form, 4, "Banco", databaseField);
        addField(form, 5, "Usuário", userField);
        addField(form, 6, "Senha", passwordField);
        content.add(form, BorderLayout.CENTER);

        JButton testButton = new JButton("Testar conexão");
        JButton cancelButton = new JButton("Cancelar");
        JButton saveButton = new JButton("Salvar");

        testButton.addActionListener(event -> testConnection());
        cancelButton.addActionListener(event -> dispose());
        saveButton.addActionListener(event -> save());
        localRadio.addActionListener(event -> updateFields());
        postgresRadio.addActionListener(event -> updateFields());

        JPanel buttons = new JPanel(new GridLayout(1, 3, 8, 0));
        buttons.add(testButton);
        buttons.add(cancelButton);
        buttons.add(saveButton);
        content.add(buttons, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(content);
        setMinimumSize(new Dimension(440, 330));
        pack();
    }

    private void loadCurrentConfig() {
        DatabaseConfig config = DatabaseConfig.load();
        if (config.isPostgres()) {
            postgresRadio.setSelected(true);
            parsePostgresUrl(config.getUrl());
            userField.setText(config.getUser());
            passwordField.setText(config.getPassword());
        } else {
            localRadio.setSelected(true);
        }
    }

    private void parsePostgresUrl(String url) {
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            return;
        }

        String value = url.substring("jdbc:postgresql://".length());
        int slash = value.indexOf('/');
        if (slash >= 0) {
            databaseField.setText(value.substring(slash + 1));
            value = value.substring(0, slash);
        }

        int colon = value.lastIndexOf(':');
        if (colon >= 0) {
            hostField.setText(value.substring(0, colon));
            portField.setText(value.substring(colon + 1));
        } else {
            hostField.setText(value);
        }
    }

    private void updateFields() {
        boolean postgres = postgresRadio.isSelected();
        hostField.setEnabled(postgres);
        portField.setEnabled(postgres);
        databaseField.setEnabled(postgres);
        userField.setEnabled(postgres);
        passwordField.setEnabled(postgres);
    }

    private void testConnection() {
        if (!postgresRadio.isSelected()) {
            JOptionPane.showMessageDialog(this, "O banco local não precisa de teste de conexão.", "CoopManager", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");
            DriverManager.setLoginTimeout(6);
            try (Connection ignored = DriverManager.getConnection(buildUrl(), userField.getText().trim(), new String(passwordField.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Conexão realizada com sucesso.", "CoopManager", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Não foi possível conectar.\n" + exception.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void save() {
        try {
            if (localRadio.isSelected()) {
                DatabaseConfig.saveLocal();
            } else {
                validatePostgresFields();
                DatabaseConfig.savePostgres(buildUrl(), userField.getText().trim(), new String(passwordField.getPassword()));
            }
            saved = true;
            JOptionPane.showMessageDialog(this, "Configuração salva. Reinicie o CoopManager para usar este banco.", "CoopManager", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void validatePostgresFields() {
        if (hostField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o servidor/IP do PostgreSQL.");
        }
        if (portField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a porta do PostgreSQL.");
        }
        if (databaseField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do banco.");
        }
        if (userField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o usuário do banco.");
        }
    }

    private String buildUrl() {
        return "jdbc:postgresql://"
                + hostField.getText().trim()
                + ":"
                + portField.getText().trim()
                + "/"
                + databaseField.getText().trim();
    }

    private void addFull(JPanel form, int row, Component component) {
        GridBagConstraints gbc = constraints(row);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(component, gbc);
    }

    private void addField(JPanel form, int row, String labelText, Component component) {
        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(SwingConstants.LEFT);

        GridBagConstraints labelConstraints = constraints(row);
        labelConstraints.gridx = 0;
        form.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = constraints(row);
        fieldConstraints.gridx = 1;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(component, fieldConstraints);
    }

    private GridBagConstraints constraints(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(6, 0, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }
}
