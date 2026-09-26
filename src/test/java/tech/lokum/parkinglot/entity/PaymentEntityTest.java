package tech.lokum.parkinglot.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentEntityTest {

    @Test
    void entityGettersAndSettersWork() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setReservation(new Reservation());
        payment.setAmount(new BigDecimal("50.00"));
        payment.setCurrency(Payment.Currency.USD);
        payment.setMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.Status.PENDING);

        assertEquals(1L, payment.getId());
        assertNotNull(payment.getReservation());
        assertEquals(new BigDecimal("50.00"), payment.getAmount());
        assertEquals(Payment.Currency.USD, payment.getCurrency());
        assertEquals(Payment.PaymentMethod.CARD, payment.getMethod());
        assertEquals(Payment.Status.PENDING, payment.getStatus());
    }

    @Test
    void entityHasCorrectTableName() {
        var table = Payment.class.getAnnotation(jakarta.persistence.Table.class);
        assertNotNull(table);
        assertEquals("payments", table.name());
    }

    @Test
    void paymentMethodEnumContainsExpectedValues() {
        assertEquals(4, Payment.PaymentMethod.values().length);
        assertNotNull(Payment.PaymentMethod.CASH);
        assertNotNull(Payment.PaymentMethod.CARD);
        assertNotNull(Payment.PaymentMethod.UPI);
        assertNotNull(Payment.PaymentMethod.ONLINE);
    }

    @Test
    void statusEnumContainsExpectedValues() {
        assertEquals(4, Payment.Status.values().length);
        assertNotNull(Payment.Status.PENDING);
        assertNotNull(Payment.Status.PAID);
        assertNotNull(Payment.Status.REFUNDED);
        assertNotNull(Payment.Status.FAILED);
    }

    @Test
    void currencyEnumContainsExpectedValues() {
        assertEquals(2, Payment.Currency.values().length);
        assertNotNull(Payment.Currency.INR);
        assertNotNull(Payment.Currency.USD);
    }
}
