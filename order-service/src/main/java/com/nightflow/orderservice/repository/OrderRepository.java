package com.nightflow.orderservice.repository;

import com.nightflow.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(String userId);

    /**
     * Items are LAZY, and the payment saga reads them outside of any
     * transaction - it must not hold one open across the calls to
     * ticket-service. Fetching them up front is what makes that safe instead of
     * leaning on open-session-in-view.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithItems(@Param("orderNumber") String orderNumber);
}
