package com.cafetron.auth;

import com.cafetron.auth.dto.AuthResponse;
import com.cafetron.auth.dto.LoginRequest;
import com.cafetron.auth.dto.RegisterRequest;
import com.cafetron.security.JwtUtil;
import com.cafetron.security.UserDetailsServiceImpl;
import com.cafetron.security.UserPrincipal;
import com.cafetron.user.User;
import com.cafetron.user.repository.UserRepository;
import com.cafetron.vendor.entity.Vendor;
import com.cafetron.vendor.repository.VendorRepository;
import com.cafetron.wallet.entity.Transaction;
import com.cafetron.wallet.entity.TransactionType;
import com.cafetron.wallet.entity.Wallet;
import com.cafetron.wallet.repository.TransactionRepository;
import com.cafetron.wallet.repository.WalletRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final BigDecimal INITIAL_WALLET_BALANCE = new BigDecimal("1500.00");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final VendorRepository vendorRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           UserDetailsServiceImpl userDetailsService,
                           WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           VendorRepository vendorRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException(
                    "Email already in use: " + request.getEmail());
        }

        if (userRepository.findByEmployeeId(
                request.getEmployeeId()).isPresent()) {
            throw new IllegalStateException(
                    "Employee ID already in use: " + request.getEmployeeId());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRole(normalizeRole(request.getRole()));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        createVendorProfileIfNeeded(user);

        // Auto-create wallet for newly registered users so order placement can debit immediately.
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(INITIAL_WALLET_BALANCE);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction topUp = new Transaction();
        topUp.setWallet(wallet);
        topUp.setAmount(INITIAL_WALLET_BALANCE);
        topUp.setType(TransactionType.TOP_UP);
        topUp.setDescription("Initial wallet credit on registration");
        transactionRepository.save(topUp);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                principal.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // Step 1 — find user by employeeId first
        UserPrincipal principal = (UserPrincipal)
                userDetailsService.loadUserByEmployeeId(
                        request.getEmployeeId());

        // Step 2 — authenticate using user.id + password internally
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(principal.getId()),  // ← user.id
                        request.getPassword()
                )
        );

        // Step 3 — generate token with user.id as subject
        User user = principal.getUser();
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                principal.getRole()
        );
    }

    private void createVendorProfileIfNeeded(User user) {
        if (!"VENDOR".equals(normalizeRole(user.getRole())) || vendorRepository.existsByEmail(user.getEmail())) {
            return;
        }

        Vendor vendor = new Vendor();
        vendor.setName(user.getName());
        vendor.setEmail(user.getEmail());
        vendor.setContactPerson(user.getName());
        vendor.setActive(true);
        vendor.setCreatedAt(LocalDateTime.now());
        vendorRepository.save(vendor);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "EMPLOYEE";
        }

        String normalizedRole = role.trim().replaceFirst("^ROLE_", "").toUpperCase();
        return "COUNTER".equals(normalizedRole) ? "VENDOR" : normalizedRole;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        }
    }
}
