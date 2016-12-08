package example;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.mysql.MySQLStatementInterceptorManagementBean;
import org.junit.*;

public class StatementInterceptorTest
{
    // use other port to avoid conflict with system MySQL service
    private static final int port = 3307;
    private static final String dbName = "test";

    private String connectionURL;
    private DB db;

    private MySQLStatementInterceptorManagementBean interceptorManagementBean;

    @Before
    public void setUp() throws ManagedProcessException {
        final DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder().setPort(port);
        connectionURL = builder.getURL(dbName);
        db = DB.newEmbeddedDB(builder.build());
        db.start();
        db.source("create_table.sql");

        final Brave brave = new Brave.Builder().build();
        interceptorManagementBean = new MySQLStatementInterceptorManagementBean(brave.clientTracer());
    }

    @After
    public void tearDown() throws ManagedProcessException, IOException {
        interceptorManagementBean.close();
        db.stop();
    }

    @Test
    public void testWithoutRewriteBatchedStatements() throws SQLException {
        executeBatchQuery(createProperties(false));
    }

    @Test
    public void testWithRewriteBatchedStatements() throws SQLException {
        executeBatchQuery(createProperties(true));
    }

    private static Properties createProperties(boolean rewriteBatchedStatements) {
        final Properties properties = new Properties();
        properties.put("user", "root");
        properties.put("password", "");
        properties.put("rewriteBatchedStatements", Boolean.toString(rewriteBatchedStatements));
        properties.put("statementInterceptors", "com.github.kristofa.brave.mysql.MySQLStatementInterceptor");
        return properties;
    }

    private void executeBatchQuery(Properties properties) throws SQLException {
        final String sql = "INSERT INTO test.test (id, name) VALUES (?, ?)";
        try (
                Connection conn = DriverManager.getConnection(connectionURL, properties);
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            for (int i = 0; i < 10; i++) {
                ps.setInt(1, i);
                ps.setString(2, "name" + i);
                ps.addBatch();
            }

            ps.executeBatch();
        }

    }
}
