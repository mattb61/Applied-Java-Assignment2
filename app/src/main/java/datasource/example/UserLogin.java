package datasource.example;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// TODO: Name this bean "userLoginBean"
// TODO: Make this a SessionScoped bean
@Named("userLoginBean")
@SessionScoped
public class UserLogin implements Serializable{
    @NotNull
    private String userName;
    @NotNull
    @NotBlank
    @Size(min=12,max=36)
    private String userPassword;
    private String token;
    private String message;
    private int userId;
    private Connection conn;

    private boolean verifyPassword(byte[] bytes) throws UnsupportedEncodingException{
        return BCrypt.verifyer().verify(userPassword.getBytes("UTF-16"), bytes).verified;
    }


    // TODO: Have this method execute immediately after bean construction using annotations
    @PostConstruct
    public void openConnection(){
        // TODO: In a try block
            // TODO: set token to null
            // TODO: create a new InitialContext
            // TODO: Get the DataSource by lookup with jdbc/Assignment2 under the /comp/env/ in the context
            // TODO: Use getConnection on the datasource to assign the conn field
        try {
            token = null;
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/Assignment2");
            this.conn = ds.getConnection();
        } catch (NamingException | SQLException e) {
            System.out.println(e.getMessage());
        }
        // TODO: catch Naming and SQL exception's
            // TODO: print the exceptions message to System.out
    }

    // TODO: Have this method execute immediately before bean destruction using annotations
    @PreDestroy
    public void closeConnection(){
        // TODO: If conn isn't null
            // TODO: in a try-block
                // TODO: close conn
        // TODO: Catch SQL Exceptions
            // TODO: print the exception's message to System.out
            if (this.conn != null) {
                try {
                    this.conn.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
    }

    public void updateToken() {
        // TODO: In a try-with-resources block
            // TODO: Create the following resource:
            // TODO: 1. A PreparedStatement that selects "userID", "token", and "PW_Hash" from the users table, where;
            //                                           "username" equals a Parameter Marker
        // TODO: In the body
            // TODO: set the "username"'s Parameter Marker to userName
            // TODO: In a try-with-resources block
                // TODO: Create the following resource:
                // TODO: 1. A ResultSet from executing the PreparedStatement's query
                // TODO: if rs.next() is true, get the bytes from PW_Hash and call verifyPassword with them; if that is also true
                    // TODO: set token to the token from the ResultSet
                    // TODO: set userID to the userID from the ResultSet
                    // TODO: set message to the empty string
                // TODO: if either condition above was false
                    // TODO: set message to "Invalid login"
            // TODO: Catch SQL and UnsupportedEncoding Exceptions
                // TODO: set message to the exception's message
                // TODO: return
        // TODO: Catch SQL Exceptions
            // TODO: set message to the exception's message
            // TODO: return
        try (PreparedStatement stmt = conn.prepareStatement("SELECT userID, token, PW_Hash FROM users WHERE username = ?")){
            
            stmt.setString(1, userName);
            try {ResultSet rs = stmt.executeQuery();
                if (rs.next() == true) {
                    if (verifyPassword(rs.getBytes("PW_Hash"))) {
                        token = rs.getString("token");
                        userId = rs.getInt("userID");
                        message = "";
                    } else {
                        message = "Invalid login";
                    }
                }
            } catch (SQLException | UnsupportedEncodingException e) {
                message = e.getMessage();
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void signup() {
        
        // TODO: In a try-with-resources resource block
            // TODO: Create the following resource:
            // TODO: 1. A PreparedStatement that inserts into the users table,
            //                  the columns "username", "PW_Hash" and "token"
            //                  using Parameter Markers for "username" and "PW_Hash" values, and;
            //                  using SHA2(RAND(), 256) for "token"'s value
        // TODO: In the body
            // TODO: ensure the following line is inside the try body
            // byte[] hash = BCrypt.withDefaults().hash(12, userPassword.getBytes("UTF-16"));
            // TODO: set the "username" Parameter marker to userName
            // TODO: set the "PW_Hash" Parameter marker to hash
            // TODO: execute the statement as an update and store the number of affected rows in an integer variable
            // TODO: If the integer variable is not 1
                // TODO: set message to "Failed to create new user: <username>" with <username> replaced with userName's value
            // TODO: else
                // TODO: set message to "Successfully created new user: <username>" with <username> replaced with userName's value
        // TODO: Catch SQL and UnsupportedEncoding Exceptions
            // TODO: set message to the exception's message
            // TODO: return
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, PW_Hash, token) VALUES (?, ?, SHA2(RAND(), 256))")) {
            
            byte[] hash = BCrypt.withDefaults().hash(12, userPassword.getBytes("UTF-16"));
            stmt.setString(1, userName);
            stmt.setBytes(2, hash);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                message = "Failed to create new user: " + userName;
            } else {
                message = "Successfully created new user: " + userName;
            }
        } catch (SQLException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserPassword() {
        return userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getToken() {
        return token;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getMessage() {
        return message;
    }
}
