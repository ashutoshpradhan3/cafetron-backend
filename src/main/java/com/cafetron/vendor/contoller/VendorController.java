package com.cafetron.vendor.contoller;

import com.cafetron.vendor.dto.VendorRequest;
import com.cafetron.vendor.dto.VendorResponse;
import com.cafetron.vendor.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    @RestController
    @RequestMapping("/vendors")
    public class VendorController {

        private final VendorService vendorService;

        public VendorController(VendorService vendorService) {
            this.vendorService = vendorService;
        }

        @PostMapping                                       // POST /vendors
        public VendorResponse create(@Valid @RequestBody VendorRequest request) {
            return vendorService.create(request);
        }

        @GetMapping                                        // GET /vendors (all)
        public List<VendorResponse> getAll() {
            return vendorService.getAll();
        }

        @GetMapping("/active")                             // GET /vendors/active
        public List<VendorResponse> getActive() {
            return vendorService.getActive();
        }

        @GetMapping("/{id}")                               // GET /vendors/5
        public VendorResponse getById(@PathVariable Long id) {
            return vendorService.getById(id);
        }

        @PutMapping("/{id}")                               // PUT /vendors/5
        public VendorResponse update(@PathVariable Long id,
                                     @Valid @RequestBody VendorRequest request) {
            return vendorService.update(id, request);
        }

        @PatchMapping("/{id}/active")                      // PATCH /vendors/5/active?active=false
        public VendorResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
            return vendorService.setActive(id, active);
        }

        @DeleteMapping("/{id}")                            // DELETE /vendors/5
        public void delete(@PathVariable Long id) {
            vendorService.delete(id);
        }
}
