package digital.slovensko.archiver.core;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.x509.CertificateToken;


public record TimestampedDocument (DSSDocument document, CertificateToken certificateToken) {
}
