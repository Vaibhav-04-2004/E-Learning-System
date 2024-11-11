package dashboard;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;

    public RegistrationForm() {
        setTitle("User Registration");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for proper alignment
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBackground(Color.LIGHT_GRAY);

        // Create form elements
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);

        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Student", "Teacher"};
        roleComboBox = new JComboBox<>(roles);

        registerButton = new JButton("Register");
        gbc.insets = new Insets(10, 10, 10, 10);

        // Layout setup
        // Username label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        mainPanel.add(usernameLabel, gbc);

        // Username field
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        mainPanel.add(usernameField, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        mainPanel.add(passwordLabel, gbc);

        // Password field
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        mainPanel.add(passwordField, gbc);

        // Role label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        mainPanel.add(roleLabel, gbc);

        // Role combo box
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        mainPanel.add(roleComboBox, gbc);

        // Login button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        mainPanel.add(registerButton, gbc);
        getContentPane().setBackground(Color.CYAN);  // Set the entire window's background color (Cyan)

        // Add the mainPanel to the JFrame
        add(mainPanel);

        // Register button action listener
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username or password cannot be empty.");
                } else {
                    // Save user to the database
                    registerUser(username, password, role);
                }
            }
        });

        // JFrame settings
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Method to register the user in the database
    private void registerUser(String username, String password, String role) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Get a connection to the database
            connection = DBConnection.getConnection();

            // SQL query to insert the user data into the 'users' table
            String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password); // Consider hashing the password for security
            preparedStatement.setString(3, role.toLowerCase()); // Store role in lowercase

            // Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "User registered successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to register the user.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while registering the user.");
        } finally {
            // Close the resources
            DBConnection.closeConnection(connection);
            try {
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new RegistrationForm();
    }
}

