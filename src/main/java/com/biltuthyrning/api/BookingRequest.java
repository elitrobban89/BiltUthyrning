package com.biltuthyrning.api;

import java.time.LocalDate;

public class BookingRequest {
    private Long carId;
    private String customerName;
    private LocalDate startDate;
    private LocalDate endDate;

    public BookingRequest() {}

    public BookingRequest(Long carId, String customerName, LocalDate startDate, LocalDate endDate) {
        this.carId = carId;
        this.customerName = customerName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}

