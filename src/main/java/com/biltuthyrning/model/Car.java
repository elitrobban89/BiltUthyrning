package com.biltuthyrning.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String year;

    @Column(nullable = false)
    private String engine;

    @Column(nullable = false)
    private BigDecimal dailyRate;

    public Car() {}

    public Car(String model, String year, String engine, BigDecimal dailyRate) {
        this.model = model;
        this.year = year;
        this.engine = engine;
        this.dailyRate = dailyRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public static CarBuilder builder() {
        return new CarBuilder();
    }

    public static class CarBuilder {
        private String model;
        private String year;
        private String engine;
        private BigDecimal dailyRate;

        public CarBuilder model(String model) {
            this.model = model;
            return this;
        }

        public CarBuilder year(String year) {
            this.year = year;
            return this;
        }

        public CarBuilder engine(String engine) {
            this.engine = engine;
            return this;
        }

        public CarBuilder dailyRate(BigDecimal dailyRate) {
            this.dailyRate = dailyRate;
            return this;
        }

        public Car build() {
            Car car = new Car();
            car.model = this.model;
            car.year = this.year;
            car.engine = this.engine;
            car.dailyRate = this.dailyRate;
            return car;
        }
    }
}

