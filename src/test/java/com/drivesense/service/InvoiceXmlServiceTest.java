package com.drivesense.service;

import com.drivesense.entity.Booking;
import com.drivesense.entity.Car;
import com.drivesense.entity.User;
import com.drivesense.enums.BootSize;
import com.drivesense.enums.CarStatus;
import com.drivesense.enums.CarType;
import com.drivesense.enums.FuelType;
import com.drivesense.enums.Transmission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InvoiceXmlServiceTest {

    @Test
    @DisplayName("Invoice XML is generated and validated against invoice.xsd schema")
    public void testInvoiceXmlGenerationAndValidation() {
        InvoiceXmlService service = new InvoiceXmlService();

        User user = new User("Aaditya Sharma", "user@example.com", "+919876543210", "hash", "DL1", LocalDate.of(1995, 1, 1), "ROLE_CUSTOMER");

        Car car = new Car("Hyundai", "Creta", "RJ14-XX-0000",
                CarType.SUV, FuelType.PETROL, Transmission.AUTOMATIC, 5, BootSize.M,
                new BigDecimal("3500.00"), new BigDecimal("200.00"), 300, new BigDecimal("12.00"),
                142, "img.jpg", CarStatus.AVAILABLE, new BigDecimal("4.8"));

        Booking booking = new Booking();
        booking.setBookingCode("DS-2026-000123");
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartTime(LocalDateTime.of(2026, 10, 10, 9, 0));
        booking.setEndTime(LocalDateTime.of(2026, 10, 12, 9, 0));
        booking.setBaseAmount(new BigDecimal("7000.00"));
        booking.setSurchargeAmount(new BigDecimal("700.00"));
        booking.setDiscountAmount(new BigDecimal("385.00"));
        booking.setTaxAmount(new BigDecimal("1323.00"));
        booking.setTotalAmount(new BigDecimal("8638.00"));
        booking.setEstCo2Kg(new BigDecimal("14.2"));

        String xml = service.generateAndValidateXmlInvoice(booking);

        assertNotNull(xml);
        assertTrue(xml.contains("<invoice code=\"DS-2026-000123\">"), "XML should contain invoice root with code");
        assertTrue(xml.contains("<customer name=\"Aaditya Sharma\" email=\"user@example.com\"/>"), "XML should contain customer attributes");
        assertTrue(xml.contains("<car brand=\"Hyundai\" model=\"Creta\" reg=\"RJ14-XX-0000\"/>"), "XML should contain car attributes");
        assertTrue(xml.contains("<base>7000.00</base>"), "XML should contain base charges");
        assertTrue(xml.contains("<total>8638.00</total>"), "XML should contain total charges");
    }
}
