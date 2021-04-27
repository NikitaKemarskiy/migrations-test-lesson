package org.nikita.util;

import db.datasource.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
    private DataSource dataSource;

    public DatabaseUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void dropTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            statement.execute("drop table posts;");
            statement.execute("drop table users;");
            statement.execute("drop table migrations;");
        }
    }
}
