package digital.slovensko.archiver.core.errors;

public class ResponseNetworkErrorException extends ArchiverException {
    public ResponseNetworkErrorException(String message, Exception e) {
        super("Nastala chyba", "Nepodarilo sa poslať odpoveď externej aplikácii", message, e);
    }
}
