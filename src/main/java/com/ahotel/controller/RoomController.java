package com.ahotel.controller;

import com.ahotel.model.Room;
import com.ahotel.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService rooms;

    public RoomController(RoomService rooms) {
        this.rooms = rooms;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String type,
                       @RequestParam(defaultValue = "") String status,
                       HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            model.addAttribute("rooms", rooms.findAll(q, type, status));
            model.addAttribute("search", q);
            model.addAttribute("typeFilter", type);
            model.addAttribute("statusFilter", status);
            model.addAttribute("totalRooms", rooms.count());
            model.addAttribute("availableRooms", rooms.countAvailable());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("rooms", rooms.findAll(q, "", ""));
        }
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        return "room-list";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        Room room = new Room();
        room.setStatus("AVAILABLE");
        form(model, session, room, false);
        return "room-form";
    }

    @PostMapping
    public String create(@ModelAttribute Room room, HttpSession session, Model model,
                         RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            long id = rooms.create(room);
            redirect.addFlashAttribute("success", "Room added.");
            return "redirect:/rooms/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            form(model, session, room, false);
            return "room-form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, HttpSession session, Model model,
                          RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            model.addAttribute("room", rooms.findById(id));
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            return "room-details";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/rooms";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable long id, HttpSession session, Model model,
                       RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            form(model, session, rooms.findById(id), true);
            return "room-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/rooms";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id, @ModelAttribute Room room, HttpSession session,
                         Model model, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            rooms.update(id, room);
            redirect.addFlashAttribute("success", "Room updated.");
            return "redirect:/rooms/" + id;
        } catch (IllegalArgumentException e) {
            room.setId(id);
            model.addAttribute("error", e.getMessage());
            form(model, session, room, true);
            return "room-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/rooms";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            rooms.delete(id);
            redirect.addFlashAttribute("success", "Room deleted.");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    private void form(Model model, HttpSession session, Room room, boolean editing) {
        model.addAttribute("room", room);
        model.addAttribute("editing", editing);
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
    }

    private boolean loggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }
}
