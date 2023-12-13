package digital.slovensko.archiver.core.errors;

public class DocumentNotSignedYetException extends ArchiverException {
    public DocumentNotSignedYetException() {
        super("Document not signed", "Document is not signed yet", "The provided document is not eligible for signature validation because the document is not signed yet.");
    }
}
