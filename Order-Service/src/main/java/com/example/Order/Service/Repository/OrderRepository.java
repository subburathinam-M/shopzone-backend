package com.example.Order.Service.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Order.Service.entity.Order;

public interface OrderRepository extends JpaRepository<Order,Long>{

      @Query("SELECT o FROM Order o WHERE o.deleted = false")
      List<Order> findAllActiveOrders();

  // 👇 Updated to use keycloakId instead of userId
  @Query("SELECT o FROM Order o WHERE o.userKeycloakId = :keycloakId AND o.deleted = false")
  List<Order> findActiveOrdersByKeycloakId(@Param("keycloakId") String keycloakId);
  List<Order> findByUserKeycloakId(String keycloakId);


// @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.deleted = false")
// List<Order> findActiveOrdersByUserId(@Param("userId") Long userId);

    // List<Order> findByUserId(Long userId);


}
