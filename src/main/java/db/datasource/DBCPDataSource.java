package db.datasource;

import config.Config;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBCPDataSource implements DataSource {
    private BasicDataSource ds = new BasicDataSource();

    public DBCPDataSource() {
        ds.setUrl(Config.getProperty("db.url"));
        ds.setUsername(Config.getProperty("db.username"));
        ds.setPassword("db.password");
        ds.setMinIdle(5);
        ds.setMaxIdle(10);
        ds.setMaxOpenPreparedStatements(100);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
