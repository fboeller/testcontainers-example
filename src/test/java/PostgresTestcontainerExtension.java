import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestcontainerExtension implements AfterEachCallback {

    static {
        var container = new PostgreSQLContainer<>("postgres:11.5");
        container.start();
        try {
            jdbi = Database.initDatabase(container.getJdbcUrl(), container.getUsername(), container.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Jdbi jdbi;

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        var allApplicationTablesQuery = "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = 'public' " +
                "AND table_type='BASE TABLE' " +
                "AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')";
        jdbi.useHandle(handle ->
                handle.createQuery(allApplicationTablesQuery)
                        .mapTo(String.class)
                        .forEach(table -> handle.execute("TRUNCATE " + table + " CASCADE"))
        );
    }
}