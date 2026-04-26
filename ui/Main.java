package ui;

import db.DatabaseConnection;
import db.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {

        // initializes DB and creates table
        DatabaseInitializer.initialize();

        MainFrame frame = new MainFrame();
        frame.setVisible(true);

        // Closes DB when app exits
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                DatabaseConnection.closeConnection();
            }
        }));
    }
}
