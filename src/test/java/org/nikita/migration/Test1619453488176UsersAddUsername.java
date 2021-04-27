package org.nikita.migration;

import db.datasource.DBCPDataSource;
import db.datasource.DataSource;
import db.migrate.Migrate;
import db.query.QueryCollection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.nikita.util.DatabaseUtil;

import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test1619453488176UsersAddUsername {
    private DataSource dataSource;

    private void addUser(String email, String password, Date birthday) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
            "insert into users (email, password, birthday) values (?, ?, ?)"
            );

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.setDate(3, birthday);

            preparedStatement.execute();
        }
    }

    /**
     * Data is initialized according to the tested schema
     * (not to the current data schema, but to the actual
     * for this migration)
     * @throws SQLException
     */
    @BeforeAll
    public void init() throws SQLException {
        dataSource = DBCPDataSource.getDataSource();

        DatabaseUtil databaseUtil = new DatabaseUtil(dataSource);
        databaseUtil.dropTables();

        String initUsersTable = "create table users (id serial primary key, email varchar not null, password varchar not null, birthday date);";
        String initPostsTable = "create table posts (id serial primary key, image varchar not null, caption varchar, likes integer not null default 0, userId integer not null references users(id));";

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            statement.execute(initUsersTable);
            statement.execute(initPostsTable);

            addUser("nikita@gmail.com", "HASHED_PASSWORD", new Date(2001, 07, 24));
            addUser("paul200@mail.ru", "HASHED_PASSWORD", new Date(2000, 01, 01));
            addUser("alexfilatov@mail.ru", "HASHED_PASSWORD", new Date(2001, 02, 01));
            addUser("yaroslavobruch@gmail.com", "HASHED_PASSWORD", new Date(2000, 05, 06));
            addUser("nikita@yandex.ru", "HASHED_PASSWORD", new Date(2001, 07, 24));
        }

        Migrate migrate = new Migrate(dataSource);
        String name = "1619453488176-users-add-username";
        migrate.up(name);
    }

    @Test
    public void testUsernameIsSuccessfullyAddedWithCorrectValue() throws SQLException {
        List<String> usernames = new LinkedList<>();
        
        usernames.add("nikita");
        usernames.add("paul200");
        usernames.add("alexfilatov");
        usernames.add("yaroslavobruch");
        usernames.add("nikita");

        Iterator<String> usernamesIterator = usernames.iterator();
        
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(QueryCollection.getUsersOrderById);

            while (resultSet.next()) {
                Assertions.assertEquals(resultSet.getString("username"), usernamesIterator.next());
            }
        }
    }

    @Test
    public void testUsersAreGroupedByWithCorrectUsername() throws SQLException {
        List<String> usernames = new LinkedList<>();

        usernames.add("alexfilatov");
        usernames.add("nikita");
        usernames.add("paul200");
        usernames.add("yaroslavobruch");

        Iterator<String> usernamesIterator = usernames.iterator();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(QueryCollection.getUsersGroupByUsernameOrderByUsername);

            while (resultSet.next()) {
                Assertions.assertEquals(resultSet.getString("username"), usernamesIterator.next());
            }
        }
    }
}
