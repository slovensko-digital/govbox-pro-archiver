package digital.slovensko.archiver.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.archiver.core.SignatureValidator;
import digital.slovensko.archiver.server.dto.HealthResponse;

import java.io.IOException;

public class HealthEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("HealthEndpoint before");
        var validator = SignatureValidator.getInstance();
        System.out.println("HealthEndpoint after");

        var response = new HealthResponse(validator.areTLsLoaded(), validator.loadedTLs());
        var gson = new Gson();

        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
        }
    }
}
