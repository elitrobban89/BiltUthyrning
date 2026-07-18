package com.biltuthyrning.service;

import com.biltuthyrning.model.Car;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleetSyncServiceTest {

    private final FleetSyncService service = new FleetSyncService();

    @Test
    void parseEvByggerBilarMedVerifieradForbrukning() throws Exception {
        String json = """
            [{"carName":"Tesla Model 3","kwhPerMil":1.55},
             {"carName":"","kwhPerMil":1.0},
             {"carName":"Trasig Utan Data","kwhPerMil":0}]
            """;
        List<Car> cars = service.parseEv(json);
        assertThat(cars).hasSize(1);
        assertThat(cars.get(0).getModel()).isEqualTo("Tesla Model 3");
        assertThat(cars.get(0).getYear()).isEqualTo("–");
        assertThat(cars.get(0).getEngine()).isEqualTo("El · 1,55 kWh/mil");
        assertThat(cars.get(0).getDailyRate()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void parseIceKapitaliserarDrivmedelIMotorfaltet() throws Exception {
        String json = """
            [{"carName":"Volkswagen Golf 1.5 TSI","fuel":"bensin","literPerMil":0.65},
             {"carName":"Volvo XC60 D5","fuel":"diesel","literPerMil":0.62}]
            """;
        List<Car> cars = service.parseIce(json);
        assertThat(cars).hasSize(2);
        assertThat(cars.get(0).getEngine()).isEqualTo("Bensin · 0,65 l/mil");
        assertThat(cars.get(1).getEngine()).isEqualTo("Diesel · 0,62 l/mil");
    }

    @Test
    void trasigJsonKastarUppat() {
        // fetchSharedFleet fångar och returnerar tom lista — parsarna själva ska kasta
        assertThatThrownBy(() -> service.parseEv("inte json")).isInstanceOf(Exception.class);
    }

    @Test
    void prisetArDeterministisktOchPremiumMarkenDyrare() {
        assertThat(FleetSyncService.priceFor("Tesla Model 3", "elbil"))
                .isEqualTo(FleetSyncService.priceFor("Tesla Model 3", "elbil"));
        // premiumpåslag: Tesla-el minst 999 + 250; Dacia-el aldrig över 999 + hashvariation (250)
        assertThat(FleetSyncService.priceFor("Tesla Model 3", "elbil"))
                .isGreaterThanOrEqualTo(new BigDecimal("1249.00"));
        assertThat(FleetSyncService.priceFor("Dacia Spring", "elbil"))
                .isBetween(new BigDecimal("999.00"), new BigDecimal("1249.00"));
        // drivlinan styr basen: diesel < laddhybrid för samma namn
        assertThat(FleetSyncService.priceFor("Kia Sportage", "diesel"))
                .isLessThan(FleetSyncService.priceFor("Kia Sportage", "laddhybrid"));
    }

    @Test
    void ultralyxMarkenFarEgenPrisniva() {
        // Rolls-Royce Spectre hyrdes ut för 1 099 kr/dag innan ultralyxnivån fanns
        assertThat(FleetSyncService.priceFor("Rolls-Royce Spectre Series II", "elbil"))
                .isGreaterThanOrEqualTo(new BigDecimal("3499.00"));
        assertThat(FleetSyncService.priceFor("Bentley Continental GT", "bensin"))
                .isGreaterThanOrEqualTo(new BigDecimal("3349.00"));
        // ultralyx ersätter premiumpåslaget, staplas inte ovanpå
        assertThat(FleetSyncService.priceFor("Rolls-Royce Spectre Series II", "elbil"))
                .isLessThanOrEqualTo(new BigDecimal("3749.00"));
    }

    @Test
    void drivlinaLasesUrMotorfaltet() {
        assertThat(FleetSyncService.fuelFromEngine("El · 1,55 kWh/mil")).isEqualTo("elbil");
        assertThat(FleetSyncService.fuelFromEngine("Diesel · 0,62 l/mil")).isEqualTo("diesel");
        assertThat(FleetSyncService.fuelFromEngine("Laddhybrid · 0,12 l/mil")).isEqualTo("laddhybrid");
        assertThat(FleetSyncService.fuelFromEngine("Hybrid · 0,45 l/mil")).isEqualTo("hybrid");
        assertThat(FleetSyncService.fuelFromEngine("Bensin · 0,65 l/mil")).isEqualTo("bensin");
    }

    @Test
    void selectSpreadTarMaxTvaPerMarkeOchBlandarDrivlinor() {
        List<Car> evs = List.of(ev("Tesla Model 3"), ev("Tesla Model Y"), ev("Tesla Model S"),
                ev("Kia EV6"));
        List<Car> ices = List.of(ice("Skoda Octavia"), ice("Skoda Kamiq"), ice("Skoda Fabia"));
        List<Car> fleet = FleetSyncService.selectSpread(evs, ices, 100);
        assertThat(fleet).extracting(Car::getModel)
                .containsExactlyInAnyOrder("Tesla Model 3", "Tesla Model Y",
                        "Kia EV6", "Skoda Octavia", "Skoda Kamiq");
        // blandningen: första två platserna är en el och en fossil
        assertThat(fleet.get(0).getEngine()).startsWith("El");
        assertThat(fleet.get(1).getEngine()).startsWith("Bensin");
    }

    @Test
    void selectSpreadHopparOverDubbladeModellnamn() {
        // EV-listan hade två varianter med visningsnamnet Rolls-Royce Spectre Series II
        List<Car> evs = List.of(ev("Rolls-Royce Spectre Series II"),
                ev("Rolls-Royce Spectre Series II"), ev("Kia EV6"));
        List<Car> fleet = FleetSyncService.selectSpread(evs, List.of(), 10);
        assertThat(fleet).extracting(Car::getModel)
                .containsExactly("Rolls-Royce Spectre Series II", "Kia EV6");
    }

    @Test
    void selectSpreadRespekterarLimit() {
        List<Car> evs = List.of(ev("Tesla Model 3"), ev("Kia EV6"), ev("Volvo EX30"));
        List<Car> ices = List.of(ice("Skoda Octavia"), ice("Toyota Yaris"));
        assertThat(FleetSyncService.selectSpread(evs, ices, 3)).hasSize(3);
        assertThat(FleetSyncService.selectSpread(evs, ices, 0)).isEmpty();
    }

    private static Car ev(String name) {
        return Car.builder().model(name).year("–").engine("El · 1,70 kWh/mil")
                .dailyRate(new BigDecimal("999.00")).build();
    }

    private static Car ice(String name) {
        return Car.builder().model(name).year("–").engine("Bensin · 0,65 l/mil")
                .dailyRate(new BigDecimal("849.00")).build();
    }
}
