package com.biltuthyrning.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Robert Andersson Kopler */
class CarUtilsTest {

    private final CarUtils utils = new CarUtils();

    @Test
    void evBadgeStyrsAvMotornInteModellnamnet() {
        // "Lexus" innehåller "EX" — badgen får aldrig utgå från modellnamnet
        assertThat(utils.getEVBadge("Bensin · 0,72 l/mil")).isEmpty();
        assertThat(utils.getEVBadge("El · 1,55 kWh/mil")).isEqualTo("EV ⚡");
        assertThat(utils.getEVBadge("El — 315 kW Twin Motor")).isEqualTo("EV ⚡");
        assertThat(utils.getEVBadge("T8 Laddhybrid — 340 kW")).isEqualTo("PHEV 🔌");
        assertThat(utils.getEVBadge("Laddhybrid · 0,12 l/mil")).isEqualTo("PHEV 🔌");
    }

    @Test
    void drivmedelKannerIgenAllaFemTyper() {
        assertThat(utils.getFuelType("El · 1,55 kWh/mil")).isEqualTo("El");
        assertThat(utils.getFuelType("Diesel · 0,62 l/mil")).isEqualTo("Diesel");
        assertThat(utils.getFuelType("Hybrid · 0,45 l/mil")).isEqualTo("Hybrid");
        assertThat(utils.getFuelType("T8 Laddhybrid — 340 kW")).isEqualTo("Laddhybrid");
        assertThat(utils.getFuelType("B4 AWD")).isEqualTo("Bensin");
    }

    @Test
    void emblemHittasOavsettStavning() {
        assertThat(utils.getEmblem("Volvo XC40")).isEqualTo("volvo");
        // diakriter: flottan skriver Škoda och Citroën, filnamnen gör det inte
        assertThat(utils.getEmblem("Škoda Enyaq iV 80")).isEqualTo("skoda");
        assertThat(utils.getEmblem("Citroën ë-C4")).isEqualTo("citroen");
        // samma märke, två stavningar i datan
        assertThat(utils.getEmblem("Mercedes-Benz EQE")).isEqualTo("mercedes");
        assertThat(utils.getEmblem("Mercedes EQB")).isEqualTo("mercedes");
        // märke och modell i samma ord
        assertThat(utils.getEmblem("MG4 Standard")).isEqualTo("mg");
        assertThat(utils.getEmblem("MG5 Long Range")).isEqualTo("mg");
        assertThat(utils.getEmblem("MG ZS EV")).isEqualTo("mg");
        // märket skrivet både med och utan bindestreck
        assertThat(utils.getEmblem("Rolls-Royce Spectre")).isEqualTo("rollsroyce");
        assertThat(utils.getEmblem("Rolls Royce Spectre")).isEqualTo("rollsroyce");
    }

    @Test
    void markeUtanFrittEmblemFallerTillbakaPaEmoji() {
        // Tomt = mallen ritar bil-emojin. Ett FEL emblem vore sämre än inget, och de här
        // märkena saknar en fri och läsbar symbol — se kommentaren i CarUtils.
        assertThat(utils.getEmblem("Hyundai Ioniq 5")).isEmpty();
        assertThat(utils.getEmblem("Peugeot e-208")).isEmpty();
        assertThat(utils.getEmblem("XPENG G6")).isEmpty();
        assertThat(utils.getEmblem("")).isEmpty();
        assertThat(utils.getEmblem(null)).isEmpty();
    }

    @Test
    void badgeFargArDeterministiskForNyaMarken() {
        assertThat(utils.getBadgeColor("Tesla Model 3"))
                .isEqualTo(utils.getBadgeColor("Tesla Model 3"))
                .matches("#[0-9A-F]{6}");
        // kurerade Volvo-flottan behåller sina handvalda färger
        assertThat(utils.getBadgeColor("Volvo XC40")).isEqualTo("#2471A3");
    }
}
