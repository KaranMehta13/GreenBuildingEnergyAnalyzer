package ui;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("Green Building Energy Analyzer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new InputPanel());
    }
}
