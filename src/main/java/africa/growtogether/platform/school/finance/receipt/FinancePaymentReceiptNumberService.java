package africa.growtogether.platform.school.finance.receipt;

import africa.growtogether.platform.ecs.ConfigurationDtos.ResolveRequest;
import africa.growtogether.platform.ecs.ConfigurationException;
import africa.growtogether.platform.ecs.ConfigurationService;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancePaymentReceiptNumberService {

    static final String CONFIGURATION_CODE =
            "GT_SCHOOL_FINANCE_RECEIPT_NUMBER_FORMAT";

    /*
     * Generic fallback only. It contains no school, country,
     * provider or payment-method identity.
     */
    static final String DEFAULT_FORMAT =
            "RCT-{sequence:10}";

    private static final Pattern SEQUENCE_TOKEN =
            Pattern.compile("\\{sequence(?::(\\d{1,2}))?}");

    private static final String NEXT_SEQUENCE_SQL = """
            INSERT INTO gts_payment_receipt_number_sequence (
                tenant_id,
                last_issued_number,
                created_at,
                updated_at
            )
            VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (tenant_id)
            DO UPDATE
            SET last_issued_number =
                    gts_payment_receipt_number_sequence.last_issued_number + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING last_issued_number
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ConfigurationService configuration;

    public FinancePaymentReceiptNumberService(
            JdbcTemplate jdbcTemplate,
            ConfigurationService configuration
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.configuration = configuration;
    }

    @Transactional
    public String next(UUID tenantId) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        Long sequence =
                jdbcTemplate.queryForObject(
                        NEXT_SEQUENCE_SQL,
                        Long.class,
                        tenantId
                );

        if (sequence == null || sequence <= 0) {
            throw new IllegalStateException(
                    "Unable to generate receipt sequence"
            );
        }

        return render(
                resolveFormat(tenantId),
                sequence
        );
    }

    private String resolveFormat(UUID tenantId) {

        try {
            var resolved =
                    configuration.resolve(
                            new ResolveRequest(
                                    CONFIGURATION_CODE,
                                    null,
                                    null,
                                    tenantId
                            )
                    );

            String configured =
                    resolved.value();

            if (configured == null || configured.isBlank()) {
                return DEFAULT_FORMAT;
            }

            return configured.trim();

        } catch (ConfigurationException ignored) {
            return DEFAULT_FORMAT;
        }
    }

    static String render(
            String format,
            long sequence
    ) {

        if (sequence <= 0) {
            throw new IllegalArgumentException(
                    "sequence must be positive"
            );
        }

        String candidate =
                format == null
                        ? ""
                        : format.trim();

        if (candidate.isBlank()) {
            throw new IllegalArgumentException(
                    "Receipt number format must not be blank"
            );
        }

        Matcher matcher =
                SEQUENCE_TOKEN.matcher(candidate);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Receipt number format must contain "
                    + "{sequence} or {sequence:N}"
            );
        }

        String widthValue =
                matcher.group(1);

        int width =
                widthValue == null
                        ? 10
                        : Integer.parseInt(widthValue);

        if (width < 1 || width > 18) {
            throw new IllegalArgumentException(
                    "Receipt sequence width must be between 1 and 18"
            );
        }

        if (matcher.find()) {
            throw new IllegalArgumentException(
                    "Receipt number format must contain "
                    + "exactly one sequence token"
            );
        }

        String numeric =
                String.format(
                        Locale.ROOT,
                        "%0" + width + "d",
                        sequence
                );

        String rendered =
                SEQUENCE_TOKEN
                        .matcher(candidate)
                        .replaceFirst(
                                Matcher.quoteReplacement(numeric)
                        )
                        .trim();

        if (rendered.isBlank() || rendered.length() > 100) {
            throw new IllegalArgumentException(
                    "Rendered receipt number must be "
                    + "between 1 and 100 characters"
            );
        }

        return rendered;
    }
}
