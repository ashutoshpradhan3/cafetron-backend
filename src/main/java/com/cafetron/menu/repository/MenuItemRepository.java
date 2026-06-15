package com.cafetron.menu.repository;

import com.cafetron.menu.entity.MenuItem;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

//Inherits basic CRUD from JpaRepository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByIsAvailableTrue();
    List<MenuItem> findByItemNameContainingIgnoreCase(String name);
    List<MenuItem> findByFoodType(String foodType);
    List<MenuItem> findByVendorId(Long vendorId);  // All items for one vendor

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MenuItem m where m.id = :id")
<<<<<<< HEAD
    MenuItem findByIdForUpdate(@NotNull Long id);

    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.vendor.isActive = true")
    List<MenuItem> findTodaysMenu(); // Today's menu: available items from active vendors.

    // Today's menu, matching a name search.
    // CONCAT adds % wildcards; LOWER makes it case-insensitive.
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.vendor.isActive = true " +
            "AND LOWER(m.itemName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<MenuItem> searchTodaysMenu(@Param("name") String name);

    // Today's menu filtered by dietary type.
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.vendor.isActive = true " +
            "AND m.foodType = :foodType")
    List<MenuItem> filterByFoodType(@Param("foodType") String foodType);
=======
    Optional<MenuItem> findByIdForUpdate(@NotNull Long id);
>>>>>>> a9668c5 (feat(order): implement placeOrder happy path with stock lock and wallet debit wiring)
}
