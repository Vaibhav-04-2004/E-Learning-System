package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database URL, username, and password (modify these as per your configuration)
    private static final String URL = "jdbc:mysql://localhost:3306/elearning";
    private static final String USER = "root";  // Replace with your MySQL username
    private static final String PASSWORD = "Vaibhav@123";  // Replace with your MySQL password

    // Method to establish and return a connection to the MySQL database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish connection using the URL, username, and password
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/elearning", "root", "Vaibhav@123");
        } catch (SQLException e) {
            // Handle SQL-related errors
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // Handle errors if the JDBC driver class is not found
            e.printStackTrace();
        }
        // Return the connection object
        return connection;
    }

    // Method to close the connection (optional for better resource management)
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
