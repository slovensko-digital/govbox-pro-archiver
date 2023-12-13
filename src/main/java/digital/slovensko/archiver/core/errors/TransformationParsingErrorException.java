package digital.slovensko.archiver.core.errors;

public class TransformationParsingErrorException extends ArchiverException {
    public TransformationParsingErrorException(String message) {
        super("Nastala chyba", "Nastala chyba pri čítaní XSLT transformácie", message);
    }
}
