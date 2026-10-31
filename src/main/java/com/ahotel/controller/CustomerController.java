package com.ahotel.controller;

import com.ahotel.model.Customer;
import com.ahotel.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    private static final int PAGE_SIZE = 10;

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        String filter = status;
        int total;
        try {
            total = customers.count(q, filter);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            filter = "";
            total = customers.count(q, filter);
        }
        int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        int currentPage = Math.max(0, Math.min(page, pageCount - 1));
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("customers", customers.findPage(q, filter, PAGE_SIZE, currentPage * PAGE_SIZE));
        model.addAttribute("recentCustomers", customers.findRecent(5));
        model.addAttribute("totalCustomers", customers.countAll());
        model.addAttribute("activeCustomers", customers.countActive());
        model.addAttribute("filteredCount", total);
        model.addAttribute("search", q);
        model.addAttribute("statusFilter", filter);
        model.addAttribute("page", currentPage);
        model.addAttribute("pageCount", pageCount);
        return "customer-list";
    }

    @GetMapping("/new")
    public String newCustomer(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        Customer customer = new Customer();
        customer.setStatus("ACTIVE");
        addFormModel(model, customer, false, session);
        return "customer-form";
    }

    @PostMapping
    public String create(@ModelAttribute Customer customer, HttpSession session,
                         Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            long id = customers.create(customer);
            redirect.addFlashAttribute("success", "Customer registered successfully.");
            return "redirect:/customers/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            addFormModel(model, customer, false, session);
            return "customer-form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable long id, HttpSession session, Model model,
                          RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            Customer customer = customers.findById(id);
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            model.addAttribute("customer", customer);
            model.addAttribute("bookings", customers.findBookingHistory(id));
            model.addAttribute("reservations", customers.findReservationHistory(id));
            model.addAttribute("maskedProofSuffix", maskedProofSuffix(customer.getIdProofNumber()));
            return "customer-details";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable long id, HttpSession session, Model model,
                       RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            addFormModel(model, customers.findById(id), true, session);
            return "customer-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id, @ModelAttribute Customer customer,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            customers.update(id, customer);
            redirect.addFlashAttribute("success", "Customer details updated.");
            return "redirect:/customers/" + id;
        } catch (IllegalArgumentException e) {
            customer.setId(id);
            model.addAttribute("error", e.getMessage());
            addFormModel(model, customer, true, session);
            return "customer-form";
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            boolean deleted = customers.deleteOrDeactivate(id);
            redirect.addFlashAttribute("success", deleted
                    ? "Customer deleted."
                    : "Customer has booking history and was deactivated to preserve those records.");
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    private void addFormModel(Model model, Customer customer, boolean editing, HttpSession session) {
        model.addAttribute("customer", customer);
        model.addAttribute("editing", editing);
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    private String maskedProofSuffix(String proofNumber) {
        if (proofNumber == null || proofNumber.isBlank()) {
            return "";
        }
        if (proofNumber.length() <= 4) {
            return "****";
        }
        return proofNumber.substring(Math.max(0, proofNumber.length() - 4));
    }
}
