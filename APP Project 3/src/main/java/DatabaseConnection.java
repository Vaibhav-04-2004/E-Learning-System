import java.sql.*;

public class DatabaseConnection {
    private Connection connection;

    public DatabaseConnection() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/elearning", "root", "yourpassword");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkLogin(String username, String password) {
        try {
            // Query to check if user exists
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            // If user exists
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
