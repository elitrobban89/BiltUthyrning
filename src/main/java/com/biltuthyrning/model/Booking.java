package com.biltuthyrning.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    public enum BookingStatus {
        CONFIRMED, CANCELLED, COMPLETED
    }

    public Booking() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private Car car;
        private String customerName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalPrice;
        private BookingStatus status;

        public BookingBuilder car(Car car) {
            this.car = car;
            return this;
        }

        public BookingBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public BookingBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public BookingBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public BookingBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public BookingBuilder status(BookingStatus status) {
            this.status = status;
            return this;
        }

        public Booking build() {
            Booking booking = new Booking();
            booking.car = this.car;
            booking.customerName = this.customerName;
            booking.startDate = this.startDate;
            booking.endDate = this.endDate;
            booking.totalPrice = this.totalPrice;
            booking.status = this.status;
            return booking;
        }
    }
}

