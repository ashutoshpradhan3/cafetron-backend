package com.cafetron.menu.service;

import com.cafetron.menu.dto.MenuItemRequest;
import com.cafetron.menu.dto.MenuItemResponse;
import com.cafetron.menu.entity.MenuItem;
import com.cafetron.vendor.entity.Vendor;
import com.cafetron.menu.repository.MenuItemRepository;
import com.cafetron.menu.repository.VendorRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final VendorRepository vendorRepository;

    // Constructor injection
    public MenuItemService(MenuItemRepository menuItemRepository, VendorRepository vendorRepository) {
        this.menuItemRepository = menuItemRepository;
        this.vendorRepository = vendorRepository;
    }

    // CREATE a new menu item from the incoming request.
    public MenuItemResponse create(MenuItemRequest request) {

        // Find the vendor named in the request (error if missing).
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // Build a new item and copy in the request's values.
        MenuItem item = new MenuItem();
        item.setItemName(request.itemName());
        item.setPrice(request.price());
        item.setStock(request.stock());
        item.setFoodType(request.foodType());
        item.setVendor(vendor);
        item.setAvailable(request.stock() > 0);

        // Save (INSERT) and return as a response.
        MenuItem saved = menuItemRepository.save(item);
        return toResponse(saved);
    }

    // READ one item by id.
    public MenuItemResponse getById(Long menuItemId) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return toResponse(item);
    }

    // READ every item (staff/admin view — includes hidden/out-of-stock ones).
    public List<MenuItemResponse> getAll() {
        return menuItemRepository.findAll()
                .stream() // go through each entity...
                .map(this::toResponse) // ...convert each to a response...
                .toList(); // ...collect into a list.
    }

    // UPDATE an existing item
    public MenuItemResponse update(Long menuItemId, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        item.setItemName(request.itemName());
        item.setPrice(request.price());
        item.setStock(request.stock());
        item.setFoodType(request.foodType());
        item.setVendor(vendor);
        item.setAvailable(request.stock() > 0);

        return toResponse(menuItemRepository.save(item));
    }

    // DELETE an item by id.
    public void delete(Long menuItemId) {
        menuItemRepository.deleteById(menuItemId);
    }

    // --- Employee-facing reads ---

    // Today's menu: available items from active vendors.
    public List<MenuItemResponse> getTodaysMenu() {
        return menuItemRepository.findTodaysMenu()
                .stream().map(this::toResponse).toList();
    }

    // Search today's menu by name.
    public List<MenuItemResponse> search(String name) {
        return menuItemRepository.searchTodaysMenu(name)
                .stream().map(this::toResponse).toList();
    }

    // Filter today's menu by dietary type.
    public List<MenuItemResponse> filterByFoodType(String foodType) {
        return menuItemRepository.filterByFoodType(foodType)
                .stream().map(this::toResponse).toList();
    }

    // --- Stock operations (with the auto-unavailable rule) ---

    // Set an item's stock (e.g. the morning restock).
    public MenuItemResponse setStock(Long menuItemId, int newStock) {
        if (newStock < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        item.setStock(newStock);
        item.setAvailable(newStock > 0); // auto rule: 0 -> hidden, else shown
        return toResponse(menuItemRepository.save(item));
    }

    // Manually show/hide an item (override). Can't enable a zero-stock item.
    public MenuItemResponse setAvailability(Long menuItemId, boolean available) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        if (available && item.getStock() == 0) {
            throw new RuntimeException("Cannot make an item available with zero stock");
        }
        item.setAvailable(available);
        return toResponse(menuItemRepository.save(item));
    }

    // Reduce stock when an order is placed.
    public MenuItemResponse decreaseStock(Long menuItemId, int quantity) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        if (item.getStock() < quantity) {
            throw new RuntimeException("Not enough stock");
        }
        item.setStock(item.getStock() - quantity);
        item.setAvailable(item.getStock() > 0); // hide automatically if it hit 0
        return toResponse(menuItemRepository.save(item));
    }

    // Converts a database entity into the response DTO sent to the frontend.
    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getItemName(),
                item.getPrice(),
                item.getStock(),
                item.getFoodType(),
                item.isAvailable(),
                item.getVendor().getId(),
                item.getVendor().getName()
        );
    }
}
