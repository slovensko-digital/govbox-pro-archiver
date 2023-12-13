package digital.slovensko.archiver.core.errors;

public class OriginalDocumentNotFoundException extends ArchiverException {

    public OriginalDocumentNotFoundException(String description) {
        super("Chyba ASiC-E kontajnera", "Súbor na podpis nebol nájdený", description);
    }
}
