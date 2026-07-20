package africa.growtogether.platform.eiam.user;

public record UserSearchCriteria(String query, UserAccountStatus status) {
    public String normalizedQuery() {
        return query == null || query.isBlank() ? null : query.trim().toLowerCase();
    }
}
