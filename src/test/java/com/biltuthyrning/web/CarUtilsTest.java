package com.biltuthyrning.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    void badgeFargArDeterministiskForNyaMarken() {
        assertThat(utils.getBadgeColor("Tesla Model 3"))
                .isEqualTo(utils.getBadgeColor("Tesla Model 3"))
                .matches("#[0-9A-F]{6}");
        // kurerade Volvo-flottan behåller sina handvalda färger
        assertThat(utils.getBadgeColor("Volvo XC40")).isEqualTo("#2471A3");
    }
}
