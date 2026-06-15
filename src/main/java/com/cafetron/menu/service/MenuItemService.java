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

    public MenuItemService(MenuItemRepository menuItemRepository, VendorRepository vendorRepository) {
        this.menuItemRepository = menuItemRepository;
        this.vendorRepository = vendorRepository;
    }

    public MenuItemResponse create(MenuItemRequest request) {
        Vendor vendor = vendorRepository.findById(request.vendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        MenuItem item = new MenuItem();
        item.setItemName(request.itemName());
        item.setPrice(request.price());
        item.setStock(request.stock());
        item.setFoodType(request.foodType());
        item.setVendor(vendor);
        item.setAvailable(request.stock() > 0);

        MenuItem saved = menuItemRepository.save(item);
        return toResponse(saved);
    }

    public MenuItemResponse getById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return toResponse(item);
    }

    public List<MenuItemResponse> getAll() {
        return menuItemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
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

    public void delete(Long id) {
        menuItemRepository.deleteById(id);
    }

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
