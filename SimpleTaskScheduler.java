// Simple Task Scheduler for Beginners
// This program helps you manage your daily tasks

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// Main class - This is where our program starts
public class SimpleTaskScheduler extends JFrame {
    
    // Database connection details
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String DB_NAME = "simple_tasks";
    static final String USER = "root";
    static final String PASS = "Simple_Task";  // CHANGE THIS to your MySQL password
    
    // GUI components (buttons, text fields, etc.)
    JTextField taskNameField;
    JTextArea taskDetailsArea;
    JComboBox<String> statusBox;
    JTable taskTable;
    DefaultTableModel tableModel;
    
    // Constructor - Sets up the window and all components
    public SimpleTaskScheduler() {
        // Set window title
        setTitle("My Task Scheduler");
        
        // Set window size (width, height)
        setSize(800, 500);
        
        // Close program when window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create the user interface
        createUI();
        
        // Load all tasks from database
        loadAllTasks();
    }
    
    // Method to create the user interface
    void createUI() {
        // Main container with border layout
        setLayout(new BorderLayout(10, 10));
        
        // TOP SECTION - Input fields
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Task Name
        inputPanel.add(new JLabel("Task Name:"));
        taskNameField = new JTextField();
        inputPanel.add(taskNameField);
        
        // Task Details
        inputPanel.add(new JLabel("Task Details:"));
        taskDetailsArea = new JTextArea(2, 20);
        inputPanel.add(new JScrollPane(taskDetailsArea));
        
        // Status dropdown
        inputPanel.add(new JLabel("Status:"));
        String[] statuses = {"To Do", "In Progress", "Done"};
        statusBox = new JComboBox<>(statuses);
        inputPanel.add(statusBox);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        JButton updateButton = new JButton("Update Task");
        JButton deleteButton = new JButton("Delete Task");
        JButton clearButton = new JButton("Clear");
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        inputPanel.add(new JLabel()); // Empty space
        inputPanel.add(buttonPanel);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // MIDDLE SECTION - Task table
        String[] columns = {"ID", "Task Name", "Details", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        taskTable = new JTable(tableModel);
        
        // When user clicks a row, load that task
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = taskTable.getSelectedRow();
                if (row >= 0) {
                    taskNameField.setText(tableModel.getValueAt(row, 1).toString());
                    taskDetailsArea.setText(tableModel.getValueAt(row, 2).toString());
                    statusBox.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                }
            }
        });
        
        add(new JScrollPane(taskTable), BorderLayout.CENTER);
        
        // Button actions
        addButton.addActionListener(e -> addTask());
        updateButton.addActionListener(e -> updateTask());
        deleteButton.addActionListener(e -> deleteTask());
        clearButton.addActionListener(e -> clearFields());
    }
    
    // Method to add a new task
    void addTask() {
        String name = taskNameField.getText().trim();
        String details = taskDetailsArea.getText().trim();
        String status = (String) statusBox.getSelectedItem();
        
        // Check if task name is empty
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task name!");
            return;
        }
        
        try {
            // Connect to database
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
            
            // SQL command to insert new task
            String sql = "INSERT INTO tasks (task_name, details, status) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, details);
            pstmt.setString(3, status);
            
            // Execute the insert
            pstmt.executeUpdate();
            
            // Close connection
            pstmt.close();
            conn.close();
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Task added successfully!");
            
            // Clear fields and reload table
            clearFields();
            loadAllTasks();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    // Method to update an existing task
    void updateTask() {
        int row = taskTable.getSelectedRow();
        
        // Check if user selected a task
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to update!");
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        String name = taskNameField.getText().trim();
        String details = taskDetailsArea.getText().trim();
        String status = (String) statusBox.getSelectedItem();
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
            
            // SQL command to update task
            String sql = "UPDATE tasks SET task_name=?, details=?, status=? WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, details);
            pstmt.setString(3, status);
            pstmt.setInt(4, id);
            
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            
            JOptionPane.showMessageDialog(this, "Task updated successfully!");
            clearFields();
            loadAllTasks();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    // Method to delete a task
    void deleteTask() {
        int row = taskTable.getSelectedRow();
        
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete!");
            return;
        }
        
        // Ask for confirmation
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this task?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
            
            // SQL command to delete task
            String sql = "DELETE FROM tasks WHERE id=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            pstmt.executeUpdate();
            pstmt.close();
            conn.close();
            
            JOptionPane.showMessageDialog(this, "Task deleted successfully!");
            clearFields();
            loadAllTasks();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    // Method to load all tasks from database into table
    void loadAllTasks() {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
            
            // SQL command to get all tasks
            String sql = "SELECT * FROM tasks ORDER BY id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            // Loop through results and add to table
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("task_name");
                String details = rs.getString("details");
                String status = rs.getString("status");
                
                tableModel.addRow(new Object[]{id, name, details, status});
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }
    
    // Method to clear all input fields
    void clearFields() {
        taskNameField.setText("");
        taskDetailsArea.setText("");
        statusBox.setSelectedIndex(0);
        taskTable.clearSelection();
    }
    
    // Method to setup database and table
    static void setupDatabase() {
        try {
            // First, create the database
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            
            String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDB);
            System.out.println("Database created successfully!");
            
            stmt.close();
            conn.close();
            
            // Now create the tasks table
            conn = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
            stmt = conn.createStatement();
            
            String createTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                "task_name VARCHAR(200) NOT NULL, " +
                                "details TEXT, " +
                                "status VARCHAR(50))";
            stmt.executeUpdate(createTable);
            System.out.println("Table created successfully!");
            
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("Error setting up database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Main method - Program starts here
    public static void main(String[] args) {
        // Setup database first
        setupDatabase();
        
        // Create and show the window
        SwingUtilities.invokeLater(() -> {
            SimpleTaskScheduler window = new SimpleTaskScheduler();
            window.setVisible(true);
        });
    }
}