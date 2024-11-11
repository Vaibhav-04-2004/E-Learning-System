package dashboard;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TeacherDashboard extends JFrame {
    private String username;
    private JPanel cardPanel; // CardLayout to switch between different panels
    private CardLayout cardLayout;

    public TeacherDashboard(String username) {
        this.username = username;
        setTitle("Teacher Dashboard - Welcome " + username);
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
        JPanel mainPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Create buttons for each option
        JButton uploadFileButton = new JButton("Upload File");
        JButton viewSubmittedAssignmentsButton = new JButton("View Submitted Assignments");
        JButton chatButton = new JButton("Chat");
        JButton createQuizButton = new JButton("Create Quiz");

        // Add action listeners for each button
        uploadFileButton.addActionListener(e -> showUploadFilePanel());
        viewSubmittedAssignmentsButton.addActionListener(e -> showSubmittedAssignmentsPanel());
        chatButton.addActionListener(e -> showChatPanel());
        createQuizButton.addActionListener(e -> showCreateQuizPanel());


        // Add buttons to the main panel
        mainPanel.add(uploadFileButton);
        mainPanel.add(viewSubmittedAssignmentsButton);
        mainPanel.add(chatButton);
        mainPanel.add(createQuizButton);

       return mainPanel;
    }
    // Panel to upload a file
    private void showUploadFilePanel() {
        getContentPane().removeAll();

        // Create the back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showMainDashboard());  // Action to go back to the main dashboard

        // Create the upload panel
        JPanel uploadPanel = new JPanel(new BorderLayout());
        JLabel instructionLabel = new JLabel("Select a file to upload:");
        JTextArea selectedFileArea = new JTextArea(2, 30);
        selectedFileArea.setEditable(false);
        JButton uploadButton = new JButton("Upload File");

        // Add components to the panel
        uploadPanel.add(instructionLabel, BorderLayout.NORTH);
        uploadPanel.add(selectedFileArea, BorderLayout.CENTER);
        uploadPanel.add(uploadButton, BorderLayout.SOUTH);

        // Add the back button at the top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST);  // Align the back button to the left
        uploadPanel.add(topPanel, BorderLayout.NORTH);

        add(uploadPanel, BorderLayout.CENTER);

        // Action listener for upload button
        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                selectedFileArea.setText("Selected file: " + filePath);

                // Save the file path to the database
                if (saveFilePathToDatabase(filePath)) {
                    JOptionPane.showMessageDialog(null, "File uploaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to upload file.");
                }
            }
        });

        revalidate();
        repaint();
    }

    // Method to show the main dashboard when 'Back' is clicked
    private void showMainDashboard() {
        // Assuming the previous screen is a main dashboard or the main menu
        // You can use this method to return to the previous dashboard
        // If you're using CardLayout or multiple screens, you can switch the current panel here.

        // Example:
        this.getContentPane().removeAll();
        // Add the main panel (or call another method that shows the main screen)
        JPanel mainPanel = createMainPage();  // Your method to create the main dashboard
        this.add(mainPanel);

        revalidate();
        repaint();
    }


    // Method to save uploaded file path to the database in uploaded_files table
    private boolean saveFilePathToDatabase(String filePath) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO uploaded_files (teacher_username, file_path) VALUES (?, ?)")) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, filePath);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error saving uploaded file path:");
            e.printStackTrace();
            return false;
        }
    }

    // Panel to view submitted assignments
    // Panel to view submitted assignments

    // Panel to view submitted assignments
    private void showSubmittedAssignmentsPanel() {
        getContentPane().removeAll();

        // Create the back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showMainPanel());  // Action to go back to the main page

        // Create the assignment panel
        JPanel assignmentPanel = new JPanel(new BorderLayout());

        // Create a panel to hold the back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST);  // Align the back button to the left
        assignmentPanel.add(topPanel, BorderLayout.NORTH);

        assignmentPanel.add(new JLabel("Submitted Assignments"), BorderLayout.CENTER);

        // List model to store assignments
        DefaultListModel<String> assignmentListModel = new DefaultListModel<>();
        JList<String> assignmentList = new JList<>(assignmentListModel);
        assignmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignmentPanel.add(new JScrollPane(assignmentList), BorderLayout.CENTER);

        JButton openFileButton = new JButton("Open Selected File");
        assignmentPanel.add(openFileButton, BorderLayout.SOUTH);

        // Map to store file paths with student info as the key
        Map<String, String> filePathMap = new HashMap<>();

        // Fetch submitted assignments from the database
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT student_username, file_path FROM assignments_submitted1");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String studentUsername = resultSet.getString("student_username");
                String filePath = resultSet.getString("file_path");
                String displayText = "Student: " + studentUsername + " - File: " + filePath;

                assignmentListModel.addElement(displayText);
                filePathMap.put(displayText, filePath);  // Map display text to file path
            }
        } catch (SQLException e) {
            System.out.println("Error fetching submitted assignments:");
            e.printStackTrace();
        }

        // Open selected file when button is clicked
        openFileButton.addActionListener(e -> {
            String selectedEntry = assignmentList.getSelectedValue();
            if (selectedEntry != null) {
                String filePath = filePathMap.get(selectedEntry);
                try {
                    Desktop.getDesktop().open(new File(filePath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error opening file.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a file to open.");
            }
        });

        add(assignmentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Method to show the main panel when 'Back' is clicked
    private void showMainPanel() {
        getContentPane().removeAll();  // Remove all components

        // Create the main page panel and add it to the content pane
        JPanel mainPanel = createMainPage();  // Assuming 'createMainPage' creates the main dashboard
        add(mainPanel, BorderLayout.CENTER);

        revalidate();  // Refresh the layout
        repaint();
    }




    // Panel to create a new quiz
    // Make quizId a class-level variable
    private int quizId = -1;

    private void showCreateQuizPanel() {
        getContentPane().removeAll();

         // Action to go back to the teacher dashboard
         // Create the back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showMainPage());
        // Create the quiz panel
        JPanel quizPanel = new JPanel(new BorderLayout());

        // Create a panel to hold the back button at the top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST);  // Align the back button to the left
        quizPanel.add(topPanel, BorderLayout.NORTH);

        quizPanel.add(new JLabel("Create New Quiz"), BorderLayout.CENTER);

        // Title input
        JPanel titlePanel = new JPanel(new FlowLayout());
        JLabel titleLabel = new JLabel("Quiz Title:");
        JTextField titleField = new JTextField(20);
        titlePanel.add(titleLabel);
        titlePanel.add(titleField);
        quizPanel.add(titlePanel, BorderLayout.NORTH);

        // Question input section
        JPanel questionPanel = new JPanel(new GridLayout(6, 2));
        questionPanel.add(new JLabel("Question:"));
        JTextField questionField = new JTextField();
        questionPanel.add(questionField);

        questionPanel.add(new JLabel("Option A:"));
        JTextField optionAField = new JTextField();
        questionPanel.add(optionAField);

        questionPanel.add(new JLabel("Option B:"));
        JTextField optionBField = new JTextField();
        questionPanel.add(optionBField);

        questionPanel.add(new JLabel("Option C:"));
        JTextField optionCField = new JTextField();
        questionPanel.add(optionCField);

        questionPanel.add(new JLabel("Option D:"));
        JTextField optionDField = new JTextField();
        questionPanel.add(optionDField);

        questionPanel.add(new JLabel("Correct Answer (A, B, C, or D):"));
        JTextField correctAnswerField = new JTextField();
        questionPanel.add(correctAnswerField);

        quizPanel.add(questionPanel, BorderLayout.CENTER);

        // Button to add question to quiz
        JButton addQuestionButton = new JButton("Add Question");
        JButton saveQuizButton = new JButton("Save Quiz");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addQuestionButton);
        buttonPanel.add(saveQuizButton);
        quizPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(quizPanel, BorderLayout.CENTER);

        // List to display added questions
        DefaultListModel<String> questionListModel = new DefaultListModel<>();
        JList<String> questionList = new JList<>(questionListModel);
        quizPanel.add(new JScrollPane(questionList), BorderLayout.EAST);

        revalidate();
        repaint();

        // List to store the questions before saving
        List<Question> questions = new ArrayList<>();

        // Add action to add question to quiz
        addQuestionButton.addActionListener(e -> {
            String questionText = questionField.getText();
            String optionA = optionAField.getText();
            String optionB = optionBField.getText();
            String optionC = optionCField.getText();
            String optionD = optionDField.getText();
            String correctAnswer = correctAnswerField.getText();

            // If quizId is not set, save the quiz first
            if (quizId == -1) {
                String quizTitle = titleField.getText();
                quizId = saveQuizToDatabase(quizTitle); // Save quiz and get quizId
                if (quizId == -1) {
                    JOptionPane.showMessageDialog(this, "Failed to save quiz.");
                    return; // Exit if saving quiz failed
                }
            }

            // Add question to the list
            questions.add(new Question(questionText, optionA, optionB, optionC, optionD, correctAnswer));
            questionListModel.addElement("Q: " + questionText);

            // Clear fields after adding
            questionField.setText("");
            optionAField.setText("");
            optionBField.setText("");
            optionCField.setText("");
            optionDField.setText("");
            correctAnswerField.setText("");
        });

        // Save quiz and questions to the database
        saveQuizButton.addActionListener(e -> {
            if (quizId != -1 && saveQuestionsToDatabase(quizId, questions)) {
                JOptionPane.showMessageDialog(this, "Quiz and questions saved successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save quiz and questions.");
            }
        });
    }

    // Method to show the teacher dashboard when 'Back' is clicked
    private void showMainPage() {
        // Assuming the previous screen is a main dashboard or the main menu
        // You can use this method to return to the previous dashboard
        // If you're using CardLayout or multiple screens, you can switch the current panel here.

        // Example:
        this.getContentPane().removeAll();
        // Add the main panel (or call another method that shows the main screen)
        JPanel mainPage = createMainPage();  // Your method to create the main dashboard
        this.add(mainPage);

        revalidate();
        repaint();
    }


    // Save quiz to the database and return the generated quiz_id
    private int saveQuizToDatabase(String quizTitle) {
        try (Connection connection = DBConnection.getConnection()) {
            // Save quiz title
            String quizQuery = "INSERT INTO quizzes (teacher_username, title, created_date) VALUES (?, ?, NOW())";
            PreparedStatement quizStmt = connection.prepareStatement(quizQuery, Statement.RETURN_GENERATED_KEYS);
            quizStmt.setString(1, username);  // Assuming 'username' is the teacher's username
            quizStmt.setString(2, quizTitle);
            quizStmt.executeUpdate();

            // Retrieve generated quiz ID
            ResultSet rs = quizStmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated quiz ID
            } else {
                System.out.println("Failed to retrieve generated quiz ID.");
                return -1; // Return -1 if failed
            }
        } catch (SQLException e) {
            System.out.println("Error saving quiz to the database:");
            e.printStackTrace();
            return -1; // Return -1 if failed
        }
    }

    // Save questions to the database with the specified quiz_id
    private boolean saveQuestionsToDatabase(int quizId, List<Question> questions) {
        try (Connection connection = DBConnection.getConnection()) {
            // Save each question to the questions table
            for (Question q : questions) {
                String questionQuery = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement questionStmt = connection.prepareStatement(questionQuery);
                questionStmt.setInt(1, quizId); // Associate the question with the quiz_id
                questionStmt.setString(2, q.question);
                questionStmt.setString(3, q.optionA);
                questionStmt.setString(4, q.optionB);
                questionStmt.setString(5, q.optionC);
                questionStmt.setString(6, q.optionD);
                questionStmt.setString(7, q.correctAnswer);

                // Check if question insertion succeeds
                int questionRowsAffected = questionStmt.executeUpdate();
                if (questionRowsAffected <= 0) {
                    System.out.println("Failed to save question: " + q.question);
                    return false;
                }
            }
            return true; // Return true if all questions were saved successfully
        } catch (SQLException e) {
            System.out.println("Error saving questions to the database:");
            e.printStackTrace();
            return false; // Return false if there was an error
        }
    }




    private void showChatPanel() {
        System.out.println("Opening chat panel..."); // Debugging output

        // Check if chat panel already exists, if not, create and add it
        if (cardPanel.getComponentCount() == 1 || cardPanel.getComponent(1) == null) {
            // Create the chat panel layout
            JPanel chatPanel = new JPanel(new BorderLayout());

            // Back button to go back to the main dashboard
            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> {
                System.out.println("Back button clicked"); // Debugging output
                cardLayout.show(cardPanel, "Main");
            });

            // Top panel to hold the Back button and chat label
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(backButton, BorderLayout.WEST);
            topPanel.add(new JLabel("Chat"), BorderLayout.CENTER);
            chatPanel.add(topPanel, BorderLayout.NORTH);

            // Chat area setup
            JTextArea chatArea = new JTextArea(15, 50);
            chatArea.setEditable(false);

            // Chat input area
            JTextField chatInputField = new JTextField(40);
            JButton sendButton = new JButton("Send");

            // Add chat area and input field
            JPanel inputPanel = new JPanel();
            inputPanel.add(chatInputField);
            inputPanel.add(sendButton);
            chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
            chatPanel.add(inputPanel, BorderLayout.SOUTH);

            // Load chat history from the database
            loadChatHistory(chatArea);

            // Send button action
            sendButton.addActionListener(e -> {
                String message = chatInputField.getText();
                if (!message.isEmpty()) {
                    System.out.println("Sending message: " + message); // Debugging output
                    if (saveChatMessageToDatabase(username, message)) {
                        chatArea.append("You: " + message + "\n");
                        chatInputField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to send message.");
                    }
                }
            });

            // Add chat panel to the card panel if not already added
            cardPanel.add(chatPanel, "Chat");
        }

        // Show chat panel
        cardLayout.show(cardPanel, "Chat");
        cardPanel.revalidate();
        cardPanel.repaint();

        System.out.println("Chat panel should now be visible."); // Debugging output
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

    public static void main(String[] args) {
        new TeacherDashboard("teacher1");
    }


    class Question {
        String question, optionA, optionB, optionC, optionD, correctAnswer;

        public Question(String question, String optionA, String optionB, String optionC, String optionD, String correctAnswer) {
            this.question = question;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.correctAnswer = correctAnswer;
        }
    }
}
