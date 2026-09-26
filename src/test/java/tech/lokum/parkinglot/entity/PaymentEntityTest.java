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
        payment.setCurrency("INR");
        payment.setMethod(Payment.PaymentMethod.CARD);
        payment.setStatus(Payment.Status.PENDING);

        assertEquals(1L, payment.getId());
        assertNotNull(payment.getReservation());
        assertEquals(new BigDecimal("50.00"), payment.getAmount());
        assertEquals("INR", payment.getCurrency());
        assertEquals(Payment.PaymentMethod.CARD, payment.getMethod());
        assertEquals(Payment.Status.PENDING, payment.getStatus());
    }

    @Test
    void entityHasCorrectTableName() {
        assertEquals(Payment.class.getAnnotation(jakarta.persistence.Table.class).name(), "payments");
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
}
