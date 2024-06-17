package org.example.orderservice.controller;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.OrderDto;
import org.example.orderservice.entity.OrderEntity;
import org.example.orderservice.service.OrderService;
import org.example.orderservice.vo.RequestOrder;
import org.example.orderservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order-service")
public class OrderController {
    private final Environment environment;
    private final OrderService orderService;

    @GetMapping("/health_check")
    public String status() {
        log.info("OrderController.status");
        return String.format("Working in [ORDER-SERVICE] PORT ON %s", environment.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder order) {
        log.info("OrderController.createOrder");
        log.info("order: {}", order);
        log.info("userId: {}", userId);

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        OrderDto orderDto = mapper.map(order, OrderDto.class);
        orderDto.setUserId(userId);

        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<?>getOrders(@PathVariable("userId") String userId) {
        log.info("OrderController.getOrders");
        Iterable<OrderEntity> orders = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> responseOrders = new ArrayList<>();
        orders.forEach(v -> {
            responseOrders.add(new ModelMapper().map(v, ResponseOrder.class));
        });
        return ResponseEntity.status(HttpStatus.OK).body(responseOrders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?>getOrder(@PathVariable("orderId") String orderId) {
        log.info("OrderController.getOrder");
        OrderDto order = orderService.getOrderByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(order, ResponseOrder.class));
    }
}
