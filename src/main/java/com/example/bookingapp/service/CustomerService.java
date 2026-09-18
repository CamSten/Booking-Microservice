package com.example.bookingapp.service;

import com.example.bookingapp.config.RestTemplateConfig;
import com.example.bookingapp.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerService {
    private final HttpSession session;
    private final RestTemplate restTemplate;
    @Value("${customer.service.url}")
    private String customerServiceUrl;

    public CustomerService(RestTemplateConfig restTemplateConfig, HttpSession session) {
        this.restTemplate = restTemplateConfig.restTemplate();
        this.session = session;
    }

    public CustomerResponseDTO loginCustomer(String email, String password) {
        try {
            LoginRequestDTO request = new LoginRequestDTO(email, password);
            LoginResponseDTO response = restTemplate.postForObject(customerServiceUrl + "/login", request, LoginResponseDTO.class);
            return (response != null)
                    ? new CustomerResponseDTO(response.getCustomer(), Feedback.OK, response.getToken())
                    : new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (ResourceAccessException e) {
            return new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (HttpStatusCodeException e) {
            return new CustomerResponseDTO(getLoginFeedbackFromStatus(e.getStatusCode()));
        }
    }

    public CustomerResponseDTO signupCustomer(CustomerDTO dto) {
        try {
            LoginResponseDTO response = restTemplate.postForObject(customerServiceUrl + "/signup", dto, LoginResponseDTO.class);
            return (response != null)
                    ? new CustomerResponseDTO(response.getCustomer(), Feedback.OK, response.getToken())
                    : new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (ResourceAccessException e) {
            return new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (HttpStatusCodeException e) {
            return new CustomerResponseDTO(getFeedbackFromStatus(e.getStatusCode()));
        }
    }

    public CustomerResponseDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        try {
            HttpEntity<CustomerDTO> request = new HttpEntity<>(customerDTO, authenticatedHeaders());
            restTemplate.exchange(customerServiceUrl + "/" + customerId, HttpMethod.PUT, request, CustomerDTO.class);
            return new CustomerResponseDTO(Feedback.OK);
        } catch (ResourceAccessException e) {
            return new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            return new CustomerResponseDTO(Feedback.INVALID_USER);
        }
    }

    public CustomerResponseDTO deleteCustomer(Long customerId) {
        try {
            restTemplate.exchange(customerServiceUrl + "/" + customerId, HttpMethod.DELETE, authenticatedRequest(), Void.class);
            return new CustomerResponseDTO(Feedback.OK);
        } catch (ResourceAccessException e) {
            return new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            return new CustomerResponseDTO(Feedback.HAS_ACTIVE_BOOKINGS);
        }
    }

    public CustomerResponseDTO getCustomerById(Long customerId) {
        try {
            ResponseEntity<CustomerDTO> response = restTemplate.exchange(customerServiceUrl + "/" + customerId,
                    HttpMethod.GET, authenticatedRequest(), CustomerDTO.class);
            CustomerDTO dto = response.getBody();
            return (dto != null) ? new CustomerResponseDTO(dto, Feedback.OK) : new CustomerResponseDTO(Feedback.INVALID_USER);
        } catch (ResourceAccessException e) {
            return new CustomerResponseDTO(Feedback.CUSTOMER_SERVICE_UNAVAILABLE);
        } catch (HttpStatusCodeException e) {
            return new CustomerResponseDTO(getFeedbackFromStatus(e.getStatusCode()));
        }
    }

    private HttpEntity<Void> authenticatedRequest() {
        String token = (String) session.getAttribute("jwtToken");
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        return new HttpEntity<>(headers);
    }

    private HttpHeaders authenticatedHeaders() {
        String token = (String) session.getAttribute("jwtToken");
        HttpHeaders headers = new HttpHeaders();

        if (token != null && !token.isBlank()) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private Feedback getFeedbackFromStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> Feedback.EMPTY_EMAIL;
            case 401 -> Feedback.UNAUTHORIZED;
            case 404 -> Feedback.INVALID_EMAIL;
            case 409 -> Feedback.USER_EXISTS;
            default -> Feedback.CUSTOMER_SERVICE_UNAVAILABLE;
        };
    }

    private Feedback getLoginFeedbackFromStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> Feedback.EMPTY_EMAIL;
            case 401 -> Feedback.INVALID_PASSWORD;
            case 404 -> Feedback.INVALID_EMAIL;
            default -> Feedback.CUSTOMER_SERVICE_UNAVAILABLE;
        };
    }
}