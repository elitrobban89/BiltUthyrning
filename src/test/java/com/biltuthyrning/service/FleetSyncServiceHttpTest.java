package com.biltuthyrning.service;

import com.biltuthyrning.model.Car;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** HTTP-felvägar mot lokal stubbserver — samma mönster som systerprojektens HTTP-tester. */
class FleetSyncServiceHttpTest {

    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void startStub() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ok/api/ev-consumption", ex -> respond(ex, 200,
                "[{\"carName\":\"Tesla Model 3\",\"kwhPerMil\":1.55}]"));
        server.createContext("/ok/api/ice-consumption", ex -> respond(ex, 200,
                "[{\"carName\":\"Volkswagen Golf 1.5 TSI\",\"fuel\":\"bensin\",\"literPerMil\":0.65}]"));
        server.createContext("/fel/api/ev-consumption", ex -> respond(ex, 500, "boom"));
        server.createContext("/fel/api/ice-consumption", ex -> respond(ex, 500, "boom"));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopStub() {
        server.stop(0);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange ex, int status, String body)
            throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    @Test
    void hamtarOchByggerFlottaFranStubben() {
        FleetSyncService service = new FleetSyncService();
        ReflectionTestUtils.setField(service, "apiUrl", baseUrl + "/ok");
        List<Car> fleet = service.fetchSharedFleet(10);
        assertThat(fleet).extracting(Car::getModel)
                .containsExactlyInAnyOrder("Tesla Model 3", "Volkswagen Golf 1.5 TSI");
    }

    @Test
    void serverfelGerTomListaUtanException() {
        FleetSyncService service = new FleetSyncService();
        ReflectionTestUtils.setField(service, "apiUrl", baseUrl + "/fel");
        assertThat(service.fetchSharedFleet(10)).isEmpty();
    }

    @Test
    void nollLimitHamtarInget() {
        FleetSyncService service = new FleetSyncService();
        ReflectionTestUtils.setField(service, "apiUrl", "http://localhost:1"); // skulle faila om det anropades
        assertThat(service.fetchSharedFleet(0)).isEmpty();
    }
}
