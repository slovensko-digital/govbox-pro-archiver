package digital.slovensko.archiver.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.archiver.core.Archiver;
import digital.slovensko.archiver.core.errors.ResponseNetworkErrorException;
import digital.slovensko.archiver.server.dto.Document;
import digital.slovensko.archiver.server.dto.ErrorResponse;
import digital.slovensko.archiver.server.dto.ExtensionResponse;

import java.io.IOException;
import java.util.Base64;

public class ExtensionEndpoint implements HttpHandler {
    private final Archiver archiver;

    public ExtensionEndpoint(Archiver archiver) {
        this.archiver = archiver;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var document = EndpointUtils.loadFromJsonExchange(exchange, Document.class).getDecodedContent();
            var timestampedDocument = archiver.timestampDocument(document);

        try {
            var b64document = Base64.getEncoder().encodeToString(timestampedDocument.document().openStream().readAllBytes());
            EndpointUtils.respondWith(new ExtensionResponse(b64document), exchange);
        } catch (IOException e) {
            throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
        }


        } catch (JsonSyntaxException | IOException e) {
            EndpointUtils.respondWithError(new ErrorResponse(422, "UNPROCESSABLE_ENTITY", "Error processing request", e.getMessage()), exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
