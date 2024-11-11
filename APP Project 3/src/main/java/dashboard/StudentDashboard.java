package dashboard;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentDashboard extends JFrame {
    private String username;
    private JPanel cardPanel; // CardLayout to switch between different panels
    private CardLayout cardLayout;

    public StudentDashboard(String username) {
        this.username = username;
        setTitle("Student Dashboard - Welcome " + username);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout); // Set CardLayout on the panel

        // Main panel containing buttons for each option
        JPanel mainPanel = createMainPage();

        // Add the mainPanel to cardPanel
        cardPanel.add(mainPanel, "Main");

        // Set JFrame settings
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER); // Set cardPanel as the center

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    private JPanel createMainPage() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding




        JButton viewFilesButton = new JButton("View Uploaded Files");
        JButton chatButton = new JButton("Chat");
        JButton submitAssignmentButton = new JButton("Submit Assignment");
        JButton quizButton = new JButton("Quiz");
        JButton peerReviewButton = new JButton("Peer Review");


        // Add action listeners to buttons
        viewFilesButton.addActionListener(e -> showViewFilesPanel());
        chatButton.addActionListener(e -> showChatPanel());
        submitAssignmentButton.addActionListener(e -> showSubmitAssignmentPanel());
        quizButton.addActionListener(e -> showQuizPanel());
        peerReviewButton.addActionListener(e -> showPeerReviewPanel());


        // Add buttons to the main panel
        mainPanel.add(viewFilesButton);
        mainPanel.add(chatButton);
        mainPanel.add(submitAssignmentButton);
        mainPanel.add(quizButton);
        mainPanel.add(peerReviewButton);


        return mainPanel;
    }
    void showPeerReviewPanel() {
        // Create peerReviewPanel
        JPanel peerReviewPanel = new JPanel(new BorderLayout());
        JPanel reviewListPanel = new JPanel();
        reviewListPanel.setLayout(new BoxLayout(reviewListPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Assignments Available for Review:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        reviewListPanel.add(titleLabel);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT a.id, a.file_path, a.student_username " +
                             "FROM assignments_submitted1 a " +
                             "LEFT JOIN peer_reviews p ON a.id = p.assignment_id AND p.reviewer_username = ? " +
                             "WHERE p.assignment_id IS NULL AND a.reviewable = TRUE")) {

            statement.setString(1, username); // Set the current user's username
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) { // Check if there are any results
                JLabel noAssignmentsLabel = new JLabel("No assignments available for review at this time.");
                reviewListPanel.add(noAssignmentsLabel);
            } else {
                while (resultSet.next()) {
                    int assignmentId = resultSet.getInt("id");
                    String studentUsername = resultSet.getString("student_username");
                    String filePath = resultSet.getString("file_path");

                    JPanel assignmentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel assignmentLabel = new JLabel("Assignment from: " + studentUsername);

                    // Open File Button
                    JButton openFileButton = new JButton("Open File");
                    openFileButton.addActionListener(e -> openAssignmentFile(filePath));

                    JTextField feedbackField = new JTextField(30);
                    JButton submitFeedbackButton = new JButton("Submit Feedback");

                    submitFeedbackButton.addActionListener(e -> {
                        String feedback = feedbackField.getText();
                        System.out.println("Submitting feedback: " + feedback);

                        if (submitFeedback(assignmentId, feedback)) {
                            JOptionPane.showMessageDialog(null, "Feedback submitted successfully!");
                            System.out.println("Feedback submission successful");
                            showPeerReviewPanel(); // Refresh the panel
                        } else {
                            JOptionPane.showMessageDialog(null, "Failed to submit feedback.");
                            System.out.println("Feedback submission failed");
                        }
                    });

                    assignmentPanel.add(assignmentLabel);
                    assignmentPanel.add(openFileButton);
                    assignmentPanel.add(feedbackField);
                    assignmentPanel.add(submitFeedbackButton);
                    reviewListPanel.add(assignmentPanel);
                }
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading assignments: " + e.getMessage());
            reviewListPanel.add(errorLabel);
            e.printStackTrace();
        }

        // Add the review list to the main peer review panel
        peerReviewPanel.add(new JScrollPane(reviewListPanel), BorderLayout.CENTER);

        // "Back" button to return to the main dashboard
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Main")); // Switch back to the main panel

        // Add the "Back" button at the bottom
        peerReviewPanel.add(backButton, BorderLayout.SOUTH);

        // Ensure panel is added to the card layout and displayed
        cardPanel.add(peerReviewPanel, "PeerReview"); // Add peerReviewPanel to cardPanel
        cardLayout.show(cardPanel, "PeerReview"); // Show peerReviewPanel

        revalidate();
        repaint();
    }



    // Method to open the assignment file in the default application
    private void openAssignmentFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file); // Open file in default application
            } else {
                JOptionPane.showMessageDialog(this, "File not found: " + filePath);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to submit feedback to the database
    // Method to submit feedback to the database
    private boolean submitFeedback(int assignmentId, String feedback) {
        String reviewedStudentUsername = getReviewedStudentUsername(assignmentId);

        // Log assignmentId, username, and reviewedStudentUsername to verify they are correctly retrieved
        System.out.println("Attempting to submit feedback...");
        System.out.println("Assignment ID: " + assignmentId);
        System.out.println("Feedback: " + feedback);
        System.out.println("Reviewer (username): " + username);
        System.out.println("Reviewed Student Username: " + reviewedStudentUsername);

        if (reviewedStudentUsername == null || reviewedStudentUsername.isEmpty()) {
            System.out.println("Error: Reviewed student username is null or empty for assignment ID " + assignmentId);
            return false;
        }

        // Ensure username is not null
        if (username == null || username.isEmpty()) {
            System.out.println("Error: Reviewer username is not set. Ensure 'username' is properly initialized.");
            return false;
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO peer_reviews (assignment_id, reviewer_username, reviewed_student_username, feedback) VALUES (?, ?, ?, ?)")) {

            // Set parameters for the prepared statement
            preparedStatement.setInt(1, assignmentId);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, reviewedStudentUsername);
            preparedStatement.setString(4, feedback);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Feedback successfully saved to the database.");
                return true;
            } else {
                System.out.println("Failed to save feedback: No rows affected.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error saving feedback to the database:");
            e.printStackTrace();
            return false;
        }
    }

    // Method to get the reviewed student's username for an assignment
    private String getReviewedStudentUsername(int assignmentId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT student_username FROM assignments_submitted1 WHERE id = ?")) {

            statement.setInt(1, assignmentId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("student_username");
            } else {
                System.out.println("No student found for assignment ID " + assignmentId);
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving reviewed student username:");
            e.printStackTrace();
            return null;
        }
    }




    // Panel to view uploaded files by teachers
    private void showViewFilesPanel()  {
        JPanel filesPanel = new JPanel(new BorderLayout());
        JPanel fileListPanel = new JPanel();
        fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Files Uploaded by Teachers:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        fileListPanel.add(titleLabel);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT file_path, teacher_username FROM uploaded_files");
             ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.isBeforeFirst()) {
                JLabel noFilesLabel = new JLabel("No files have been uploaded by teachers yet.");
                fileListPanel.add(noFilesLabel);
            } else {
                while (resultSet.next()) {
                    String filePath = resultSet.getString("file_path");
                    String uploadedBy = resultSet.getString("teacher_username");

                    JPanel fileEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JLabel fileLabel = new JLabel("File: " + filePath + " (Uploaded by: " + uploadedBy + ")");
                    JButton openButton = new JButton("Open");

                    openButton.addActionListener(e -> openFile(filePath));

                    fileEntryPanel.add(fileLabel);
                    fileEntryPanel.add(openButton);
                    fileListPanel.add(fileEntryPanel);
                }
            }
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading files: " + e.getMessage());
            fileListPanel.add(errorLabel);
            e.printStackTrace();
        }

        filesPanel.add(new JScrollPane(fileListPanel), BorderLayout.CENTER);

        // "Back" button to return to the main dashboard
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Main"));

        filesPanel.add(backButton, BorderLayout.SOUTH);

        cardPanel.add(filesPanel, "ViewFiles"); // Add filesPanel to cardPanel with identifier "ViewFiles"
        cardLayout.show(cardPanel, "ViewFiles"); // Display the filesPanel

        cardPanel.revalidate();
        cardPanel.repaint();
    }


    // Method to open the selected file
    private void openFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "File not found: " + filePath);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Panel to submit an assignment
    private void showSubmitAssignmentPanel() {
        JPanel submitPanel = new JPanel(new BorderLayout());
        JLabel instructionLabel = new JLabel("Select an assignment file to upload:");
        JTextArea selectedFileArea = new JTextArea(2, 30);
        selectedFileArea.setEditable(false);
        JButton submitButton = new JButton("Submit Assignment");
        JButton backButton = new JButton("Back to Dashboard");

        submitPanel.add(instructionLabel, BorderLayout.NORTH);
        submitPanel.add(selectedFileArea, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(backButton);
        submitPanel.add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                selectedFileArea.setText("Selected file: " + filePath);

                if (saveAssignmentToDatabase(filePath)) {
                    JOptionPane.showMessageDialog(null, "Assignment submitted successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to submit assignment.");
                }
            }
        });

        // Back button to return to dashboard
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Main"));

        // Add submitPanel to cardPanel with identifier "SubmitAssignment" (if not added already)
        cardPanel.add(submitPanel, "SubmitAssignment");
        cardLayout.show(cardPanel, "SubmitAssignment");
    }



    // Method to save submitted assignment to the database
    private boolean saveAssignmentToDatabase(String filePath) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO assignments_submitted1 (student_username, file_path) VALUES (?, ?)")) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, filePath);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error saving assignment:");
            e.printStackTrace();
            return false;
        }
    }

    // Chat panel to display chat messages and send new ones
    private void showChatPanel() {
        // Create chat panel components
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Chat"), BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea(15, 50);
        chatArea.setEditable(false);
        JTextField chatInputField = new JTextField(40);
        JButton sendButton = new JButton("Send");
        JButton backButton = new JButton("Back to Dashboard"); // Back button

        JPanel inputPanel = new JPanel();
        inputPanel.add(chatInputField);
        inputPanel.add(sendButton);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Top panel to hold the back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        chatPanel.add(topPanel, BorderLayout.NORTH);

        // Load chat history from the database
        loadChatHistory(chatArea);

        // Send button action to save message to the database
        sendButton.addActionListener(e -> {
            String message = chatInputField.getText();
            if (!message.isEmpty()) {
                if (saveChatMessageToDatabase(username, message)) {
                    chatArea.append("You: " + message + "\n");
                    chatInputField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send message.");
                }
            }
        });

        // Back button action to return to the main dashboard
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Main"));

        // Add the chatPanel to cardPanel if it hasn’t been added yet
        cardPanel.add(chatPanel, "Chat");
        cardLayout.show(cardPanel, "Chat");
    }



    // Method to load chat history from the database
    private void loadChatHistory(JTextArea chatArea) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT sender_username, message FROM chats ORDER BY timestamp");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String sender = resultSet.getString("sender_username");
                String message = resultSet.getString("message");
                chatArea.append(sender + ": " + message + "\n");
            }
        } catch (SQLException e) {
            System.out.println("Error loading chat history:");
            e.printStackTrace();
        }
    }

    // Method to save chat messages to the database
    private boolean saveChatMessageToDatabase(String senderUsername, String message) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO chats (sender_username, message) VALUES (?, ?)")) {

            preparedStatement.setString(1, senderUsername);
            preparedStatement.setString(2, message);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error saving chat message:");
            e.printStackTrace();
            return false;
        }
    }

    private void showQuizPanel() {
        getContentPane().removeAll();
        JPanel quizSelectionPanel = new JPanel(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Select a Quiz:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        listPanel.add(titleLabel);

        // Fetch all quizzes from the database
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT quiz_id, title FROM quizzes ORDER BY created_date DESC")) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int quizId = resultSet.getInt("quiz_id");
                String quizTitle = resultSet.getString("title");

                JButton quizButton = new JButton(quizTitle);
                quizButton.addActionListener(e -> showQuizPanel(quizId, quizTitle));
                listPanel.add(quizButton);

            }

        } catch (SQLException e) {
            listPanel.add(new JLabel("Error loading quizzes: " + e.getMessage()));
            e.printStackTrace();
        }

        quizSelectionPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        add(quizSelectionPanel);
        revalidate();
        repaint();
    }

    // Display questions for the selected quiz
    private void showQuizPanel(int quizId, String quizTitle) {
        getContentPane().removeAll();
        JPanel quizPanel = new JPanel(new BorderLayout());
        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Quiz: " + quizTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        questionPanel.add(titleLabel);

        JButton submitQuizButton = new JButton("Submit Quiz");


        // Maps to store questions and options
        Map<Integer, String> questionMap = new HashMap<>();
        Map<Integer, String> correctAnswers = new HashMap<>();
        Map<Integer, JRadioButton> selectedOptions = new HashMap<>();

        // Fetch questions for the selected quiz ID
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM questions WHERE quiz_id = ?")) {
            statement.setInt(1, quizId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int questionId = resultSet.getInt("question_id");
                String questionText = resultSet.getString("question_text");
                String optionA = resultSet.getString("option_a");
                String optionB = resultSet.getString("option_b");
                String optionC = resultSet.getString("option_c");
                String optionD = resultSet.getString("option_d");
                String correctAnswer = resultSet.getString("correct_answer");

                questionMap.put(questionId, questionText);
                correctAnswers.put(questionId, correctAnswer);

                questionPanel.add(new JLabel("Question: " + questionText));

                ButtonGroup optionGroup = new ButtonGroup();
                JRadioButton optA = new JRadioButton(optionA);
                JRadioButton optB = new JRadioButton(optionB);
                JRadioButton optC = new JRadioButton(optionC);
                JRadioButton optD = new JRadioButton(optionD);

                optionGroup.add(optA);
                optionGroup.add(optB);
                optionGroup.add(optC);
                optionGroup.add(optD);

                JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                optionsPanel.add(optA);
                optionsPanel.add(optB);
                optionsPanel.add(optC);
                optionsPanel.add(optD);

                questionPanel.add(optionsPanel);

                // Store selected option for each question
                selectedOptions.put(questionId, optA);  // Default selection as example
                optA.addActionListener(e -> selectedOptions.put(questionId, optA));
                optB.addActionListener(e -> selectedOptions.put(questionId, optB));
                optC.addActionListener(e -> selectedOptions.put(questionId, optC));
                optD.addActionListener(e -> selectedOptions.put(questionId, optD));
            }
        } catch (SQLException e) {
            questionPanel.add(new JLabel("Error loading quiz: " + e.getMessage()));
            e.printStackTrace();
        }

        submitQuizButton.addActionListener(e -> {
            int score = 0;
            try (Connection connection = DBConnection.getConnection()) {
                connection.setAutoCommit(false); // Start a transaction

                for (Map.Entry<Integer, JRadioButton> entry : selectedOptions.entrySet()) {
                    int questionId = entry.getKey();
                    JRadioButton selectedOption = entry.getValue();
                    String selectedAnswer = selectedOption.getText();

                    // Check the answer and calculate the score
                    if (selectedAnswer.equals(correctAnswers.get(questionId))) {
                        score++;
                    }

                    // Prepare the SQL statement to save the response
                    String responseQuery = "INSERT INTO responses (student_username, quiz_id, question_id, poll_id, selected_option, response_date) " +
                            "VALUES (?, ?, ?, NULL, ?, NOW())";
                    try (PreparedStatement responseStmt = connection.prepareStatement(responseQuery)) {
                        responseStmt.setString(1, username); // student's username
                        responseStmt.setInt(2, quizId); // quiz ID
                        responseStmt.setInt(3, questionId); // question ID
                        responseStmt.setString(4, selectedAnswer);
                        // selected answer

                        // Execute the insert
                        int rowsInserted = responseStmt.executeUpdate();
                        if (rowsInserted == 0) {
                            throw new SQLException("Failed to insert response for question ID: " + questionId);
                        }
                    }
                }

                // Commit the transaction
                connection.commit();

                // Display the score to the user
                JOptionPane.showMessageDialog(this, "Your Score: " + score + " out of " + questionMap.size());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save quiz responses: " + ex.getMessage());

                // Rollback if any failure occurred
                try (Connection connection = DBConnection.getConnection()) {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        });


        quizPanel.add(new JScrollPane(questionPanel), BorderLayout.CENTER);
        quizPanel.add(submitQuizButton, BorderLayout.SOUTH);
        add(quizPanel);
        revalidate();
        repaint();
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDashboard("student1"));
    }
}
