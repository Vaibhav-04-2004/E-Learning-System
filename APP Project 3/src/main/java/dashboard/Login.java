package dashboard;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;



public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;

    public Login() {
        setTitle("User Login");

        Image backgroundImage;
        try {
            BufferedImage originalImage = ImageIO.read(new File("C:\\Users\\vaibh\\Desktop\\login.jpg")); // Update path
            backgroundImage = originalImage.getScaledInstance(600, 400, Image.SCALE_SMOOTH); // Scale to frame size
            System.out.println("Background image loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            backgroundImage = null;
            System.out.println("Failed to load background image.");
        }


        BackgroundPanel mainPanel = new BackgroundPanel(backgroundImage);
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

        loginButton = new JButton("Login");

        gbc.insets = new Insets(10, 10, 10, 10);

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
        mainPanel.add(loginButton, gbc);
        getContentPane().setBackground(Color.CYAN);
        add(mainPanel);

        // Login button action listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String role = (String) roleComboBox.getSelectedItem();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username or password cannot be empty.");
                } else {
                    // Verify login credentials
                    if (loginUser(username, password, role)) {
                        JOptionPane.showMessageDialog(null, "Login successful!");

                        // Redirect based on role
                        if (role.equalsIgnoreCase("teacher")) {
                            new TeacherDashboard(username);
                        } else if (role.equalsIgnoreCase("student")) {
                            new StudentDashboard(username);
                        }

                        dispose(); // Close the login window
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid credentials. Please try again.");
                    }
                }
            }
        });

        // JFrame settings
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Method to verify the user's credentials from the database
    private boolean loginUser(String username, String password, String role) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Get database connection
            connection = DBConnection.getConnection();

            // SQL query to check if the user exists with the provided username, password, and role
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password); // If you're using hashing, compare hashed passwords
            preparedStatement.setString(3, role.toLowerCase());

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            // Return true if a matching user is found
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the resources
            DBConnection.closeConnection(connection);
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false; // Return false if no user matches the provided credentials
    }

    public static void main(String[] args) {
        new Login();
    }
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}
