import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.UUID;

import static io.vertx.core.http.HttpMethod.POST;

public class Main {

    public static void main(String[] args) throws Exception {
        migrateDatabase(createPostgresDataSource());
        var vertx = Vertx.vertx();
        var router = configureRouter(Router.router(vertx));
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(4201);
    }

    private static DataSource createPostgresDataSource() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setDatabaseName("postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("secret");
        // TODO Will this work on MacOS?
        dataSource.setURL("jdbc:postgresql://localhost:4202/postgres");
        return dataSource;
    }

    public static void migrateDatabase(DataSource dataSource) throws Exception {
        try (var connection = dataSource.getConnection()) {
            var migrator = new Liquibase(
                    "migrations.xml",
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(connection)
            );
            migrator.update("");
        }
    }

    private static Router configureRouter(Router router) {
        router.route(POST, "/run").handler(routingContext -> {
            var response = routingContext.response();
            response.end("Hello world!");
        });
        return router;
    }

    public static Jdbi configureJdbi(Jdbi jdbi) {
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerRowMapper(Run.class, (rs, ctx) -> new Run(
                rs.getObject("id", UUID.class),
                rs.getString("status")
        ));
        return jdbi;
    }
}
