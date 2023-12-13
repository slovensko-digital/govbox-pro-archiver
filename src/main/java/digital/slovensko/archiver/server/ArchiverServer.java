package digital.slovensko.archiver.server;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import com.sun.net.httpserver.HttpServer;

import digital.slovensko.archiver.core.Archiver;
import digital.slovensko.archiver.server.filters.ArchiverCorsFilter;

public class ArchiverServer {
    private final HttpServer server;
    private final Archiver archiver;

    public ArchiverServer(Archiver archiver, String hostname, int port, ExecutorService executorService) {
        this.archiver = archiver;
        this.server = buildServer(hostname, port);
        this.server.setExecutor(executorService);
    }

    public void start() {
        // Health
        server.createContext("/health", new HealthEndpoint()).getFilters()
                .add(new ArchiverCorsFilter("GET"));

        // Documentation
        server.createContext("/docs", new DocumentationEndpoint());

        // Extend
        server.createContext("/extend", new ExtensionEndpoint(archiver)).getFilters()
                .add(new ArchiverCorsFilter("POST"));

        // Validate
        server.createContext("/validate", new ValidationEndpoint()).getFilters()
                .add(new ArchiverCorsFilter(List.of("POST")));


        // Start server
        server.start();
    }

    private HttpServer buildServer(String hostname, int port) {
        try {
            return HttpServer.create(new InetSocketAddress(hostname, port), 0);


        } catch (BindException e) {
            throw new RuntimeException("error.launchFailed.header port is already in use", e); // TODO

        } catch (Exception e) {
            throw new RuntimeException("error.serverNotCreated", e); // TODO
        }
    }

    public void stop() {
        ((ExecutorService) server.getExecutor()).shutdown(); // TODO find out why requests hang
        server.stop(1);
    }
}
