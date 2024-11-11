package dashboard;

import database.DBConnection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Chatbox extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public Chatbox() {
        setTitle("Chatbox");

        chatArea = new JTextArea(20, 30);
        inputField = new JTextField(20);
        sendButton = new JButton("Send");

        setLayout(new java.awt.FlowLayout());
        add(new JScrollPane(chatArea));
        add(inputField);
        add(sendButton);

        // Load chat history on startup
        loadChatHistory();

        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                chatArea.append("You: " + message + "\n");
                saveMessageToDatabase("You", message);  // Save message to DB
                inputField.setText("");
            }
        });

        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // Method to load chat history from the database
    private void loadChatHistory() {
        Connection connection = DBConnection.getConnection();
        try {
            String query = "SELECT * FROM chat_history";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                chatArea.append(resultSet.getString("username") + ": " + resultSet.getString("message") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

    // Method to save a message to the database
    private void saveMessageToDatabase(String username, String message) {
        Connection connection = DBConnection.getConnection();
        try {
            String query = "INSERT INTO chat_history (username, message) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(connection);
        }
    }

    public static void main(String[] args) {
        new Chatbox();
    }
}
