package digital.slovensko.archiver.core;

import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import static digital.slovensko.archiver.core.DSSUtils.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;


public class Archiver {
    private final TSPSource tspSource;

    public Archiver(TSPSource tspSource) {
        this.tspSource = tspSource;
    }

    public void initializeSignatureValidator(ScheduledExecutorService scheduledExecutorService, ExecutorService cachedExecutorService) {
        SignatureValidator.getInstance().initialize(cachedExecutorService);

        scheduledExecutorService.scheduleAtFixedRate(() -> SignatureValidator.getInstance().refresh(),
                480, 480, java.util.concurrent.TimeUnit.MINUTES);
    }

    public TimestampedDocument timestampDocument(InMemoryDocument document) {
        var validator = SignatureValidator.getInstance();
        var service = getServiceForDocument(document, validator.getVerifier());
        service.setTspSource(tspSource);

        var params = getTimestampParametersForDocument(document, service);
        params.setSignatureLevel(getLtaSignatureLevelForDocument(document));

        return new TimestampedDocument(service.extendDocument(document, params), params.getSigningCertificate());
    }
}
