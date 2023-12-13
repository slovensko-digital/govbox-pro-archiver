package digital.slovensko.archiver.core.errors;

public class TransformationException extends ArchiverException {
    public TransformationException(String message, String description) {
        super("Chyba  transformácie", message, description);
    }

    public TransformationException(String message, String description, Throwable e) {
        super("Chyba transformácie", message, description, e);
    }
}
