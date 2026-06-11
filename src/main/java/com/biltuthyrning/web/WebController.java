package com.biltuthyrning.web;

import com.biltuthyrning.model.Booking;
import com.biltuthyrning.service.BookingService;
import com.biltuthyrning.service.CarService;
import com.biltuthyrning.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class WebController {

    private final CarService carService;
    private final BookingService bookingService;
    private final UserService userService;

    public WebController(CarService carService, BookingService bookingService, UserService userService) {
        this.carService = carService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        var cars     = carService.getAllCars();
        var bookings = bookingService.getAllBookings();
        long active  = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
        int revenue  = bookings.stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
        model.addAttribute("cars", cars);
        model.addAttribute("bookings", bookings);
        model.addAttribute("activeBookings", active);
        model.addAttribute("totalRevenue", revenue);
        model.addAttribute("username", user.getUsername());
        return "dashboard";
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            RedirectAttributes ra) {
        try {
            userService.register(username, password, "USER");
            ra.addFlashAttribute("regSuccess", "Kontot skapat! Logga in nu.");
        } catch (Exception e) {
            ra.addFlashAttribute("regError", e.getMessage());
        }
        return "redirect:/login";
    }

    @PostMapping("/book")
    public String createBooking(
            @RequestParam Long carId,
            @RequestParam String customerName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes ra) {
        try {
            Booking b = bookingService.createBooking(carId, customerName, startDate, endDate);
            ra.addFlashAttribute("success", "Bokning bekräftad! Boknings-ID: " + b.getId());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Bokningsfel: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.cancelBooking(id);
            ra.addFlashAttribute("success", "Bokning avbokad!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Fel: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/complete/{id}")
    public String completeBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.completeBooking(id);
            ra.addFlashAttribute("success", "Bokning markerad som avslutad!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Fel: " + e.getMessage());
        }
        return "redirect:/";
    }
}
