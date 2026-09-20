package africa.growtogether.platform.school.finance.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FinancePaymentReceiptNumberServiceContractTest {

    @Test
    void genericDefaultFormatProducesTenDigitSequence() {

        assertEquals(
                "RCT-0000000042",
                FinancePaymentReceiptNumberService.render(
                        "RCT-{sequence}",
                        42
                )
        );
    }

    @Test
    void tenantConfiguredFormatCanChangePresentation() {

        assertEquals(
                "ORG-RCT-000042",
                FinancePaymentReceiptNumberService.render(
                        "ORG-RCT-{sequence:06}",
                        42
                )
        );
    }

    @Test
    void sequenceMustBePositive() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinancePaymentReceiptNumberService.render(
                                "RCT-{sequence}",
                                0
                        )
        );
    }

    @Test
    void formatMustContainSequenceToken() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinancePaymentReceiptNumberService.render(
                                "RCT-NO-SEQUENCE",
                                1
                        )
        );
    }

    @Test
    void formatCannotContainMultipleSequenceTokens() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinancePaymentReceiptNumberService.render(
                                "{sequence}-{sequence}",
                                1
                        )
        );
    }

    @Test
    void widthMustRemainControlled() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinancePaymentReceiptNumberService.render(
                                "RCT-{sequence:19}",
                                1
                        )
        );
    }

    @Test
    void renderedReceiptNumberMustFitSchemaColumn() {

        String excessivePrefix =
                "X".repeat(100);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        FinancePaymentReceiptNumberService.render(
                                excessivePrefix
                                + "{sequence}",
                                1
                        )
        );
    }
}
