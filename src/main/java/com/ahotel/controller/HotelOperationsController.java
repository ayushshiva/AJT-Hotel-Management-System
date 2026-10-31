package com.ahotel.controller;

import com.ahotel.service.HotelOperationsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HotelOperationsController {
    private final HotelOperationsService operations;

    public HotelOperationsController(HotelOperationsService operations) {
        this.operations = operations;
    }

    @GetMapping("/food")
    public String food(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "") String category,
                       HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("search", q);
        model.addAttribute("categoryFilter", category);
        model.addAttribute("foodItems", operations.foodItems(q, category));
        model.addAttribute("foodCategories", operations.foodCategories());
        model.addAttribute("foodOrders", operations.foodOrders());
        model.addAttribute("foodOrderLines", operations.foodOrderLines());
        model.addAttribute("checkedInBookings", operations.checkedInBookings());
        return "food";
    }

    @PostMapping("/food/items")
    public String createFoodItem(@RequestParam String name, @RequestParam String category,
                                 @RequestParam(required = false) String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam(defaultValue = "false") boolean available,
                                 HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.saveFoodItem(null, name, category, description, price, available);
            redirect.addFlashAttribute("success", "Menu item added.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/food";
    }

    @PostMapping("/food/items/{id}")
    public String updateFoodItem(@PathVariable long id, @RequestParam String name,
                                 @RequestParam String category, @RequestParam(required = false) String description,
                                 @RequestParam BigDecimal price,
                                 @RequestParam(defaultValue = "false") boolean available,
                                 HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.saveFoodItem(id, name, category, description, price, available);
            redirect.addFlashAttribute("success", "Menu item updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/food";
    }

    @PostMapping("/food/items/{id}/delete")
    public String deleteFoodItem(@PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.deleteFoodItem(id);
            redirect.addFlashAttribute("success", "Menu item deleted.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/food";
    }

    @PostMapping("/food/orders")
    public String createFoodOrder(@RequestParam long reservationId,
                                  @RequestParam(required = false) List<Long> itemIds,
                                  @RequestParam MultiValueMap<String, String> parameters,
                                  HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            Map<Long, Integer> quantities = new LinkedHashMap<>();
            if (itemIds != null) {
                for (Long itemId : itemIds) {
                    String quantity = parameters.getFirst("quantity_" + itemId);
                    quantities.put(itemId, quantity == null ? 1 : Integer.parseInt(quantity));
                }
            }
            long id = operations.placeFoodOrder(reservationId, quantities);
            redirect.addFlashAttribute("success", "Food order #" + id + " placed.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/food";
    }

    @PostMapping("/food/orders/{id}/status")
    public String foodOrderStatus(@PathVariable long id, @RequestParam String status,
                                  HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.changeFoodOrderStatus(id, status);
            redirect.addFlashAttribute("success", "Food order status updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/food";
    }

    @GetMapping("/laundry")
    public String laundry(@RequestParam(defaultValue = "") String q, HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("search", q);
        model.addAttribute("requests", operations.laundryRequests(q));
        model.addAttribute("checkedInBookings", operations.checkedInBookings());
        return "laundry";
    }

    @PostMapping("/laundry")
    public String createLaundry(@RequestParam long reservationId, @RequestParam String itemName,
                                @RequestParam int quantity, @RequestParam BigDecimal ratePerItem,
                                @RequestParam(required = false) LocalDate expectedDeliveryDate,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.createLaundry(reservationId, itemName, quantity, ratePerItem, expectedDeliveryDate);
            redirect.addFlashAttribute("success", "Laundry request created.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/laundry";
    }

    @PostMapping("/laundry/{id}")
    public String updateLaundry(@PathVariable long id, @RequestParam long reservationId,
                                @RequestParam String itemName, @RequestParam int quantity,
                                @RequestParam BigDecimal ratePerItem,
                                @RequestParam(required = false) LocalDate expectedDeliveryDate,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.updateLaundry(id, reservationId, itemName, quantity, ratePerItem, expectedDeliveryDate);
            redirect.addFlashAttribute("success", "Laundry request updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/laundry";
    }

    @PostMapping("/laundry/{id}/status")
    public String laundryStatus(@PathVariable long id, @RequestParam String status,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.changeLaundryStatus(id, status);
            redirect.addFlashAttribute("success", "Laundry status updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/laundry";
    }

    @GetMapping("/room-service")
    public String roomService(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("requests", operations.serviceRequests());
        model.addAttribute("checkedInBookings", operations.checkedInBookings());
        model.addAttribute("employees", operations.employees("", ""));
        return "room-service";
    }

    @PostMapping("/room-service")
    public String createService(@RequestParam long reservationId, @RequestParam String serviceType,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal serviceCharge, HttpSession session,
                                RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.createServiceRequest(reservationId, serviceType, description, serviceCharge);
            redirect.addFlashAttribute("success", "Room service request created.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/room-service";
    }

    @PostMapping("/room-service/{id}")
    public String updateService(@PathVariable long id, @RequestParam long reservationId,
                                @RequestParam String serviceType,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal serviceCharge, HttpSession session,
                                RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.updateServiceRequest(id, reservationId, serviceType, description, serviceCharge);
            redirect.addFlashAttribute("success", "Service request updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/room-service";
    }

    @PostMapping("/room-service/{id}/assign")
    public String assignService(@PathVariable long id, @RequestParam Long employeeId,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.assignServiceEmployee(id, employeeId);
            redirect.addFlashAttribute("success", "Staff assigned.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/room-service";
    }

    @PostMapping("/room-service/{id}/status")
    public String serviceStatus(@PathVariable long id, @RequestParam String status,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.changeServiceStatus(id, status);
            redirect.addFlashAttribute("success", "Service status updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/room-service";
    }

    @GetMapping("/employees")
    public String employees(@RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "") String designation,
                            HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("search", q);
        model.addAttribute("designationFilter", designation);
        model.addAttribute("employees", operations.employees(q, designation));
        return "employees";
    }

    @PostMapping("/employees")
    public String createEmployee(@RequestParam(required = false) Long id, @RequestParam String fullName,
                                 @RequestParam String mobileNumber, @RequestParam String email,
                                 @RequestParam(required = false) String address,
                                 @RequestParam String designation, @RequestParam String department,
                                 @RequestParam BigDecimal salary, @RequestParam LocalDate joiningDate,
                                 HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.saveEmployee(id, fullName, mobileNumber, email, address, designation,
                    department, salary, joiningDate);
            redirect.addFlashAttribute("success", id == null ? "Employee added." : "Employee updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees";
    }

    @PostMapping("/employees/{id}/deactivate")
    public String deactivateEmployee(@PathVariable long id, HttpSession session,
                                     RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.deactivateEmployee(id);
            redirect.addFlashAttribute("success", "Employee deactivated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/billing")
    public String billing(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("invoices", operations.invoices());
        model.addAttribute("completedBookings", operations.completedBookingsWithoutInvoice());
        return "billing";
    }

    @PostMapping("/billing")
    public String createInvoice(@RequestParam long reservationId, @RequestParam String paymentMethod,
                                HttpSession session, RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            long id = operations.createInvoice(reservationId, paymentMethod);
            redirect.addFlashAttribute("success", "Invoice generated.");
            return "redirect:/billing/" + id;
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/billing";
        }
    }

    @GetMapping("/billing/{id}")
    public String invoice(@PathVariable long id, HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("invoice", operations.invoice(id));
        model.addAttribute("payments", operations.invoicePayments(id));
        return "invoice";
    }

    @PostMapping("/billing/{id}/payments")
    public String payment(@PathVariable long id, @RequestParam BigDecimal amount,
                          @RequestParam String paymentMethod, HttpSession session,
                          RedirectAttributes redirect) {
        if (!loggedIn(session)) return "redirect:/login";
        try {
            operations.recordPayment(id, amount, paymentMethod);
            redirect.addFlashAttribute("success", "Payment recorded.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billing/" + id;
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(defaultValue = "revenue") String report,
                          @RequestParam(required = false) LocalDate from,
                          @RequestParam(required = false) LocalDate to,
                          HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("report", report);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        try {
            List<Map<String, Object>> rows = operations.report(report, from, to);
            model.addAttribute("rows", rows);
            model.addAttribute("recordCount", rows.size());
            BigDecimal paidRevenue = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                if (row.get("paid_amount") instanceof BigDecimal amount) {
                    paidRevenue = paidRevenue.add(amount);
                }
            }
            model.addAttribute("paidRevenue", paidRevenue);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("rows", List.of());
            model.addAttribute("recordCount", 0);
            model.addAttribute("paidRevenue", BigDecimal.ZERO);
        }
        return "reports";
    }

    private boolean loggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }
}
