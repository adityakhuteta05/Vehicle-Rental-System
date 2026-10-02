package com.drivesense.service;

import com.drivesense.dto.xml.InvoiceXml;
import com.drivesense.entity.Booking;
import com.drivesense.util.DateUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringWriter;

@Service
public class InvoiceXmlService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceXmlService.class);

    public String generateAndValidateXmlInvoice(Booking booking) {
        try {
            InvoiceXml xmlModel = buildInvoiceXmlModel(booking);

            JAXBContext jaxbContext = JAXBContext.newInstance(InvoiceXml.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Validate against invoice.xsd
            try {
                ClassPathResource xsdResource = new ClassPathResource("xml/invoice.xsd");
                if (xsdResource.exists()) {
                    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = sf.newSchema(xsdResource.getURL());
                    marshaller.setSchema(schema);
                }
            } catch (Exception e) {
                log.warn("Could not load invoice.xsd for schema validation: {}", e.getMessage());
            }

            StringWriter writer = new StringWriter();
            marshaller.marshal(xmlModel, writer);
            return writer.toString();

        } catch (Exception e) {
            log.error("Failed to generate XML invoice for booking {}", booking.getBookingCode(), e);
            throw new RuntimeException("XML invoice generation failed: " + e.getMessage(), e);
        }
    }

    public InvoiceXml buildInvoiceXmlModel(Booking booking) {
        InvoiceXml.CustomerXml customer = new InvoiceXml.CustomerXml(
                booking.getUser().getFullName(),
                booking.getUser().getEmail()
        );

        InvoiceXml.CarXml car = new InvoiceXml.CarXml(
                booking.getCar().getBrand(),
                booking.getCar().getModel(),
                booking.getCar().getRegNumber()
        );

        InvoiceXml.PeriodXml period = new InvoiceXml.PeriodXml(
                DateUtil.ISO_SHORT_FORMATTER.format(booking.getStartTime()),
                DateUtil.ISO_SHORT_FORMATTER.format(booking.getEndTime())
        );

        InvoiceXml.ChargesXml charges = new InvoiceXml.ChargesXml(
                booking.getBaseAmount(),
                booking.getSurchargeAmount(),
                booking.getDiscountAmount(),
                booking.getTaxAmount(),
                booking.getTotalAmount()
        );

        InvoiceXml.CarbonXml carbon = new InvoiceXml.CarbonXml(booking.getEstCo2Kg());

        return new InvoiceXml(booking.getBookingCode(), customer, car, period, charges, carbon);
    }
}
