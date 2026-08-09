package com.nightflow.orderservice.service;

import com.nightflow.orderservice.client.TicketServiceClient;
import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.entity.OrderItem;
import com.nightflow.orderservice.entity.OrderStatus;
import com.nightflow.orderservice.producer.OrderProducer;
import com.nightflow.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The payment saga of {@link OrderService#payOrder(String)}.
 *
 * These are the branches the live failure injection exercises against the
 * running stack; keeping them here means a regression shows up in `mvn test`
 * instead of only in a manual run.
 */
class OrderPaymentSagaTest {

    private static final long ORDER_ID = 7L;
    private static final String ORDER_NUMBER = "order-7";

    private OrderRepository orderRepository;
    private TicketServiceClient ticketServiceClient;
    private OrderProducer orderProducer;
    private OrderStateWriter orderStateWriter;
    private OrderService orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        ticketServiceClient = mock(TicketServiceClient.class);
        orderProducer = mock(OrderProducer.class);
        orderStateWriter = mock(OrderStateWriter.class);
        orderService = new OrderService(orderRepository, ticketServiceClient, orderProducer, orderStateWriter);

        order = Order.builder()
                .id(ORDER_ID)
                .orderNumber(ORDER_NUMBER)
                .userId("2")
                .totalAmount(new BigDecimal("200.00"))
                .status(OrderStatus.PENDING)
                .items(List.of(
                        item(1L, "res-a"),
                        item(2L, "res-b")))
                .build();

        when(orderStateWriter.loadWithItems(ORDER_NUMBER)).thenReturn(order);
        when(orderStateWriter.markCompleted(eq(ORDER_ID), anyString())).thenAnswer(invocation -> {
            order.setStatus(OrderStatus.COMPLETED);
            order.setPaymentReference(invocation.getArgument(1));
            return order;
        });
        when(orderStateWriter.markFailed(eq(ORDER_ID), any(), any(), any())).thenAnswer(invocation -> {
            order.setStatus(invocation.getArgument(1));
            order.setPaymentReference(invocation.getArgument(2));
            order.setFailureReason(invocation.getArgument(3));
            return order;
        });
    }

    @Test
    @DisplayName("happy path: every item confirmed, order COMPLETED, event published")
    void confirmsEveryItemAndCompletes() {
        Order result = orderService.payOrder(ORDER_NUMBER);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(ticketServiceClient).confirmSale("res-a", ORDER_ID, 2L);
        verify(ticketServiceClient).confirmSale("res-b", ORDER_ID, 2L);
        verify(orderProducer).sendOrderCreatedEvent(any());
        verify(ticketServiceClient, never()).releaseSale(anyLong());
        verify(ticketServiceClient, never()).cancelReservation(anyString());
    }

    @Test
    @DisplayName("partial failure: the confirmed item is released, the order is FAILED, no event")
    void compensatesWhenASecondItemCannotBeConfirmed() {
        doThrow(new RuntimeException("[404] Rezervasyon bulunamadi"))
                .when(ticketServiceClient).confirmSale(eq("res-b"), anyLong(), anyLong());

        assertThrows(com.nightflow.orderservice.exception.OrderSagaFailedException.class,
                () -> orderService.payOrder(ORDER_NUMBER));

        // Undo the sale that item 1 already got, and drop any reservation still held.
        verify(ticketServiceClient).releaseSale(ORDER_ID);
        verify(ticketServiceClient).cancelReservation("res-a");
        verify(ticketServiceClient).cancelReservation("res-b");

        ArgumentCaptor<OrderStatus> status = ArgumentCaptor.forClass(OrderStatus.class);
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(orderStateWriter).markFailed(eq(ORDER_ID), status.capture(), anyString(), reason.capture());

        assertEquals(OrderStatus.FAILED, status.getValue());
        assertTrue(reason.getValue().contains("refunded"), reason.getValue());

        verify(orderStateWriter, never()).markCompleted(anyLong(), anyString());
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("ticket-service unreachable: compensation cannot run either, order is COMPENSATION_FAILED")
    void recordsCompensationFailureWhenTheRollbackAlsoFails() {
        doThrow(new RuntimeException("Connection refused"))
                .when(ticketServiceClient).confirmSale(anyString(), anyLong(), anyLong());
        doThrow(new RuntimeException("Connection refused"))
                .when(ticketServiceClient).releaseSale(anyLong());
        doThrow(new RuntimeException("Connection refused"))
                .when(ticketServiceClient).cancelReservation(anyString());

        assertThrows(com.nightflow.orderservice.exception.OrderSagaFailedException.class,
                () -> orderService.payOrder(ORDER_NUMBER));

        ArgumentCaptor<OrderStatus> status = ArgumentCaptor.forClass(OrderStatus.class);
        verify(orderStateWriter).markFailed(eq(ORDER_ID), status.capture(), anyString(), anyString());

        assertEquals(OrderStatus.COMPENSATION_FAILED, status.getValue());
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("an order that is not PENDING is never paid twice")
    void rejectsAnOrderThatIsNotPending() {
        order.setStatus(OrderStatus.COMPLETED);

        assertThrows(IllegalStateException.class, () -> orderService.payOrder(ORDER_NUMBER));

        verify(ticketServiceClient, never()).confirmSale(anyString(), anyLong(), anyLong());
        verify(orderProducer, never()).sendOrderCreatedEvent(any());
    }

    private static OrderItem item(Long id, String reservationId) {
        return OrderItem.builder()
                .id(id)
                .categoryId(id)
                .categoryName("cat-" + id)
                .eventId("event-1")
                .eventName("event")
                .quantity(1)
                .price(new BigDecimal("100.00"))
                .reservationId(reservationId)
                .build();
    }
}
