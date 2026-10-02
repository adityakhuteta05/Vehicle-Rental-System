package com.drivesense.dto.xml;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlRootElement(name = "invoice")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceXml {

    @XmlAttribute(name = "code")
    private String code;

    @XmlElement(name = "customer")
    private CustomerXml customer;

    @XmlElement(name = "car")
    private CarXml car;

    @XmlElement(name = "period")
    private PeriodXml period;

    @XmlElement(name = "charges")
    private ChargesXml charges;

    @XmlElement(name = "carbon")
    private CarbonXml carbon;

    public InvoiceXml() {}

    public InvoiceXml(String code, CustomerXml customer, CarXml car, PeriodXml period, ChargesXml charges, CarbonXml carbon) {
        this.code = code;
        this.customer = customer;
        this.car = car;
        this.period = period;
        this.charges = charges;
        this.carbon = carbon;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CustomerXml getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerXml customer) {
        this.customer = customer;
    }

    public CarXml getCar() {
        return car;
    }

    public void setCar(CarXml car) {
        this.car = car;
    }

    public PeriodXml getPeriod() {
        return period;
    }

    public void setPeriod(PeriodXml period) {
        this.period = period;
    }

    public ChargesXml getCharges() {
        return charges;
    }

    public void setCharges(ChargesXml charges) {
        this.charges = charges;
    }

    public CarbonXml getCarbon() {
        return carbon;
    }

    public void setCarbon(CarbonXml carbon) {
        this.carbon = carbon;
    }

    // Inner JAXB structures matching PRD schema exactly

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CustomerXml {
        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "email")
        private String email;

        public CustomerXml() {}
        public CustomerXml(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CarXml {
        @XmlAttribute(name = "brand")
        private String brand;

        @XmlAttribute(name = "model")
        private String model;

        @XmlAttribute(name = "reg")
        private String reg;

        public CarXml() {}
        public CarXml(String brand, String model, String reg) {
            this.brand = brand;
            this.model = model;
            this.reg = reg;
        }

        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getReg() { return reg; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PeriodXml {
        @XmlAttribute(name = "from")
        private String from;

        @XmlAttribute(name = "to")
        private String to;

        public PeriodXml() {}
        public PeriodXml(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ChargesXml {
        @XmlElement(name = "base")
        private BigDecimal base;

        @XmlElement(name = "surcharge")
        private BigDecimal surcharge;

        @XmlElement(name = "discount")
        private BigDecimal discount;

        @XmlElement(name = "gst")
        private BigDecimal gst;

        @XmlElement(name = "total")
        private BigDecimal total;

        public ChargesXml() {}
        public ChargesXml(BigDecimal base, BigDecimal surcharge, BigDecimal discount, BigDecimal gst, BigDecimal total) {
            this.base = base;
            this.surcharge = surcharge;
            this.discount = discount;
            this.gst = gst;
            this.total = total;
        }

        public BigDecimal getBase() { return base; }
        public BigDecimal getSurcharge() { return surcharge; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal getGst() { return gst; }
        public BigDecimal getTotal() { return total; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CarbonXml {
        @XmlAttribute(name = "estimatedKg")
        private BigDecimal estimatedKg;

        public CarbonXml() {}
        public CarbonXml(BigDecimal estimatedKg) {
            this.estimatedKg = estimatedKg;
        }

        public BigDecimal getEstimatedKg() { return estimatedKg; }
    }
}
