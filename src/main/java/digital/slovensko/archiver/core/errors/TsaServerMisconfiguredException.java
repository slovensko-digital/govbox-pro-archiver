package digital.slovensko.archiver.core.errors;

public class TsaServerMisconfiguredException extends ArchiverException {
    public TsaServerMisconfiguredException(String description, Throwable e) {
        super("Chyba TSA servera", "Nepodarilo sa pridať časovú pečiatku", description, e);
    }
}
