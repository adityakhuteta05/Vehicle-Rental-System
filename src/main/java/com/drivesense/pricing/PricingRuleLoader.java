package com.drivesense.pricing;

import com.drivesense.pricing.xml.DateRangeXml;
import com.drivesense.pricing.xml.PricingRulesConfigXml;
import com.drivesense.pricing.xml.RuleXml;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PricingRuleLoader {

    private static final Logger log = LoggerFactory.getLogger(PricingRuleLoader.class);

    private final List<PricingRule> activeRules = new ArrayList<>();
    private BigDecimal gstPercent = new BigDecimal("18.00");

    @PostConstruct
    public void init() {
        loadRulesFromXml();
    }

    public synchronized void loadRulesFromXml() {
        activeRules.clear();
        try {
            ClassPathResource resource = new ClassPathResource("xml/pricing-rules.xml");
            if (!resource.exists()) {
                log.warn("pricing-rules.xml not found on classpath, using defaults");
                fallbackDefaultRules();
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(PricingRulesConfigXml.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                PricingRulesConfigXml config = (PricingRulesConfigXml) unmarshaller.unmarshal(is);

                if (config.getTax() != null && config.getTax().getGstPercent() != null) {
                    this.gstPercent = config.getTax().getGstPercent();
                }

                // Factory Pattern: Instantiate concrete strategy objects from XML rules
                for (RuleXml ruleXml : config.getRules()) {
                    PricingRule rule = createRuleFromXml(ruleXml);
                    if (rule != null) {
                        activeRules.add(rule);
                    }
                }
                log.info("Successfully loaded {} dynamic pricing rules from XML. GST: {}%", activeRules.size(), gstPercent);
            }
        } catch (Exception e) {
            log.error("Failed to parse pricing-rules.xml with JAXB, falling back to built-in rules", e);
            fallbackDefaultRules();
        }

        // Always attach dynamic context-based rules
        activeRules.add(new LoyaltyRule());
        activeRules.add(new DriverAndExtrasRule());
        activeRules.add(new DepositRule());
    }

    private PricingRule createRuleFromXml(RuleXml xml) {
        String name = xml.getName() != null ? xml.getName().toUpperCase() : "";
        if (name.startsWith("WEEKEND")) {
            return new WeekendRule(xml.getPercent());
        } else if (name.startsWith("FESTIVAL")) {
            List<FestivalRule.DateRange> ranges = new ArrayList<>();
            if (xml.getRanges() != null) {
                for (DateRangeXml r : xml.getRanges()) {
                    try {
                        ranges.add(new FestivalRule.DateRange(LocalDate.parse(r.getFrom()), LocalDate.parse(r.getTo())));
                    } catch (Exception e) {
                        log.warn("Invalid festival range from={} to={}", r.getFrom(), r.getTo());
                    }
                }
            }
            return new FestivalRule(xml.getPercent(), ranges);
        } else if (name.startsWith("LONG_RENTAL")) {
            int minDays = xml.getMinDays() != null ? xml.getMinDays() : 3;
            return new LongRentalRule(minDays, xml.getPercent());
        } else if (name.startsWith("EARLY_BIRD")) {
            int minAdvance = xml.getMinAdvanceDays() != null ? xml.getMinAdvanceDays() : 14;
            return new EarlyBirdRule(minAdvance, xml.getPercent());
        }
        return null;
    }

    private void fallbackDefaultRules() {
        activeRules.add(new WeekendRule(10.0));
        activeRules.add(new LongRentalRule(3, 5.0));
        activeRules.add(new LongRentalRule(7, 12.0));
        activeRules.add(new EarlyBirdRule(14, 7.0));
        activeRules.add(new LoyaltyRule());
        activeRules.add(new DriverAndExtrasRule());
        activeRules.add(new DepositRule());
        this.gstPercent = new BigDecimal("18.00");
    }

    public List<PricingRule> getActiveRules() {
        return activeRules;
    }

    public BigDecimal getGstPercent() {
        return gstPercent;
    }
}
