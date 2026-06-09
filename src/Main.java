import view.DatabaseConfigDialog;
import view.MainSwingView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

public class Main {
    private static RandomAccessFile lockFile;
    private static FileChannel lockChannel;
    private static FileLock appLock;

    public static void main(String[] args) {
        if (!bloquearInstanciaUnica()) {
            JOptionPane.showMessageDialog(null, "O CoopManager já está aberto.", "CoopManager", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
            return;
        }

        configurarAparencia();
        if (configurarBancoSolicitado(args)) {
            SwingUtilities.invokeLater(() -> {
                DatabaseConfigDialog.showDialog(null);
                System.exit(0);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainSwingView view = new MainSwingView();
                view.iniciar();
            } catch (Exception exception) {
                String detalhe = exception.getMessage();
                if (detalhe == null || detalhe.isBlank()) {
                    detalhe = "Verifique a configuração do banco de dados e tente novamente.";
                }
                JOptionPane.showMessageDialog(
                        null,
                        "Não foi possível iniciar o CoopManager.\n" + detalhe,
                        "CoopManager",
                        JOptionPane.ERROR_MESSAGE
                );
                exception.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static boolean configurarBancoSolicitado(String[] args) {
        if (args == null) {
            return false;
        }

        for (String arg : args) {
            if ("--configurar-banco".equalsIgnoreCase(arg)
                    || "--config-db".equalsIgnoreCase(arg)
                    || "--database-config".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void configurarAparencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception exception) {
            System.out.println("Não foi possível carregar a aparência Nimbus.");
        }
    }

    private static boolean bloquearInstanciaUnica() {
        try {
            File lockPath = new File(System.getProperty("java.io.tmpdir"), "coopmanager.lock");
            lockFile = new RandomAccessFile(lockPath, "rw");
            lockChannel = lockFile.getChannel();
            appLock = lockChannel.tryLock();
            return appLock != null;
        } catch (OverlappingFileLockException exception) {
            return false;
        } catch (IOException exception) {
            return true;
        }
    }
}
