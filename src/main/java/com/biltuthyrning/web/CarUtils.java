package com.biltuthyrning.web;

import org.springframework.stereotype.Component;

@Component("carUtils")
public class CarUtils {

    // Palett för bilar utanför den kurerade Volvo-flottan — deterministisk per modellnamn
    private static final String[] PALETTE = {
        "#2E86C1", "#8E44AD", "#B9770E", "#117A65", "#A93226", "#5D6D7E", "#1F618D", "#7D6608"
    };

    public String getBadgeColor(String model) {
        String m = model.toUpperCase();
        if (m.contains("EX60"))  return "#1E8449";
        if (m.contains("EX30"))  return "#148F77";
        if (m.contains("XC40"))  return "#2471A3";
        if (m.contains("XC60"))  return "#1A6090";
        if (m.contains("XC90"))  return "#154360";
        if (m.contains("S90"))   return "#6C3483";
        if (m.contains("V90"))   return "#0E6655";
        if (m.contains("V60"))   return "#922B21";
        return PALETTE[Math.abs(model.hashCode() % PALETTE.length)];
    }

    /** Badge avgörs av motorfältet, inte modellnamnet — "Lexus" innehåller "EX" men är ingen elbil. */
    public String getEVBadge(String engine) {
        String e = engine.toUpperCase();
        if (e.startsWith("EL")) return "EV ⚡";
        if (e.contains("LADDHYBRID") || e.contains("T8") || e.contains("RECHARGE")) return "PHEV 🔌";
        return "";
    }

    public String getFuelType(String engine) {
        String e = engine.toUpperCase();
        if (e.startsWith("EL")) return "El";
        if (e.contains("LADDHYBRID") || e.contains("T8")) return "Laddhybrid";
        if (e.contains("DIESEL")) return "Diesel";
        if (e.contains("HYBRID")) return "Hybrid";
        return "Bensin";
    }
}
