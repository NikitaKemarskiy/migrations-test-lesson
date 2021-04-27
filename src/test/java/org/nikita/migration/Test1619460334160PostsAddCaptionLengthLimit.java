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
import org.nikita.util.LoggerUtil;
import org.nikita.util.RandomUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.logging.Logger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Test1619460334160PostsAddCaptionLengthLimit {
    private final static String name = "1619460334160-posts-add-caption-length-limit";
    private final static String logDirectory = "log";

    private DataSource dataSource;
    private LoggerUtil loggerUtil;
    private Logger logger;
    private Path logFilePath;

    private void addUser(String email, String password, Date birthday, String username) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
            "insert into users (email, password, birthday, username) values (?, ?, ?, ?)"
            );

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            preparedStatement.setDate(3, birthday);
            preparedStatement.setString(4, username);

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

        String initUsersTable = "create table users (id serial primary key, email varchar not null, password varchar not null, birthday date, username varchar);";
        String initPostsTable = "create table posts (id serial primary key, image varchar not null, caption varchar, likes integer not null default 0, userId integer not null references users(id));";

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();

            statement.execute(initUsersTable);
            statement.execute(initPostsTable);

            addUser("nikita@gmail.com", "HASHED_PASSWORD", new Date(2001, 07, 24), "nikita");
            addUser("paul200@mail.ru", "HASHED_PASSWORD", new Date(2000, 01, 01), "paul200");
            addUser("alexfilatov@mail.ru", "HASHED_PASSWORD", new Date(2001, 02, 01), "alexfilatov");
        }

        loggerUtil = new LoggerUtil();
        logFilePath = Paths.get(logDirectory, name + ".log");
        logger = loggerUtil.initFileLogger(logFilePath.toAbsolutePath().toString(), name);

        Migrate migrate = new Migrate(dataSource);
        migrate.up(name);
    }

    @Test
    public void testPostIsSuccessfullyAddedIfCaptionLimitIsNotExceeded() throws SQLException {
        loggerUtil.logRunningTest(logger, "testPostIsSuccessfullyAddedIfCaptionLimitIsNotExceeded");

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(QueryCollection.addPost);

            preparedStatement.setString(1, "imageName");
            preparedStatement.setString(2, "caption not exceeding a limit of 2200 characters");
            preparedStatement.setInt(3, 150);
            preparedStatement.setInt(4, 1);

            preparedStatement.execute();
        }
    }

    @Test
    public void testAddPostThrowsExceptionIfCaptionLimitIsExceeded() throws SQLException {
        loggerUtil.logRunningTest(logger, "testPostIsSuccessfullyAddedIfCaptionLimitIsNotExceeded");

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(QueryCollection.addPost);

            String caption = "caption exceeding a limit of 2200 characters: " + new RandomUtil().getRandomString(2200);

            preparedStatement.setString(1, "imageName");
            preparedStatement.setString(2, caption);
            preparedStatement.setInt(3, 150);
            preparedStatement.setInt(4, 1);

            Assertions.assertThrows(
                SQLException.class,
                () -> preparedStatement.execute(),
                "value too long for type character varying(2200)"
            );
        }
    }
}
