package com.biltuthyrning.web;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component("carUtils")
public class CarUtils {

    /**
     * Märkesemblem — samma 37 SVG:er som elbilsappen använder, kopierade till
     * {@code static/emblem/} i stället för hotlänkade: flottan ska rita ut sig även när
     * den andra tjänsten sover.
     *
     * <p>Nyckeln är modellnamnets FÖRSTA ORD, normaliserat till gemener utan diakriter, så
     * att "Škoda Enyaq" och "Citroën ë-C4" hittar rätt. Bilarna kommer från två håll — den
     * kurerade Volvo-flottan i DataInitializer och den delade flottan från CarAdvice
     * (FleetSyncService) — och båda skriver märket först.
     *
     * <p>Ett märke utan emblem faller tillbaka på bil-emojin. De som saknas (Peugeot,
     * Hyundai, XPENG, Zeekr …) saknas därför att ingen fri och läsbar symbol finns — ett
     * fel emblem är sämre än inget, och det är mätt i elbilsappen och inte glömt här.
     */
    private static final Map<String, String> EMBLEM = new HashMap<>();
    static {
        for (String s : new String[]{
                "alpine", "audi", "bmw", "byd", "citroen", "cupra", "dacia", "fiat", "ford",
                "geely", "gwm", "honda", "jac", "jeep", "kgm", "kia", "lexus", "mazda",
                "mercedes", "mg", "mini", "mitsubishi", "nio", "nissan", "opel", "polestar",
                "renault", "skoda", "smart", "subaru", "suzuki", "tesla", "toyota",
                "vinfast", "volkswagen", "volvo"}) {
            EMBLEM.put(s, s);
        }
        EMBLEM.put("mercedes-benz", "mercedes");
        EMBLEM.put("rolls-royce", "rollsroyce");
        EMBLEM.put("rolls", "rollsroyce");   // "Rolls Royce Spectre" skrivs ibland utan bindestreck
        EMBLEM.put("vw", "volkswagen");
    }

    /**
     * Emblemets filnamn (utan ändelse) för en bil, eller tom sträng när märket saknar emblem.
     * Mallen frågar på det här och väljer bild eller emoji.
     */
    public String getEmblem(String model) {
        if (model == null) return "";
        String[] delar = model.trim().split("\\s+");
        if (delar.length == 0 || delar[0].isEmpty()) return "";
        String marke = normalisera(delar[0]);
        String slug = EMBLEM.get(marke);
        if (slug != null) return slug;
        // "MG4 Standard" och "MG5 Long Range" skriver märke och modell i SAMMA ord — tre bilar
        // i flottan stod utan emblem trots att mg.svg fanns.
        if (marke.matches("mg\\d+")) return "mg";
        // "Rolls Royce" och "Alfa Romeo" delas av mellanslag — prova märket som två ord
        // innan vi ger upp, annars hade bara den bindestrecksskrivna varianten träffat.
        if (delar.length > 1) {
            slug = EMBLEM.get(marke + "-" + normalisera(delar[1]));
            if (slug != null) return slug;
        }
        return "";
    }

    private static String normalisera(String ord) {
        return Normalizer.normalize(ord.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                         .replaceAll("\\p{M}", "");
    }

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
