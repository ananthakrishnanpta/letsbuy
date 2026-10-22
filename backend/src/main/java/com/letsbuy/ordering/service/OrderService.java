package com.letsbuy.ordering.service;

import com.letsbuy.cart.api.CartLine;
import com.letsbuy.cart.api.CartPort;
import com.letsbuy.catalog.api.InventoryPort;
import com.letsbuy.catalog.api.ProductSnapshot;
import com.letsbuy.common.api.PageResponse;
import com.letsbuy.common.exception.ConflictException;
import com.letsbuy.common.exception.NotFoundException;
import com.letsbuy.ordering.domain.Address;
import com.letsbuy.ordering.domain.CustomerOrder;
import com.letsbuy.ordering.domain.OrderItem;
import com.letsbuy.ordering.domain.OrderStatus;
import com.letsbuy.ordering.domain.Payment;
import com.letsbuy.ordering.domain.PaymentMethod;
import com.letsbuy.ordering.domain.PaymentStatus;
import com.letsbuy.ordering.domain.ShippingPolicy;
import com.letsbuy.ordering.persistence.OrderRepository;
import com.letsbuy.ordering.persistence.PaymentRepository;
import com.letsbuy.ordering.web.dto.OrderResponse;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final AddressService addresses;
    private final CartPort cart;
    private final InventoryPort inventory;

    public OrderService(
            OrderRepository orders,
            PaymentRepository payments,
            AddressService addresses,
            CartPort cart,
            InventoryPort inventory
    ) {
        this.orders = orders;
        this.payments = payments;
        this.addresses = addresses;
        this.cart = cart;
        this.inventory = inventory;
    }

    @Transactional
    public OrderResponse checkout(UUID userId, UUID addressId, String paymentMethod) {
        PaymentMethod method = parseMethod(paymentMethod);
        List<CartLine> lines = cart.lines(userId);
        if (lines.isEmpty()) {
            throw new ConflictException("Your cart is empty.");
        }
        Address address = addresses.requireOwned(userId, addressId);
        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setOrderNumber(nextOrderNumber());
        order.setStatus(OrderStatus.PLACED);
        order.setCurrency("INR");
        copyAddress(order, address);
        long subtotal = 0;
        for (CartLine line : lines) {
            ProductSnapshot product = inventory.reserve(line.productId(), line.quantity());
            if (!"INR".equals(product.currency())) {
                throw new ConflictException("Only INR products can be checked out.");
            }
            long lineTotal = Math.multiplyExact(product.priceMinor(), line.quantity());
            subtotal = Math.addExact(subtotal, lineTotal);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.id());
            item.setSku(product.sku());
            item.setProductName(product.name());
            item.setUnitPriceMinor(product.priceMinor());
            item.setQuantity(line.quantity());
            item.setLineTotalMinor(lineTotal);
            order.getItems().add(item);
        }
        long shipping = ShippingPolicy.quote(subtotal);
        order.setSubtotalMinor(subtotal);
        order.setShippingMinor(shipping);
        order.setTotalMinor(Math.addExact(subtotal, shipping));
        orders.save(order);

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmountMinor(order.getTotalMinor());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProvider("NONE");
        payments.save(payment);
        cart.clear(userId);
        return OrderResponse.detail(order, payment.getMethod().name(), payment.getStatus().name());
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> list(UUID userId, int page, int size) {
        var result = orders.findByUserId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<OrderResponse> items = result.getContent().stream()
                .map(order -> {
                    Payment payment = payments.findByOrderId(order.getId()).orElse(null);
                    return OrderResponse.summary(
                            order,
                            payment == null ? null : payment.getMethod().name(),
                            payment == null ? null : payment.getStatus().name()
                    );
                })
                .toList();
        return new PageResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.hasNext());
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID userId, UUID orderId) {
        CustomerOrder order = orders.findWithItemsByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        Payment payment = payments.findByOrderId(order.getId()).orElse(null);
        return OrderResponse.detail(
                order,
                payment == null ? null : payment.getMethod().name(),
                payment == null ? null : payment.getStatus().name()
        );
    }

    @Transactional
    public OrderResponse cancel(UUID userId, UUID orderId) {
        CustomerOrder order = orders.findWithItemsByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NotFoundException("Order not found."));
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new ConflictException("Only placed orders can be cancelled.");
        }
        for (OrderItem item : order.getItems()) {
            inventory.restock(item.getProductId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        Payment payment = payments.findByOrderId(order.getId()).orElseThrow(() -> new NotFoundException("Payment not found."));
        payment.setStatus(PaymentStatus.VOIDED);
        return OrderResponse.detail(order, payment.getMethod().name(), payment.getStatus().name());
    }

    @Transactional
    public OrderResponse fulfill(UUID orderId) {
        CustomerOrder order = orders.findWithItemsById(orderId).orElseThrow(() -> new NotFoundException("Order not found."));
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new ConflictException("Only placed orders can be fulfilled.");
        }
        order.setStatus(OrderStatus.FULFILLED);
        Payment payment = payments.findByOrderId(order.getId()).orElseThrow(() -> new NotFoundException("Payment not found."));
        if (payment.getMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.CAPTURED);
        }
        return OrderResponse.detail(order, payment.getMethod().name(), payment.getStatus().name());
    }

    private String nextOrderNumber() {
        String day = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "LB" + day + "-" + randomSuffix(6);
            if (!orders.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new ConflictException("Could not allocate an order number.");
    }

    private static String randomSuffix(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
        }
        return new String(chars);
    }

    private static PaymentMethod parseMethod(String value) {
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new ConflictException("Payment method must be COD.");
        }
    }

    private static void copyAddress(CustomerOrder order, Address address) {
        order.setShippingFullName(address.getFullName());
        order.setShippingLine1(address.getLine1());
        order.setShippingLine2(address.getLine2());
        order.setShippingCity(address.getCity());
        order.setShippingRegion(address.getRegion());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountryCode(address.getCountryCode());
        order.setShippingPhone(address.getPhone());
    }
}
