package datasource.example;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// TODO: Name this bean "chatLogBean"
// TODO: Make this a SessionScoped bean
@Named("chatLogBean")
@SessionScoped
public class Messages implements Serializable{
    private List<String> chatLog = new LinkedList<>();
    private Connection conn;
    private String messageToPost;
    @Inject private UserLogin login;

    // TODO: Have this method execute immediately after bean construction using annotations
    @PostConstruct
    public void openConnection(){
        // TODO: In a try block
            // TODO: create a new InitialContext
            // TODO: Get the DataSource by lookup with jdbc/Assignment2 under the /comp/env/ in the context
            // TODO: Use getConnection on the datasource to assign the conn field
        // TODO: catch Naming and SQL exception's
            // TODO: print the exceptions message to System.out
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/Assignment2");
            this.conn = ds.getConnection();
        } catch (NamingException | SQLException e) {
            System.out.println(e.getMessage());
        }
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

    public void pullLog() {
        // TODO: In a try-with-resources resource block
            // TODO: Create the following resources:
            // TODO: 1. A PreparedStatement that selects "message" and "sentOn" from the messages table, and;
            //                                           "username" from the suers table by;
            //                                           joining the users and messages table on the "userID" columns in both tables
            // TODO: 2. A ResultSet by executing the PreparedStatement's query
        // TODO: In the body
            // TODO: clear the chatLog
            // TODO: while the ResultSet has more entries
                // TODO: Create a string that looks like "<username>@<sentOn>: <message>" where:
                //          <username> is the username String for this row in the ResultSet
                //          <sentOn> is the sentOn Timestamp for this row in the ResultSet
                //          <message> is the message String for this row in the ResultSet
                // TODO: Add that String to the chatLog
        // TODO: Catch SQL Exceptions
            // TODO: print the exception's message to System.out
        try (
            PreparedStatement stmt = conn.prepareStatement("SELECT messages.message, messages.sentOn, users.username FROM messages INNER JOIN users ON messages.userID = users.userID");
            ResultSet rs = stmt.executeQuery();
        ) {
            chatLog.clear();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append(rs.getString(3));
                sb.append("@");
                sb.append(rs.getTimestamp(2));
                sb.append(": ");
                sb.append(rs.getString(1));
                chatLog.add(sb.toString());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void postMessage(){
        // TODO: In a try-with-resources resource block
            // TODO: Create the following resource:
            // TODO: 1. A PreparedStatement that inserts into the messages table,
            //                  the columns "message", "sentOn" and "userID"
            //                  using Parameter Markers for all 3 values
        // TODO: In the body
            // TODO: set "message"'s Placemarker to messageToPost
            // TODO: set "sentOn"'s Placemarker to new Timestamp(Instant.now().toEpochMilli())
            // TODO: set "userID"'s Placemarker to login.getUserId()
            // TODO: execute the statement
        // TODO: Catch SQL Exceptions
            // TODO: print the exception's message to System.out
        // TODO: call pullLog();
        try {PreparedStatement stmt = conn.prepareStatement("INSERT INTO messages (message, sentOn, userID) VALUES (?, ?, ?)");
            stmt.setString(1, messageToPost);
            stmt.setTimestamp(2, new Timestamp(Instant.now().toEpochMilli()));
            stmt.setInt(3, login.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        pullLog();
    }

    public List<String> getChatLog(){
        return chatLog;
    }

    public String getMessageToPost() {
        return messageToPost;
    }

    public void setMessageToPost(String messageToPost) {
        this.messageToPost = messageToPost;
    }
}
