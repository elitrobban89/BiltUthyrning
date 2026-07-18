package com.biltuthyrning.api;

import com.biltuthyrning.model.Car;
import com.biltuthyrning.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CarControllerTest {

    private MockMvc mvc;
    private CarService carService;

    @BeforeEach
    void setUp() {
        carService = mock(CarService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CarController(carService)).build();
    }

    @Test
    void listarFlottanMedForbrukningIMotorfaltet() throws Exception {
        Car shared = Car.builder().model("Tesla Model 3").year("–")
                .engine("El · 1,55 kWh/mil").dailyRate(new BigDecimal("1299.00")).build();
        shared.setId(11L);
        when(carService.getAllCars()).thenReturn(List.of(shared));

        mvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("Tesla Model 3"))
                .andExpect(jsonPath("$[0].engine").value("El · 1,55 kWh/mil"));
    }

    @Test
    void hamtarEnskildBil() throws Exception {
        Car car = new Car("Volvo XC40", "2023", "B4 AWD", new BigDecimal("899.00"));
        car.setId(3L);
        when(carService.getCarById(3L)).thenReturn(car);

        mvc.perform(get("/api/cars/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Volvo XC40"))
                .andExpect(jsonPath("$.dailyRate").value(899.00));
    }
}
