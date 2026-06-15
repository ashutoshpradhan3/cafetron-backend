package com.cafetron.vendor.service;

import com.cafetron.menu.repository.VendorRepository;

import com.cafetron.vendor.dto.VendorRequest;
import com.cafetron.vendor.dto.VendorResponse;
import com.cafetron.vendor.entity.Vendor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    // CREATE a vendor.
    public VendorResponse create(VendorRequest request) {
        Vendor vendor = new Vendor();
        vendor.setName(request.name());
        vendor.setEmail(request.email());
        vendor.setPhone(request.phone());
        vendor.setContactPerson(request.contactPerson());
        vendor.setActive(true);
        vendor.setCreatedAt(LocalDateTime.now());
        return toResponse(vendorRepository.save(vendor));
    }

    // READ one vendor.
    public VendorResponse getById(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        return toResponse(vendor);
    }

    // READ all vendors (admin view).
    public List<VendorResponse> getAll() {
        return vendorRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    // READ only active vendors (handy when building the menu).
    public List<VendorResponse> getActive() {
        return vendorRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).toList();
    }

    // UPDATE a vendor's details (not its active state).
    public VendorResponse update(Long vendorId, VendorRequest request) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setName(request.name());
        vendor.setEmail(request.email());
        vendor.setPhone(request.phone());
        vendor.setContactPerson(request.contactPerson());
        return toResponse(vendorRepository.save(vendor));
    }

    // Activate / deactivate a vendor (the on-off switch).
    public VendorResponse setActive(Long vendorId, boolean active) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        vendor.setActive(active);
        return toResponse(vendorRepository.save(vendor));
    }

    // DELETE a vendor — note: fails if the vendor still has menu items (the FK).
    // Prefer deactivating instead of deleting.
    public void delete(Long id) {
        vendorRepository.deleteById(id);
    }

    private VendorResponse toResponse(Vendor v) {
        return new VendorResponse(
                v.getId(), v.getName(), v.getEmail(), v.getPhone(),
                v.getContactPerson(), v.isActive(), v.getCreatedAt()
        );
    }
}
