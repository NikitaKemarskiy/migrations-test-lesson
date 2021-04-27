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
import org.nikita.util.LoggerInconsistencyMatcher;
import org.nikita.util.LoggerUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test1619453488176UsersAddUsername {
    private final static String name = "1619453488176-users-add-username";
    private final static String logDirectory = "log";

    private DataSource dataSource;
    private LoggerUtil loggerUtil;
    private Logger logger;
    private Path logFilePath;

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
    public void init() throws SQLException, IOException {
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

        loggerUtil = new LoggerUtil();
        logFilePath = Paths.get(logDirectory, name + ".log");
        logger = loggerUtil.initFileLogger(logFilePath.toAbsolutePath().toString(), name);

        Migrate migrate = new Migrate(dataSource);
        migrate.up(name);
    }

    @Test
    public void testUsernameIsSuccessfullyAddedWithCorrectValue() throws SQLException {
        loggerUtil.logRunningTest(logger, "testUsernameIsSuccessfullyAddedWithCorrectValue");

        List<String> usernames = new LinkedList<>();
        
        usernames.add("nikita");
        usernames.add("paul200");
        usernames.add("alexfilatov");
        usernames.add("yaroslavobruch");
        usernames.add("nikita");

        Iterator<String> usernamesIterator = usernames.iterator();
        LoggerInconsistencyMatcher loggerInconsistencyMatcher = new LoggerInconsistencyMatcher(logger);

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(QueryCollection.getUsersOrderById);

            while (resultSet.next()) {
                String expected = usernamesIterator.next();
                String actual = resultSet.getString("username");

                if (expected.equals(actual)) {
                    loggerInconsistencyMatcher.addMatch();
                } else {
                    loggerInconsistencyMatcher.addInconsistency(resultSet.getRow(), expected, actual);
                }
            }
        }

        loggerInconsistencyMatcher.log();

        Assertions.assertEquals(
                loggerInconsistencyMatcher.getInconsistencies(),
                0,
                String.format("Inconsistencies found. Explore logs at %s", logFilePath.toAbsolutePath().toString())
        );
    }

    @Test
    public void testUsersAreGroupedByWithCorrectUsername() throws SQLException {
        loggerUtil.logRunningTest(logger, "testUsersAreGroupedByWithCorrectUsername");

        List<String> usernames = new LinkedList<>();

        usernames.add("alexfilatov");
        usernames.add("nikita");
        usernames.add("paul200");
        usernames.add("yaroslavobruch");

        Iterator<String> usernamesIterator = usernames.iterator();
        LoggerInconsistencyMatcher loggerInconsistencyMatcher = new LoggerInconsistencyMatcher(logger);

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(QueryCollection.getUsersGroupByUsernameOrderByUsername);

            while (resultSet.next()) {
                String expected = usernamesIterator.next();
                String actual = resultSet.getString("username");

                if (expected.equals(actual)) {
                    loggerInconsistencyMatcher.addMatch();
                } else {
                    loggerInconsistencyMatcher.addInconsistency(resultSet.getRow(), expected, actual);
                }
            }
        }

        loggerInconsistencyMatcher.log();

        Assertions.assertEquals(
            loggerInconsistencyMatcher.getInconsistencies(),
        0,
            String.format("Inconsistencies found. Explore logs at %s", logFilePath.toAbsolutePath().toString())
        );
    }
}
