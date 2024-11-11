import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Chatbox extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    // Database connection variables
    private Connection connection;
    private PreparedStatement preparedStatement;

    public Chatbox() {
        setTitle("Chatbox");

        // Initialize chat area and input field
        chatArea = new JTextArea(20, 30);
        chatArea.setEditable(false); // Chat area should be read-only
        inputField = new JTextField(20);
        sendButton = new JButton("Send");

        // Layout
        setLayout(new java.awt.FlowLayout());
        add(new JScrollPane(chatArea));
        add(inputField);
        add(sendButton);

        // Load chat history from the database when the chatbox starts
        loadChatHistory();

        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.trim().isEmpty()) {
                    chatArea.append("You: " + message + "\n");

                    // Store the message in the database
                    saveMessageToDatabase("You", message);

                    // Clear input field
                    inputField.setText("");
                }
            }
        });

        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Initialize database connection
        connectToDatabase();
    }

    // Method to connect to the MySQL database
    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/elearning", "root", "Vaibhav@123");
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save the message to the database
    private void saveMessageToDatabase(String sender, String message) {
        try {
            String query = "INSERT INTO chat (sender, message) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, message);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to load chat history from the database
    private void loadChatHistory() {
        try {
            // Connect to the database and query for chat history
            connectToDatabase();
            String query = "SELECT sender, message FROM chat ORDER BY timestamp ASC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Append each message to the chat area
            while (resultSet.next()) {
                String sender = resultSet.getString("sender");
                String message = resultSet.getString("message");
                chatArea.append(sender + ": " + message + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Close database resources
    private void closeDatabaseConnection() {
        try {
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Chatbox();
    }
}
