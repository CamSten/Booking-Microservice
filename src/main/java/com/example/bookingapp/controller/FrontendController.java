package com.example.bookingapp.controller;

import com.example.bookingapp.model.*;
import com.example.bookingapp.service.CustomerService;
import com.example.bookingapp.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
public class FrontendController {
    private final RoomService roomService;
    private final CustomerService customerService;

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/home";
    }

    public FrontendController(RoomService roomService, CustomerService customerService) {
        this.roomService = roomService;
        this.customerService = customerService;
    }

    @GetMapping("/home")
    public String showHomePage(Model model,
                               @RequestParam(required = false) LocalDate startdate,
                               @RequestParam(required = false) LocalDate enddate) {
        if (startdate != null && enddate != null) {
            List<Room> availableRooms = roomService.findAvailableRooms(startdate, enddate);
            model.addAttribute("rooms", availableRooms);
        } else {
            model.addAttribute("rooms", roomService.getAllRooms());
        }
        return "homepage";
    }

    @GetMapping("/room")
    public String showRoomPage(@RequestParam Long id, @RequestParam(required = false) String startdate,
                               @RequestParam(required = false) String enddate, Model model) {
        model.addAttribute("room", roomService.getRoomById(id));
        model.addAttribute("startdate", startdate);
        model.addAttribute("enddate", enddate);
        return "roompage";
    }

    @GetMapping("/search")
    public String showSearchPage( ) {
        return "searchpage";
    }

    @GetMapping("/book")
    public String showBookingPage(@RequestParam Long roomId,
                                  @RequestParam(required = false) Long bookingId,
                                  @RequestParam(required = false) LocalDate startdate,
                                  @RequestParam(required = false) LocalDate enddate, Model model) {
        Room room = roomService.getRoomById(roomId);
        model.addAttribute("room", room);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("startdate", startdate);
        model.addAttribute("enddate", enddate);
        return "bookingpage";
    }

    @GetMapping("/customer")
    public String showCustomerPage(Model model) {
        model.addAttribute("loginCustomer", new CustomerDTO());
        model.addAttribute("signupCustomer", new CustomerDTO());
        return "customer";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginCustomer") CustomerDTO customer, HttpSession session, Model model,
                        @RequestParam(required = false) Boolean returnToBook, @RequestParam(required = false) Long roomId) {
        CustomerResponseDTO responseDTO = customerService.loginCustomer(customer.getEmail(), customer.getPassword());
        if (responseDTO.getFeedback() == Feedback.OK && responseDTO.getCustomerDTO() != null) {
            return getRerouteForValidSignIn(session, responseDTO, returnToBook, roomId);
        }
        model.addAttribute("error", responseDTO.getFeedback().feedback);
        model.addAttribute("loginCustomer", customer);
        model.addAttribute("signupCustomer", new CustomerDTO());
        return "customer";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("signupCustomer") CustomerDTO customer, HttpSession session, Model model,
                         @RequestParam(required = false) Boolean returnToBook, @RequestParam(required = false) Long roomId) {
        CustomerResponseDTO responseDTO = customerService.signupCustomer(customer);
        if (responseDTO.getFeedback() == Feedback.OK && responseDTO.getCustomerDTO() != null) {
            return getRerouteForValidSignIn(session, responseDTO, returnToBook, roomId);
        }
        model.addAttribute("signupError", responseDTO.getFeedback().feedback);
        model.addAttribute("signupCustomer", customer);
        model.addAttribute("loginCustomer", new CustomerDTO());
        return "customer";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customer";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("loginCustomerId");
        if (customerId == null) {
            return "redirect:/customer";
        }
        CustomerResponseDTO response = customerService.getCustomerById(customerId);
        if (response.getFeedback() == Feedback.CUSTOMER_SERVICE_UNAVAILABLE) {
            model.addAttribute("error", response.getFeedback().feedback);
            model.addAttribute("customer", new CustomerDTO());
            return "profile";
        }
        model.addAttribute("customer", response.getCustomerDTO());
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {
        Long customerId = (Long) session.getAttribute("loginCustomerId");
        if (customerId == null) {
            return "redirect:/customer";
        }
        CustomerResponseDTO response = customerService.getCustomerById(customerId);
        if (response.getFeedback() == Feedback.CUSTOMER_SERVICE_UNAVAILABLE) {
            model.addAttribute("error", response.getFeedback().feedback);
            return "editProfile";
        }
        model.addAttribute("customer", response.getCustomerDTO());
        return "editProfile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("customer") CustomerDTO customer, HttpSession session, Model model,
                                RedirectAttributes redirectAttributes) {
        Long customerId = (Long) session.getAttribute("loginCustomerId");
        if (customerId == null) {
            return "redirect:/customer";
        }
        CustomerResponseDTO responseDTO = customerService.updateCustomer(customerId, customer);
        if (responseDTO.getFeedback() != Feedback.OK) {
            model.addAttribute("editError", responseDTO.getFeedback().feedback);
            return "editProfile";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Your profile has been updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteCustomer(HttpSession session, RedirectAttributes redirectAttributes) {
        Long customerId = (Long) session.getAttribute("loginCustomerId");
        if (customerId == null) {
            return "redirect:/customer";
        }
        CustomerResponseDTO responseDTO = customerService.deleteCustomer(customerId);
        if(responseDTO.getFeedback() == Feedback.OK) {
            session.invalidate();
            return "redirect:/customer";
        }
        else {
            redirectAttributes.addFlashAttribute("deleteError", responseDTO.getFeedback().feedback);
            return "redirect:/profile";
        }
    }

    private String getRerouteForValidSignIn(HttpSession session, CustomerResponseDTO responseDTO, Boolean returnToBook, Long roomId ){
        session.setAttribute("loginCustomerId", responseDTO.getCustomerDTO().getId());
        session.setAttribute("jwtToken", responseDTO.getToken());
        if (Boolean.TRUE.equals(returnToBook) && roomId != null) {
            return "redirect:/book?roomId=" + roomId;
        }
        return "redirect:/profile";
    }
}