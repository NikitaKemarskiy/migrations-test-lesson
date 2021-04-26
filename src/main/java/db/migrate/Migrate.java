package db.migrate;

import config.Config;
import db.datasource.DataSource;
import db.query.QueryCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Migrate {
    private DataSource dataSource;
    private File migrationsDirectory;

    public Migrate(DataSource dataSource) {
        try {
            this.dataSource = dataSource;

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();

            statement.execute(QueryCollection.initMigrationsDatabase);

            migrationsDirectory = new File(
                Migrate.class.getClassLoader().getResource(Config.getProperty("migrations.directory")
            ).getFile());
        } catch (SQLException err) {
            System.err.println(err);
            System.exit(1);
        }
    }

    public void up() {
        try {
            Connection connection = dataSource.getConnection();

            List<File> migrationDirectories = Arrays.stream(migrationsDirectory.listFiles())
                .filter(file -> {
                    try {
                        PreparedStatement statement = connection.prepareStatement(QueryCollection.getMigrationByName);
                        statement.setString(1, file.getName());
                        ResultSet resultSet = statement.executeQuery();
                        return !resultSet.next();
                    } catch (SQLException err) {
                        System.err.println(err);
                        return false;
                    }
                })
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

            for (File file : migrationDirectories) {
                System.out.printf(">>> Running migration: %s%n", file.getName());

                Path upSQLScriptPath = Paths.get(file.getPath(), "up.sql");
                String upSQLScript = String.join(" ", Files.readAllLines(upSQLScriptPath));

                Statement upStatement = connection.createStatement();
                upStatement.execute(upSQLScript);

                PreparedStatement addMigrationStatement = connection.prepareStatement(QueryCollection.addMigration);
                addMigrationStatement.setString(1, file.getName());
                addMigrationStatement.execute();
            }
        } catch (IOException | SQLException err) {
            System.err.println(err);
            System.exit(1);
        }
    }

    public void down() {
        try {
            Connection connection = dataSource.getConnection();

            List<File> migrationDirectories = Arrays.stream(migrationsDirectory.listFiles())
                    .filter(file -> {
                        try {
                            PreparedStatement statement = connection.prepareStatement(QueryCollection.getMigrationByName);
                            statement.setString(1, file.getName());
                            ResultSet resultSet = statement.executeQuery();
                            return resultSet.next();
                        } catch (SQLException err) {
                            System.err.println(err);
                            return false;
                        }
                    })
                    .sorted(Comparator.comparing(File::getName).reversed())
                    .collect(Collectors.toList());

            for (File file : migrationDirectories) {
                System.out.printf(">>> Downgrading migration: %s%n", file.getName());

                Path downSQLScriptPath = Paths.get(file.getPath(), "down.sql");
                String downSQLScript = String.join(" ", Files.readAllLines(downSQLScriptPath));

                Statement upStatement = connection.createStatement();
                upStatement.execute(downSQLScript);

                PreparedStatement addMigrationStatement = connection.prepareStatement(QueryCollection.deleteMigration);
                addMigrationStatement.setString(1, file.getName());
                addMigrationStatement.execute();
            }
        } catch (IOException | SQLException err) {
            System.err.println(err);
            System.exit(1);
        }
    }
}
