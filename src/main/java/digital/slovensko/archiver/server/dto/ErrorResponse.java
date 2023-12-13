package digital.slovensko.archiver.server.dto;

import digital.slovensko.archiver.core.errors.ArchiverException;

public class ErrorResponse {
    private final int statusCode;
    private final ErrorResponseBody body;

    private ErrorResponse(int statusCode, ErrorResponseBody body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    private ErrorResponse(int statusCode, String code, ArchiverException e) {
        this(statusCode, new ErrorResponseBody(code, e.getSubheading(), e.getDescription()));
    }

    public ErrorResponse(int statusCode, String code, String message, String details) {
        this(statusCode, new ErrorResponseBody(code, message, details));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorResponseBody getBody() {
        return body;
    }

    public static ErrorResponse buildFromException(Exception e) {
        return switch (e.getClass().getSimpleName()) {
            case "UnrecognizedException" -> new ErrorResponse(502, "UNRECOGNIZED_DSS_ERROR", (ArchiverException) e);
            case "OriginalDocumentNotFoundException" -> new ErrorResponse(422, "ORIGINAL_DOCUMENT_NOT_FOUND", (ArchiverException) e);
            default -> new ErrorResponse(500, "INTERNAL_ERROR", "Unexpected exception signing document", e.getMessage());
        };
    }
}
