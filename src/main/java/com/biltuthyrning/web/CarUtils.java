package com.biltuthyrning.web;

import org.springframework.stereotype.Component;

@Component("carUtils")
public class CarUtils {

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
        return "#2E86C1";
    }

    public String getEVBadge(String model) {
        String m = model.toUpperCase();
        if (m.contains("EX"))   return "EV ⚡";
        if (m.contains("T8") || m.contains("RECHARGE")) return "PHEV 🔌";
        return "";
    }

    public String getFuelType(String engine) {
        String e = engine.toUpperCase();
        if (e.startsWith("EL")) return "El";
        if (e.contains("LADDHYBRID") || e.contains("T8")) return "Laddhybrid";
        return "Bensin";
    }
}
