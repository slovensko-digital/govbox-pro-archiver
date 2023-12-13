package digital.slovensko.archiver.server.dto;

public record HealthResponse (boolean trustedListsLoaded, int loadedTrustedLists) {
}
