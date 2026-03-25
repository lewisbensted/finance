package finance.database;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final String url = "JDBC:sqlite:finance.db";
    private static final SQLiteDataSource ds = new SQLiteDataSource();
    static {ds.setUrl(url);};

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
