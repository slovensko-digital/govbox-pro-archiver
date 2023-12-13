package digital.slovensko.archiver.core.errors;

import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;

public class ArchiverException extends RuntimeException {
    private final String heading;
    private final String subheading;
    private final String description;

    public ArchiverException(String heading, String subheading, String description, Throwable e) {
        super(e);
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public ArchiverException(String heading, String subheading, String description) {
        this.heading = heading;
        this.subheading = subheading;
        this.description = description;
    }

    public String getHeading() {
        return heading;
    }

    public String getSubheading() {
        return subheading;
    }

    public String getDescription() {
        return description;
    }

    public static ArchiverException createFromDSSException(DSSException e) {
        for (Throwable cause = e; cause != null && cause.getCause() != cause; cause = cause.getCause()) {
            if (cause.getMessage() != null) {
                if (cause instanceof DSSExternalResourceException) {
                    return new TsaServerMisconfiguredException("Nastavený TSA server odmietol pridať časovú pečiatku. Skontrolujte nastavenia TSA servera.", cause);
                } else if (cause instanceof NullPointerException && cause.getMessage().contains("Host name")) {
                    return new TsaServerMisconfiguredException("Nie je nastavená žiadna adresa TSA servera. Skontrolujte nastavenia TSA servera.", cause);
                }
            }
        }

        return new UnrecognizedException(e);
    }
}
