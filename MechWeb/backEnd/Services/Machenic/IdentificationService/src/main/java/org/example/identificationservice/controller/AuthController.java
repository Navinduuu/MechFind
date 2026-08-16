package org.example.identificationservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.identificationservice.model.User;
import org.example.identificationservice.model.UserType;
import org.example.identificationservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mechfind/auth")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginInput input) {
        try {
            User user = userService.loginUser(input.getEmail(), input.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException ex) {
            log.warn("Login unauthorized for email {}: {}", input.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping("/initiate-register")
    public ResponseEntity<?> initiateRegister(@Valid @RequestBody RegisterInput input) {
        try {
            userService.validateEmailUniqueness(input.getEmail());
            return ResponseEntity.ok("Email is available. Proceed to OTP verification.");
        } catch (RuntimeException ex) {
            log.error("Initiate registration error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterInput input) {
        try {
            User registeredUser = userService.registerUser(input);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException ex) {
            log.error("Registration error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordInput input) {
        try {
            userService.changePassword(
                    input.getEmail(),
                    input.getOldPassword(),
                    input.getNewPassword()
            );
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException ex) {
            log.warn("Password change failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public static class RegisterInput {
        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        // Removed strict @NotNull to prevent 400 Bad Request when registering as TowTruck or omitting specialty
        private String speciality;

        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotNull(message = "userType is required (Mechanic or TowTruck)")
        private UserType userType;

        @NotBlank(message = "Street is required")
        private String street;

        private String gender;
        private Double latitude;
        private Double longitude;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public UserType getUserType() { return userType; }
        public void setUserType(UserType userType) { this.userType = userType; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getSpeciality() { return speciality; }
        public void setSpeciality(String speciality) { this.speciality = speciality; }
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }
    private Double latitude;
    private Double longitude;

    // ... existing getters ...

    public Double getLatitude() { return latitude; }
    public void setLatitude(Object latitude) {
        if (latitude instanceof Number) {
            this.latitude = ((Number) latitude).doubleValue();
        } else if (latitude instanceof String) {
            String str = ((String) latitude).trim();
            this.latitude = str.isEmpty() ? null : Double.valueOf(str);
        } else {
            this.latitude = null;
        }
    }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Object longitude) {
        if (longitude instanceof Number) {
            this.longitude = ((Number) longitude).doubleValue();
        } else if (longitude instanceof String) {
            String str = ((String) longitude).trim();
            this.longitude = str.isEmpty() ? null : Double.valueOf(str);
        } else {
            this.longitude = null;
        }
    }
    public static class LoginInput {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordInput {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Old password is required")
        private String oldPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}