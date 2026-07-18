package com.biltuthyrning.service;

import com.biltuthyrning.model.Car;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Hämtar en delad bilflotta från CarAdvice-backendens publika API:er
 * (/api/ev-consumption och /api/ice-consumption) — samma databas som
 * Bilresa-kalkylatorn konsumerar. Bilarna får verifierad förbrukning i
 * motorfältet och ett deterministiskt demo-dagspris baserat på drivlina
 * och märke. Fail-silent: vid nätverks-/API-fel returneras tom lista och
 * den kurerade basflottan behålls som den är.
 */
@Service
public class FleetSyncService {

    private static final Logger log = LoggerFactory.getLogger(FleetSyncService.class);

    private static final int MAX_PER_BRAND = 2;

    private static final Set<String> PREMIUM_BRANDS = Set.of(
            "audi", "bmw", "mercedes", "mercedes-benz", "porsche", "tesla", "polestar",
            "volvo", "lexus", "jaguar", "land", "genesis", "maserati", "lotus");

    // Ultralyx: utan egen prisnivå hyrdes Rolls-Royce Spectre ut för 1 099 kr/dag
    private static final Set<String> ULTRA_LUXURY_BRANDS = Set.of(
            "rolls-royce", "bentley", "ferrari", "lamborghini", "aston", "mclaren",
            "maybach", "bugatti", "koenigsegg");

    @Value("${caradvice.api.url:https://caradvice.onrender.com}")
    private String apiUrl;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    /** Upp till {@code limit} bilar ur den delade databasen, blandat el och fossilt. */
    public List<Car> fetchSharedFleet(int limit) {
        if (limit <= 0) return List.of();
        try {
            List<Car> evs = parseEv(get("/api/ev-consumption"));
            List<Car> ices = parseIce(get("/api/ice-consumption"));
            List<Car> fleet = selectSpread(evs, ices, limit);
            log.info("Delad flotta: {} bilar hämtade från CarAdvice ({} el, {} fossil/hybrid tillgängliga)",
                    fleet.size(), evs.size(), ices.size());
            return fleet;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } catch (Exception e) {
            log.warn("Delad flotta kunde inte hämtas från CarAdvice: {}", e.getMessage());
            return List.of();
        }
    }

    private String get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(apiUrl + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + res.statusCode() + " från " + path);
        }
        return res.body();
    }

    List<Car> parseEv(String json) throws Exception {
        List<Car> cars = new ArrayList<>();
        for (JsonNode n : mapper.readTree(json)) {
            String name = n.path("carName").asText("").trim();
            double kwh = n.path("kwhPerMil").asDouble(0);
            if (name.isBlank() || kwh <= 0) continue;
            cars.add(Car.builder()
                    .model(name)
                    .year("–")
                    .engine("El · " + svDecimal(kwh) + " kWh/mil")
                    .dailyRate(priceFor(name, "elbil"))
                    .build());
        }
        return cars;
    }

    List<Car> parseIce(String json) throws Exception {
        List<Car> cars = new ArrayList<>();
        for (JsonNode n : mapper.readTree(json)) {
            String name = n.path("carName").asText("").trim();
            String fuel = n.path("fuel").asText("").trim().toLowerCase(Locale.ROOT);
            double liter = n.path("literPerMil").asDouble(0);
            if (name.isBlank() || fuel.isBlank() || liter <= 0) continue;
            String label = Character.toUpperCase(fuel.charAt(0)) + fuel.substring(1);
            cars.add(Car.builder()
                    .model(name)
                    .year("–")
                    .engine(label + " · " + svDecimal(liter) + " l/mil")
                    .dailyRate(priceFor(name, fuel))
                    .build());
        }
        return cars;
    }

    /**
     * Blandar el och fossilt varannan bil, max {@value MAX_PER_BRAND} per märke
     * så flottan inte domineras av de märken som har flest varianter i databasen.
     */
    static List<Car> selectSpread(List<Car> evs, List<Car> ices, int limit) {
        List<Car> result = new ArrayList<>();
        Map<String, Integer> perBrand = new HashMap<>();
        int i = 0, j = 0;
        boolean evTurn = true;
        while (result.size() < limit && (i < evs.size() || j < ices.size())) {
            Car candidate = null;
            if (evTurn && i < evs.size()) candidate = evs.get(i++);
            else if (!evTurn && j < ices.size()) candidate = ices.get(j++);
            else if (i < evs.size()) candidate = evs.get(i++);
            else candidate = ices.get(j++);
            evTurn = !evTurn;

            String brand = brandOf(candidate.getModel());
            int seen = perBrand.getOrDefault(brand, 0);
            if (seen >= MAX_PER_BRAND) continue;
            perBrand.put(brand, seen + 1);
            result.add(candidate);
        }
        return result;
    }

    /** Demo-dagspris: bas per drivlina + premium-/ultralyxpåslag + deterministisk variation per namn. */
    public static BigDecimal priceFor(String name, String fuel) {
        int base = switch (fuel) {
            case "elbil" -> 999;
            case "laddhybrid" -> 1049;
            case "hybrid" -> 949;
            case "diesel" -> 899;
            default -> 849;
        };
        String brand = brandOf(name);
        if (ULTRA_LUXURY_BRANDS.contains(brand)) base += 2500;
        else if (PREMIUM_BRANDS.contains(brand)) base += 250;
        base += Math.abs(name.hashCode() % 6) * 50;
        return new BigDecimal(base + ".00");
    }

    /** Drivlina ur motorfältet på en delad bil ("El · 1,55 kWh/mil" → elbil) — för omprisning vid boot. */
    public static String fuelFromEngine(String engine) {
        String e = engine.toLowerCase(Locale.ROOT);
        if (e.startsWith("el")) return "elbil";
        if (e.startsWith("laddhybrid")) return "laddhybrid";
        if (e.startsWith("hybrid")) return "hybrid";
        if (e.startsWith("diesel")) return "diesel";
        return "bensin";
    }

    static String brandOf(String model) {
        String[] parts = model.trim().split("\\s+");
        return parts.length == 0 ? "" : parts[0].toLowerCase(Locale.ROOT);
    }

    static String svDecimal(double v) {
        return String.format(Locale.ROOT, "%.2f", v).replace('.', ',');
    }
}
