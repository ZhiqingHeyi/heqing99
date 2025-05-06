package com.heqing.hotel.controller;

import com.heqing.hotel.model.Checkout;
import com.heqing.hotel.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/checkout")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @PostMapping("/create")
    public ResponseEntity<Checkout> createCheckout(@RequestBody Checkout checkout) {
        return ResponseEntity.ok(checkoutService.createCheckout(checkout));
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<Checkout> confirmCheckout(@PathVariable Long id) {
        return ResponseEntity.ok(checkoutService.confirmCheckout(id));
    }

    @GetMapping("/records")
    public ResponseEntity<Page<Checkout>> getCheckouts(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkoutDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(checkoutService.getCheckouts(
                roomNumber, guestName, checkoutDate, PageRequest.of(page, pageSize)));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getCheckoutStats() {
        return ResponseEntity.ok(checkoutService.getCheckoutStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Checkout> getCheckout(@PathVariable Long id) {
        return ResponseEntity.ok(checkoutService.getCheckout(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Checkout> updateCheckout(@PathVariable Long id, @RequestBody Checkout checkout) {
        return ResponseEntity.ok(checkoutService.updateCheckout(id, checkout));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckout(@PathVariable Long id) {
        checkoutService.deleteCheckout(id);
        return ResponseEntity.ok().build();
    }
} 