package marchoffools.server;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import marchoffools.server.ui.ServerGUI;

public class ServerMain {
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}