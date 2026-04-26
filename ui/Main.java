package ui;

import db.DatabaseConnection;
import db.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {

        // Add this - initializes DB and creates table
        DatabaseInitializer.initialize();

        // Your existing code - unchanged
        MainFrame frame = new MainFrame();
        frame.setVisible(true);

        // Add this - closes DB when app exits
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                DatabaseConnection.closeConnection();
            }
        }));
    }
}