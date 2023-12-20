package digital.slovensko.archiver.core;

import static digital.slovensko.archiver.core.DSSUtils.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;

public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";
    private CertificateVerifier verifier;
    private TLValidationJob validationJob;
    private static Logger logger = LoggerFactory.getLogger(SignatureValidator.class);

    // Singleton
    private static SignatureValidator instance;

    private SignatureValidator() {
    }

    public synchronized static SignatureValidator getInstance() {
        if (instance == null)
            instance = new SignatureValidator();

        return instance;
    }

    private synchronized Reports validate(SignedDocumentValidator docValidator) {
        docValidator.setCertificateVerifier(verifier);

        // TODO: do not print stack trace inside DSS
        return docValidator.validateDocument();
    }

    public synchronized CertificateVerifier getVerifier() {
        return verifier;
    }

    public synchronized Reports validate(DSSDocument document) {
        var documentValidator = createDocumentValidator(document);
        if (documentValidator == null)
            return null;

        try {
            return validate(documentValidator);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public synchronized void refresh() {
        validationJob.offlineRefresh();
    }

    public synchronized void initialize(ExecutorService executorService) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        logger.debug("Initializing signature validator at {}", formatter.format(new Date()));

        validationJob = new TLValidationJob();

        var lotlSource = new LOTLSource();
        lotlSource.setCertificateSource(getJournalCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
        lotlSource.setUrl(LOTL_URL);
        lotlSource.setPivotSupport(true);

        var offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(21600000);  // 6 hours
        offlineFileLoader.setDataLoader(new CommonsDataLoader());
        validationJob.setOfflineDataLoader(offlineFileLoader);

        var onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(new CommonsDataLoader());
        validationJob.setOnlineDataLoader(onlineFileLoader);

        var trustedListCertificateSource = new TrustedListsCertificateSource();
        validationJob.setTrustedListCertificateSource(trustedListCertificateSource);
        validationJob.setListOfTrustedListSources(lotlSource);
        validationJob.setSynchronizationStrategy(new ExpirationAndSignatureCheckStrategy());
        validationJob.setExecutorService(executorService);
        validationJob.setDebug(false);

        logger.debug("Starting signature validator offline refresh");
        validationJob.offlineRefresh();

        verifier = new CommonCertificateVerifier();
        verifier.setTrustedCertSources(trustedListCertificateSource);
        verifier.setCrlSource(new OnlineCRLSource());
        verifier.setOcspSource(new OnlineOCSPSource());

        logger.debug("Signature validator initialized at {}", formatter.format(new Date()));
    }

    private CertificateSource getJournalCertificateSource() throws AssertionError {
        try {
            var keystore = getClass().getResourceAsStream("lotlKeyStore.p12");
            return new KeyStoreCertificateSource(keystore, "PKCS12", "dss-password");

        } catch (DSSException | NullPointerException e) {
            throw new AssertionError("Cannot load LOTL keystore", e);
        }
    }

    public boolean areTLsLoaded() {
        return validationJob != null;
    }

    public int loadedTLs() {
        if (validationJob == null)
            return 0;

        return validationJob.getSummary().getNumberOfProcessedTLs();
    }
}
