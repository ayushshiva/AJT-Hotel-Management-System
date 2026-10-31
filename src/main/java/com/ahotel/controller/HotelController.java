package com.ahotel.controller;

import com.ahotel.model.Booking;
import com.ahotel.service.BookingService;
import com.ahotel.service.ReservationService;
import com.ahotel.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HotelController {
    private final BookingService service;
    private final RoomService rooms;
    private final ReservationService reservations;
    public HotelController(BookingService service, RoomService rooms, ReservationService reservations) {
        this.service = service;
        this.rooms = rooms;
        this.reservations = reservations;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session) {
        return session.getAttribute("loggedInUser") != null ? "redirect:/dashboard" : "login";
    }

    // Academic demonstration login. Replace these credentials with database-backed authentication for production.
    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password,
                        HttpSession session, RedirectAttributes redirect) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("loggedInUser", username);
            return "redirect:/dashboard";
        }
        redirect.addFlashAttribute("error", "Invalid username or password.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String home(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("bookings", service.findAll());
        model.addAttribute("bookingCount", service.count());
        model.addAttribute("roomCount", rooms.count());
        model.addAttribute("availableRoomCount", rooms.countAvailable());
        model.addAttribute("activeReservationCount", reservations.countActive());
        return "dashboard";
    }

    @PostMapping("/bookings")
    public String save(@ModelAttribute Booking booking, HttpSession session, RedirectAttributes redirect) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/login";
        try {
            service.create(booking);
            redirect.addFlashAttribute("success", "Booking saved.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
