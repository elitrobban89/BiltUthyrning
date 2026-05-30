package com.biltuthyrning.api;

import com.biltuthyrning.model.Car;
import com.biltuthyrning.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }
    
    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }
    
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }
}

