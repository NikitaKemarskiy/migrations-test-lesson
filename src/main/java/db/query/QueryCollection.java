package db.query;

public class QueryCollection {
    private QueryCollection() {}

    public final static String initMigrationsDatabase = "create table if not exists migrations (name varchar not null unique);";
    public final static String getMigrationByName = "select * from migrations where name = ?;";
    public final static String addMigration = "insert into migrations (name) values (?);";
    public final static String deleteMigration = "delete from migrations where name = ?;";
}
