package digital.slovensko.archiver.server.dto;

import java.util.Base64;

import eu.europa.esig.dss.model.InMemoryDocument;


public record Document (String filename, String content) {
    public InMemoryDocument getDecodedContent() {
        return new InMemoryDocument(Base64.getDecoder().decode(content));
    }
}
