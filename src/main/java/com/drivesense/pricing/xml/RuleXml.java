package com.drivesense.pricing.xml;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class RuleXml {

    @XmlAttribute(name = "name")
    private String name;

    @XmlAttribute(name = "type")
    private String type; // SURCHARGE or DISCOUNT

    @XmlAttribute(name = "percent")
    private double percent;

    @XmlAttribute(name = "minDays")
    private Integer minDays;

    @XmlAttribute(name = "minAdvanceDays")
    private Integer minAdvanceDays;

    @XmlElement(name = "range")
    private List<DateRangeXml> ranges = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public Integer getMinDays() {
        return minDays;
    }

    public void setMinDays(Integer minDays) {
        this.minDays = minDays;
    }

    public Integer getMinAdvanceDays() {
        return minAdvanceDays;
    }

    public void setMinAdvanceDays(Integer minAdvanceDays) {
        this.minAdvanceDays = minAdvanceDays;
    }

    public List<DateRangeXml> getRanges() {
        return ranges;
    }

    public void setRanges(List<DateRangeXml> ranges) {
        this.ranges = ranges;
    }
}
