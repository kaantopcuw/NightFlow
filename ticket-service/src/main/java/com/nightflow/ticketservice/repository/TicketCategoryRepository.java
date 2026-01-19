package com.nightflow.ticketservice.repository;

import com.nightflow.ticketservice.entity.CategoryStatus;
import com.nightflow.ticketservice.entity.TicketCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Long> {

    List<TicketCategory> findByEventId(String eventId);

    List<TicketCategory> findByEventIdAndStatus(String eventId, CategoryStatus status);

    /**
     * Pessimistic lock ile kategori getir - envanter güncellemesi için
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tc FROM TicketCategory tc WHERE tc.id = :id")
    Optional<TicketCategory> findByIdWithLock(@Param("id") Long id);

    /**
     * Rezerve miktarını artır
     */
    @Modifying
    @Query("UPDATE TicketCategory tc SET tc.reservedQuantity = tc.reservedQuantity + :quantity, tc.updatedAt = CURRENT_TIMESTAMP WHERE tc.id = :id")
    int incrementReserved(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Rezerve miktarını azalt
     */
    @Modifying
    @Query("UPDATE TicketCategory tc SET tc.reservedQuantity = tc.reservedQuantity - :quantity, tc.updatedAt = CURRENT_TIMESTAMP WHERE tc.id = :id AND tc.reservedQuantity >= :quantity")
    int decrementReserved(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Satış işlemi: reserved -> sold
     */
    @Modifying
    @Query("UPDATE TicketCategory tc SET tc.reservedQuantity = tc.reservedQuantity - :quantity, tc.soldQuantity = tc.soldQuantity + :quantity, tc.updatedAt = CURRENT_TIMESTAMP WHERE tc.id = :id AND tc.reservedQuantity >= :quantity")
    int confirmSale(@Param("id") Long id, @Param("quantity") int quantity);
}
