import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FileUploader {

    public static void main(String[] args) {
        // Show file chooser
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file: " + filePath);

            // Now save the file path to the database
            saveFilePathToDatabase(filePath);
        }
    }

    // Method to save file path to MySQL database
    public static void saveFilePathToDatabase(String filePath) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // 1. Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/elearning", "root", "yourpassword");

            // 3. Prepare SQL query to insert file path into the database
            String insertQuery = "INSERT INTO uploaded_files (file_path) VALUES (?)";
            preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, filePath);

            // 4. Execute the query
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("File path stored successfully in the database.");
            } else {
                System.out.println("Failed to store the file path.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            // 5. Close the database resources
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
