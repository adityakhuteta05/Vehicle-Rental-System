package com.drivesense.pricing.xml;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "pricingRules")
@XmlAccessorType(XmlAccessType.FIELD)
public class PricingRulesConfigXml {

    @XmlElement(name = "rule")
    private List<RuleXml> rules = new ArrayList<>();

    @XmlElement(name = "tax")
    private TaxXml tax;

    public List<RuleXml> getRules() {
        return rules;
    }

    public void setRules(List<RuleXml> rules) {
        this.rules = rules;
    }

    public TaxXml getTax() {
        return tax;
    }

    public void setTax(TaxXml tax) {
        this.tax = tax;
    }
}
