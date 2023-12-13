package digital.slovensko.archiver.core.errors;

public class EmptyBodyException extends ArchiverException {
    public EmptyBodyException(String message) {
        super("Empty body", "JsonSyntaxException parsing request body.", message);
    }
}
