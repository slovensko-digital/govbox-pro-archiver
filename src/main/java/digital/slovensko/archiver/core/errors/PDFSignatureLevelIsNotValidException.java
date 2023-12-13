package digital.slovensko.archiver.core.errors;

public class PDFSignatureLevelIsNotValidException extends ArchiverException {
    public PDFSignatureLevelIsNotValidException() {
        super("Nastala chyba", "Typ podpisu nie je podporovaný", "Zadali ste typ podpisu, ktorý nie je podporovaný");
    }

    public PDFSignatureLevelIsNotValidException(String signatureLevelString) {
        super("Nastala chyba", "Typ podpisu nie je podporovaný", "Zadali ste typ podpisu \"" + signatureLevelString + "\", ktorý nie je podporovaný");
    }
}
