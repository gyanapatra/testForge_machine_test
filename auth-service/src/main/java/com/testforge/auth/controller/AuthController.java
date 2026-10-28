package com.testforge.auth.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.testforge.auth.entity.AppUser;
import com.testforge.auth.entity.PendingRegistration;
import com.testforge.auth.entity.RevokedToken;
import com.testforge.auth.repository.PendingRegistrationRepository;
import com.testforge.auth.repository.RevokedTokenRepository;
import com.testforge.auth.repository.UserRepository;
import com.testforge.auth.request.LoginRequest;
import com.testforge.auth.request.SignupRequest;
import com.testforge.auth.request.VerifyOtpRequest;
import com.testforge.auth.response.AuthResponse;
import com.testforge.auth.service.JwtService;
import com.testforge.auth.service.OtpService;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final int MAX_OTP_ATTEMPTS = 5;

    private final UserRepository users;
    private final RevokedTokenRepository revoked;
    private final PendingRegistrationRepository pendingRegistrations;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final OtpService otpService;
    private final Set<String> blockedCountries;
    

    public AuthController(
            UserRepository users,
            RevokedTokenRepository revoked,
            PendingRegistrationRepository pendingRegistrations,
            PasswordEncoder encoder,
            JwtService jwt,
            OtpService otpService,
            @Value("${app.blocked-countries}") String blocked
    ) {
        this.users = users;
        this.revoked = revoked;
        this.pendingRegistrations = pendingRegistrations;
        this.encoder = encoder;
        this.jwt = jwt;
        this.otpService = otpService;

        this.blockedCountries = Set.of(
                blocked.split(",")).stream()
                .map(String::trim)
                .map(country -> country.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    // 1. Signup: create a pending registration and generate an OTP.
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse signup(@Valid @RequestBody SignupRequest req) {

    	String countryCode = req.countryCode() == null
    	        ? ""
    	        : req.countryCode().trim().toUpperCase(Locale.ROOT);

    	if (!countryCode.matches("[A-Z]{2}")) {
    	    throw new ResponseStatusException(
    	            HttpStatus.BAD_REQUEST,
    	            "A valid two-letter country code is required"
    	    );
    	}

    	if (blockedCountries.contains(countryCode)) {
    	    throw new ResponseStatusException(
    	            HttpStatus.FORBIDDEN,
    	            "Sign-up is not available in your country"
    	    );
    	}

        String username = req.username().trim();
        String email = req.email().trim().toLowerCase(Locale.ROOT);

        if (users.existsByUsername(username)
                || users.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username or email already exists"
            );
        }

        // Prevent duplicate pending usernames belonging to another email.
        if (pendingRegistrations.existsByUsername(username)) {
            PendingRegistration existing =
                    pendingRegistrations.findByEmail(email).orElse(null);

            if (existing == null
                    || !existing.getUsername().equals(username)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username is already pending verification"
                );
            }
        }

        // Remove a previous pending registration for this email.
        pendingRegistrations.deleteByEmail(email);

        String otp = otpService.generateOtp();
        String otpHash = otpService.hashOtp(email, otp);

        PendingRegistration pending = new PendingRegistration(
                username,
                email,
                encoder.encode(req.password()),
                req.userType(),
                otpHash,
                otpService.getExpiryTime()
        );

        pendingRegistrations.save(pending);

        // Local machine-test mode: display the OTP in the Auth Service console.
        System.out.println(
                "[TESTFORGE DEMO OTP] Email: "
                        + email
                        + " | OTP: "
                        + otp
                        + " | Valid for 5 minutes"
        );

        return new AuthResponse(
                "OTP generated. Check the Auth Service console and verify your email within 5 minutes.",
                null,
                username,
                req.userType().name()
        );
    }

    // 2. Verify OTP: create the actual user only after successful verification.
    @PostMapping("/auth/verify-otp")
    public AuthResponse verifyOtp(
            @Valid @RequestBody VerifyOtpRequest req
    ) {
        String email = req.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        PendingRegistration pending = pendingRegistrations
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired OTP. Please sign up again."
                ));

        if (pending.getExpiresAt().isBefore(Instant.now())) {
            pendingRegistrations.deleteByEmail(email);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired. Please sign up again."
            );
        }

        if (pending.getAttempts() >= MAX_OTP_ATTEMPTS) {
            pendingRegistrations.deleteByEmail(email);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP attempts exceeded. Please sign up again."
            );
        }

        boolean validOtp = otpService.matches(
                email,
                req.otp(),
                pending.getOtpHash()
        );

        if (!validOtp) {
            pending.incrementAttempts();

            if (pending.getAttempts() >= MAX_OTP_ATTEMPTS) {
                pendingRegistrations.deleteByEmail(email);

                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Maximum OTP attempts exceeded. Please sign up again."
                );
            }

            pendingRegistrations.save(pending);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP. Please try again."
            );
        }

        // Recheck uniqueness before creating the account.
        if (users.existsByUsername(pending.getUsername())
                || users.existsByEmail(email)) {

            pendingRegistrations.deleteByEmail(email);

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username or email already exists"
            );
        }

        AppUser user = users.save(
                new AppUser(
                        pending.getUsername(),
                        email,
                        pending.getPasswordHash(),
                        pending.getRole()
                )
        );

        pendingRegistrations.deleteByEmail(email);

        return new AuthResponse(
                "Registration successful",
                null,
                user.getUsername(),
                user.getRole().name()
        );
    }

    // 3. Login: unchanged.
    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {

        AppUser user = users.findByUsername(req.username())
                .orElse(null);

        if (user == null
                || !encoder.matches(
                        req.password(),
                        user.getPasswordHash()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login Failed"
            );
        }

        return new AuthResponse(
                "Login successful",
                jwt.createToken(user),
                user.getUsername(),
                user.getRole().name()
        );
    }

    // 4. Logout: revoke the current JWT.
    @PostMapping("/auth/logout")
    public AuthResponse logout(HttpServletRequest request) {

        Claims claims = claimsFrom(request);

        if (!revoked.existsByJti(claims.getId())) {
            revoked.save(
                    new RevokedToken(
                            claims.getId(),
                            claims.getExpiration().toInstant()
                    )
            );
        }

        return new AuthResponse(
                claims.getSubject() + " Logged Out",
                null,
                claims.getSubject(),
                claims.get("role", String.class)
        );
    }

    // 5. Dashboard: authenticated users only.
    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        return "Welcome " + claimsFrom(request).getSubject();
    }

    // 6. Settings: administrators only.
    @GetMapping("/settings")
    public String settings(HttpServletRequest request) {

        Claims claims = claimsFrom(request);

        if (!"ADMINISTRATOR".equals(
                claims.get("role", String.class))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Administrator role required"
            );
        }

        return "Settings access granted";
    }

    private Claims claimsFrom(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Bearer token required"
            );
        }

        Claims claims;

        try {
            claims = jwt.parse(header.substring(7));
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }

        if (revoked.existsByJti(claims.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token has been logged out"
            );
        }

        return claims;
    }
}