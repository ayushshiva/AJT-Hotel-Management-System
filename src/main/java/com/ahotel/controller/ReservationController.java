package com.ahotel.controller;

import com.ahotel.model.Reservation;
import com.ahotel.service.ReservationService;
import com.ahotel.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/booking-management")
public class ReservationController {
    private final ReservationService reservations;
    private final RoomService rooms;

    public ReservationController(ReservationService reservations, RoomService rooms) {
        this.reservations = reservations;
        this.rooms = rooms;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String status,
                       HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("reservations", reservations.findAll(q, status));
        model.addAttribute("search", q);
        model.addAttribute("statusFilter", status);
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        return "booking-list";
    }

    @GetMapping("/availability")
    @ResponseBody
    public Map<String, Object> availability(@RequestParam long roomId,
                                             @RequestParam LocalDate checkIn,
                                             @RequestParam LocalDate checkOut,
                                             HttpSession session) {
        if (!loggedIn(session)) {
            return Map.of("available", false, "message", "Sign in to check room availability.");
        }
        try {
            boolean available = reservations.isAvailable(roomId, checkIn, checkOut);
            return Map.of("available", available,
                    "message", available ? "Room is available for those dates."
                            : "Room is unavailable or has an overlapping active booking.");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return Map.of("available", false, "message", e.getMessage());
        }
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        form(model, session, new Reservation(), false);
        return "booking-form";
    }

    @PostMapping
    public String create(@ModelAttribute Reservation reservation, HttpSession session, Model model,
                         RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            long id = reservations.create(reservation);
            redirect.addFlashAttribute("success", "Booking reserved.");
            return "redirect:/booking-management/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            form(model, session, reservation, false);
            return "booking-form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, HttpSession session, Model model,
                          RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            model.addAttribute("reservation", reservations.findById(id));
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            return "booking-details";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking-management";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable long id, HttpSession session, Model model,
                       RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            form(model, session, reservations.findById(id), true);
            return "booking-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking-management";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id, @ModelAttribute Reservation reservation,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            reservations.update(id, reservation);
            redirect.addFlashAttribute("success", "Booking updated.");
            return "redirect:/booking-management/" + id;
        } catch (IllegalArgumentException e) {
            reservation.setId(id);
            model.addAttribute("error", e.getMessage());
            form(model, session, reservation, true);
            return "booking-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking-management";
        }
    }

    @PostMapping("/{id}/{action}")
    public String action(@PathVariable long id, @PathVariable String action,
                         HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            reservations.changeStatus(id, action);
            redirect.addFlashAttribute("success", "Booking status updated.");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/booking-management/" + id;
    }

    private void form(Model model, HttpSession session, Reservation reservation, boolean editing) {
        model.addAttribute("reservation", reservation);
        model.addAttribute("customers", reservations.findActiveCustomers());
        model.addAttribute("rooms", rooms.findAll("", "", ""));
        model.addAttribute("editing", editing);
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
    }

    private boolean loggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }
}
