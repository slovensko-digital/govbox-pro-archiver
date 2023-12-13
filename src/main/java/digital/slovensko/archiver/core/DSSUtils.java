package digital.slovensko.archiver.core;

import digital.slovensko.archiver.core.errors.DocumentNotSignedYetException;
import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.asic.cades.ASiCWithCAdESSignatureParameters;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.cades.validation.ASiCContainerWithCAdESValidatorFactory;
import eu.europa.esig.dss.asic.xades.ASiCWithXAdESSignatureParameters;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.asic.xades.validation.ASiCContainerWithXAdESValidatorFactory;
import eu.europa.esig.dss.cades.CAdESSignatureParameters;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.cades.validation.CMSDocumentValidatorFactory;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.pades.validation.PDFDocumentValidatorFactory;
import eu.europa.esig.dss.signature.AbstractSignatureService;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import eu.europa.esig.dss.xades.validation.XMLDocumentValidatorFactory;

public class DSSUtils {
    public static SignedDocumentValidator createDocumentValidator(DSSDocument document) {
        if (new PDFDocumentValidatorFactory().isSupported(document))
            return new PDFDocumentValidatorFactory().create(document);

        if (new XMLDocumentValidatorFactory().isSupported(document))
            return new XMLDocumentValidatorFactory().create(document);

        if (new ASiCContainerWithXAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithXAdESValidatorFactory().create(document);

        if (new ASiCContainerWithCAdESValidatorFactory().isSupported(document))
            return new ASiCContainerWithCAdESValidatorFactory().create(document);

        if (new CMSDocumentValidatorFactory().isSupported(document))
            return new CMSDocumentValidatorFactory().create(document);

        throw new DocumentNotSignedYetException();
    }

    public static SignatureLevel getLtaSignatureLevelForDocument(InMemoryDocument document) {
        var validator = createDocumentValidator(document);
        validator.setCertificateVerifier(new CommonCertificateVerifier());

        var r = validator.validateDocument();
        var level = r.getSimpleReport().getSignatureFormat(r.getSimpleReport().getFirstSignatureId());

        return switch(level.getSignatureForm()) {
            case XAdES -> SignatureLevel.XAdES_BASELINE_LTA;
            case CAdES -> SignatureLevel.CAdES_BASELINE_LTA;
            case PAdES -> SignatureLevel.PAdES_BASELINE_LTA;
            default -> throw new RuntimeException(
                    "Unsupported signature type: " + level.getSignatureForm().name());
        };
    }

    public static AbstractSignatureService getServiceForDocument(InMemoryDocument document, CertificateVerifier verifier) {
        var validator = createDocumentValidator(document);
        validator.setCertificateVerifier(new CommonCertificateVerifier());

        var r = validator.validateDocument();
        var level = r.getSimpleReport().getSignatureFormat(r.getSimpleReport().getFirstSignatureId());
        var isContainer = r.getSimpleReport().getContainerType() != null;

        return switch (level.getSignatureForm()) {
            case XAdES -> isContainer ? new ASiCWithXAdESService(verifier) : new XAdESService(verifier);
            case CAdES -> isContainer ? new ASiCWithCAdESService(verifier) : new CAdESService(verifier);
            case PAdES -> new PAdESService(verifier);
            default -> throw new RuntimeException(
                    "Unsupported signature type: " + level.getSignatureForm().name());

        };
    }

    public static AbstractSignatureParameters getTimestampParametersForDocument(InMemoryDocument document, AbstractSignatureService service) {
        var validator = createDocumentValidator(document);
        validator.setCertificateVerifier(new CommonCertificateVerifier());

        var r = validator.validateDocument();
        var level = r.getSimpleReport().getSignatureFormat(r.getSimpleReport().getFirstSignatureId());
        var isContainer = r.getSimpleReport().getContainerType() != null;

        return switch (level.getSignatureForm()) {
            case XAdES -> isContainer ? new ASiCWithXAdESSignatureParameters() : new XAdESSignatureParameters();
            case CAdES -> isContainer ? new ASiCWithCAdESSignatureParameters() : new CAdESSignatureParameters();
            case PAdES -> new PAdESSignatureParameters();
            default -> throw new RuntimeException(
                    "Unsupported signature type: " + level.getSignatureForm().name());

        };
    }

}
