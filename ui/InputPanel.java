package ui;

import db.DatabaseConnection;
import db.DatabaseInitializer;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class InputPanel extends JPanel {

    JTextField applianceNameField, powerField, hoursField, rateField;
    JTextField solarWattField, sunlightHoursField;
    JComboBox<String> durationBox;
    JButton addButton, calculateButton, resetButton;
    JTextArea resultArea;

    ChartPanel pieChartPanel, barChartPanel;

    double totalEnergy = 0;
    Map<String, Double> applianceData = new HashMap<>();

    public InputPanel() {

        // COLORS
        Color bgColor = new Color(245, 247, 250);
        Color panelColor = Color.WHITE;
        Color accent = new Color(76, 175, 80);
        Color buttonColor = new Color(100, 149, 237);

        setLayout(new BorderLayout(10, 10));
        setBackground(bgColor);
        setPreferredSize(new Dimension(900, 600));

        JLabel title = new JLabel("Green Building Energy Analyzer", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(accent);
        add(title, BorderLayout.NORTH);

        // INPUTS
        applianceNameField = new JTextField();
        powerField = new JTextField();
        hoursField = new JTextField();
        rateField = new JTextField("7");

        solarWattField = new JTextField();
        sunlightHoursField = new JTextField();

        durationBox = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Yearly"});

        addButton = new JButton("Add Appliance");
        calculateButton = new JButton("Calculate");
        resetButton = new JButton("Reset");

        styleButton(addButton, buttonColor);
        styleButton(calculateButton, accent);
        styleButton(resetButton, Color.GRAY);

        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBackground(new Color(250, 250, 250));

        // LEFT PANEL
        JPanel leftPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));
        leftPanel.setPreferredSize(new Dimension(300, 600));
        leftPanel.setBackground(panelColor);

        addRow(leftPanel, "Appliance Name:", applianceNameField);
        addRow(leftPanel, "Power (W):", powerField);
        addRow(leftPanel, "Hours Used:", hoursField);
        addRow(leftPanel, "Rate per Unit (₹):", rateField);
        addRow(leftPanel, "Duration:", durationBox);
        addRow(leftPanel, "Solar Wattage (W):", solarWattField);
        addRow(leftPanel, "Sunlight Hours:", sunlightHoursField);

        leftPanel.add(addButton);
        leftPanel.add(calculateButton);
        leftPanel.add(resetButton);

        // RIGHT PANEL
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        rightPanel.setBackground(panelColor);
        rightPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // CHARTS
        JPanel chartContainer = new JPanel(new GridLayout(2, 1));
        chartContainer.setBorder(BorderFactory.createTitledBorder("Analytics"));
        chartContainer.setBackground(panelColor);

        pieChartPanel = new ChartPanel(null);
        barChartPanel = new ChartPanel(null);

        chartContainer.add(pieChartPanel);
        chartContainer.add(barChartPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightPanel, chartContainer);
        splitPane.setDividerLocation(250);

        add(leftPanel, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);

        // ACTIONS
        addButton.addActionListener(e -> addAppliance());
        calculateButton.addActionListener(e -> calculateTotal());
        resetButton.addActionListener(e -> resetAll());
    }

    private void addRow(JPanel panel, String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(l);
        panel.add(field);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private void addAppliance() {
        try {
            String name = applianceNameField.getText();
            double power = Double.parseDouble(powerField.getText());
            double hours = Double.parseDouble(hoursField.getText());
            double rate = Double.parseDouble(rateField.getText());
            String duration = (String) durationBox.getSelectedItem();

            double solarWatt = 0;
            double sunlight = 0;
            try {
                solarWatt = Double.parseDouble(solarWattField.getText());
                sunlight = Double.parseDouble(sunlightHoursField.getText());
            } catch (Exception ignored) {}

            double energy = (power * hours) / 1000;
            totalEnergy += energy;
            applianceData.put(name, energy);

            // SAVE TO DATABASE
            saveToDatabase(name, power, hours, rate, duration, solarWatt, sunlight);

            resultArea.append("✔ " + name + " → " + energy + " kWh\n");

            applianceNameField.setText("");
            powerField.setText("");
            hoursField.setText("");

            updateCharts();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid inputs");
        }
    }

    //  Saves appliance to SQLite
    private void saveToDatabase(String name, double power, double hours,
                                double rate, String duration,
                                double solarWatt, double sunlight) {

        String sql = "INSERT INTO appliances " +
                "(appliance_name, power_watts, hours_used, rate_per_unit, duration, solar_wattage, sunlight_hours) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, power);
            pstmt.setDouble(3, hours);
            pstmt.setDouble(4, rate);
            pstmt.setString(5, duration);
            pstmt.setDouble(6, solarWatt);
            pstmt.setDouble(7, sunlight);
            pstmt.executeUpdate();

            System.out.println("Saved to DB: " + name);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateCharts() {

        DefaultPieDataset pieDataset = new DefaultPieDataset();
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();

        for (String key : applianceData.keySet()) {
            double value = applianceData.get(key);
            pieDataset.setValue(key, value);
            barDataset.addValue(value, "Energy", key);
        }

        pieChartPanel.setChart(
                ChartFactory.createPieChart("Energy Distribution", pieDataset, true, true, false)
        );

        barChartPanel.setChart(
                ChartFactory.createBarChart("Energy Comparison", "Appliance", "kWh", barDataset)
        );
    }

    private void calculateTotal() {
        try {
            double rate = Double.parseDouble(rateField.getText());

            String duration = (String) durationBox.getSelectedItem();
            int multiplier = 1;

            if (duration.equals("Weekly")) multiplier = 7;
            else if (duration.equals("Monthly")) multiplier = 30;
            else if (duration.equals("Yearly")) multiplier = 365;

            double adjustedEnergy = totalEnergy * multiplier;

            double solarEnergy = 0;
            try {
                double solarWatt = Double.parseDouble(solarWattField.getText());
                double sunlight = Double.parseDouble(sunlightHoursField.getText());
                solarEnergy = (solarWatt * sunlight * multiplier) / 1000;
            } catch (Exception ignored) {}

            double finalEnergy = adjustedEnergy - solarEnergy;
            if (finalEnergy < 0) finalEnergy = 0;

            double bill = finalEnergy * rate;
            double carbon = finalEnergy * 0.82;

            resultArea.append("\n----- ENERGY REPORT -----\n");
            resultArea.append("Duration: " + duration + "\n");
            resultArea.append("Total Consumption: " + String.format("%.2f", adjustedEnergy) + " kWh\n");
            resultArea.append("Solar Energy Produced: " + String.format("%.2f", solarEnergy) + " kWh\n");
            resultArea.append("Net Energy (Consumed - Solar): " + String.format("%.2f", finalEnergy) + " kWh\n");
            resultArea.append("Estimated Bill: ₹" + String.format("%.2f", bill) + "\n");
            resultArea.append("Carbon Footprint: " + String.format("%.2f", carbon) + " kg CO2\n");
            resultArea.append("Green Score: " + getGreenScore(finalEnergy) + "\n");

            if (finalEnergy > 20) {
                resultArea.append("Suggestion: Reduce high-power appliance usage.\n");
            } else {
                resultArea.append("Good job! Energy usage is efficient.\n");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid rate");
        }
    }

    private String getGreenScore(double energy) {
        if (energy < 50) return "A+ (Excellent)";
        else if (energy < 150) return "A (Efficient)";
        else if (energy < 350) return "B (Moderate)";
        else return "C (High Consumption)";
    }

    private void resetAll() {
        // CLEAR DATABASE
        clearDatabase();

        totalEnergy = 0;
        applianceData.clear();
        resultArea.setText("");

        applianceNameField.setText("");
        powerField.setText("");
        hoursField.setText("");
        solarWattField.setText("");
        sunlightHoursField.setText("");
        rateField.setText("7");

        pieChartPanel.setChart(null);
        barChartPanel.setChart(null);
    }

    // Clears all records from DB on Reset
    private void clearDatabase() {
        String sql = "DELETE FROM appliances";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();
            System.out.println("Database cleared.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
