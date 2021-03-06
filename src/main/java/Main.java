import db.datasource.DBCPDataSource;
import db.datasource.DataSource;
import db.migrate.Migrate;

public class Main {
    public static void main(String[] args) {
        DataSource dataSource = DBCPDataSource.getDataSource();
        Migrate migrate = new Migrate(dataSource);

        migrate.up();
    }
}
