package digital.slovensko.archiver.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.archiver.core.SignatureValidator;
import digital.slovensko.archiver.core.errors.DocumentNotSignedYetException;
import digital.slovensko.archiver.server.dto.Document;
import digital.slovensko.archiver.server.dto.ErrorResponse;
import digital.slovensko.archiver.server.dto.ValidationResponseBody;
import eu.europa.esig.dss.model.DSSDocument;

import java.io.IOException;

public class ValidationEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        DSSDocument document = null;
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, Document.class);
            if (body.content() == null)
                throw new IllegalArgumentException("Document to validate is not provided.");

            document = body.getDecodedContent();
            if (document == null || document.openStream().readAllBytes().length < 1)
                throw new IllegalArgumentException("Document to validate is null.");

        } catch (JsonSyntaxException | IOException | IllegalArgumentException e) {
            EndpointUtils.respondWithError(new ErrorResponse(422, "UNPROCESSABLE_ENTITY", "Error processing request", e.getMessage()), exchange);
        }

        try {
            var reports = SignatureValidator.getInstance().validate(document);
            if (reports == null) {
                EndpointUtils.respondWithError(ErrorResponse.buildFromException(new DocumentNotSignedYetException()), exchange);
                return;
            }

            var responseBody = ValidationResponseBody.build(reports, document);
            if (responseBody == null) {
                EndpointUtils.respondWithError(new ErrorResponse(400, "VALIDATION_FAILED", "Validation failed for the given document", "Validation failed for the given document"), exchange);
                return;
            }

            EndpointUtils.respondWith(responseBody, exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
