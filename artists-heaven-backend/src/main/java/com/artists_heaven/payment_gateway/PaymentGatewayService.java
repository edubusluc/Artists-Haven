package com.artists_heaven.payment_gateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderItemRepository;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.shopping_cart.CartItemDTO;
import com.artists_heaven.shopping_cart.ShoppingCartService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class PaymentGatewayService {

    Dotenv dotenv = Dotenv.load();
    private String endpointSecret = dotenv.get("STRIPE_KEY");
    private String webhookSecret = dotenv.get("STRIPE_WEBHOOK");

    private final UserService userService;

    private final OrderRepository orderRepository;

    private final ProductService productService;

    private final OrderItemRepository orderItemRepository;

    private final ShoppingCartService shoppingCartService;

    private final EmailSenderService emailSenderService;

    private static final String EVENT_TYPE = "checkout.session.completed";

    public PaymentGatewayService(UserService userService, OrderRepository orderRepository,
            ProductService productService, OrderItemRepository orderItemRepository,
            ShoppingCartService shoppingCartService,
            EmailSenderService emailSenderService) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderItemRepository = orderItemRepository;
        this.emailSenderService = emailSenderService;
        this.shoppingCartService = shoppingCartService;
    }

    public String checkoutProducts(List<CartItemDTO> items, Long id) {
        Stripe.apiKey = endpointSecret;

        Boolean productAvailable = checkProductAvailable(items);
        if(!productAvailable){
            return "No se ha completado el pago: Producto no disponible";
        }

        return processPaymentSession(items, id);

    }

    private String processPaymentSession(List<CartItemDTO> items, Long id) {
        try {
            List<SessionCreateParams.LineItem> lineItems = buildLineItems(items);
            Map<String, String> metadata = buildMetadata(items, id);
            SessionCreateParams.Builder params = buildSessionParams(lineItems, metadata);
            
            addUserDetails(params);
            
            // Crear sesión en Stripe
            Session session = Session.create(params.build());
            return session.getUrl();
        } catch (StripeException e) {
            return "No se ha completado el pago: " + e.getMessage();
        }
    }



    public List<SessionCreateParams.LineItem> buildLineItems(List<CartItemDTO> items) {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (CartItemDTO item : items) {
            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                    .builder()
                    .setName("Artists Heaven - " + item.getProduct().getName() + " - Size: " + item.getSize())
                    .build();

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("EUR")
                    .setUnitAmount(item.getProduct().getPrice().longValue() * 100)
                    .setProductData(productData)
                    .build();

            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setQuantity(item.getQuantity().longValue())
                    .setPriceData(priceData)
                    .build());
        }
        return lineItems;
    }

    private Map<String, String> buildMetadata(List<CartItemDTO> items, Long id) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", id != null ? id.toString() : "Anonymous");

        for (CartItemDTO item : items) {
            String key = "product_" + item.getProduct().getId();
            String productMetadata = item.getQuantity() + "|" + item.getSize();
            metadata.merge(key, productMetadata, (oldValue, newValue) -> oldValue + "," + newValue);
        }
        return metadata;
    }

    private SessionCreateParams.Builder buildSessionParams(List<SessionCreateParams.LineItem> lineItems,
            Map<String, String> metadata) {
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/success")
                .setCancelUrl("http://localhost:3000/cancel")
                .addAllLineItem(lineItems);

        metadata.forEach(params::putMetadata);
        return params;
    }

    private void addUserDetails(SessionCreateParams.Builder params) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            params.setCustomerEmail(user.getEmail());
            addUserShippingAddress(params, user);
        } else {
            params.setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US)
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.ES)
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.FR)
                    .build());
        }
    }

    private void addUserShippingAddress(SessionCreateParams.Builder params, User user) {
        if (user.getAddress() != null) {
            Map<String, String[]> customFieldsData = Map.of(
                    "city", new String[] { "Ciudad", user.getCity() },
                    "shipping_address", new String[] { "Dirección de Envío", user.getAddress() },
                    "postal_code", new String[] { "Código Postal", user.getPostalCode() });

            customFieldsData.forEach((key, value) -> params.addCustomField(createCustomField(key, value[0], value[1])));
        }
    }

    private SessionCreateParams.CustomField createCustomField(String key, String label, String defaultValue) {
        return SessionCreateParams.CustomField.builder()
                .setKey(key)
                .setLabel(
                        SessionCreateParams.CustomField.Label.builder()
                                .setType(SessionCreateParams.CustomField.Label.Type.CUSTOM)
                                .setCustom(label)
                                .build())
                .setType(SessionCreateParams.CustomField.Type.TEXT)
                .setText(
                        SessionCreateParams.CustomField.Text.builder()
                                .setDefaultValue(defaultValue)
                                .build())
                .build();
    }

    public void processStripeEvent(String payload, String sigHeader) {
        Event event = verifySignature(payload, sigHeader);

        if (EVENT_TYPE.equals(event.getType())) {
            Session session = getSession(event);
            if (session == null)
                return;

            Long userId = getUserId(session);
            User user = userId == null ? null : userService.getUserById(userId);
            String email = (user != null) ? user.getEmail() : session.getCustomerDetails().getEmail();

            Order order = createOrder(session, userId, user);
            createOrderItems(session, order, userId);

            handlePostOrderActions(userId, email);
        }
    }

    private Event verifySignature(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Error de verificación de firma: " + e.getMessage());
        }
    }

    private Session getSession(Event event) {
        return (Session) event.getDataObjectDeserializer().getObject().orElse(null);
    }

    private Long getUserId(Session session) {
        String userIdStr = session.getMetadata().get("userId");
        return "Anonymous".equals(userIdStr) ? null : Long.parseLong(userIdStr);
    }

    private Order createOrder(Session session, Long userId, User user) {
        Order order = new Order();
        Long amount = session.getAmountTotal() / 100;

        order.setTotalPrice(amount.floatValue());
        order.setUserId(userId);
        if (user != null) {
            order.setCountry(user.getCountry());
        }
        return orderRepository.save(order);
    }

    private void createOrderItems(Session session, Order order, Long userId) {
        List<OrderItem> items = new ArrayList<>();
        List<String> fields = session.getCustomFields().stream()
                .map(field -> field.getText().getDefaultValue())
                .collect(Collectors.toList());

        String city = userId != null ? fields.get(0) : session.getCustomerDetails().getAddress().getCity();
        String addressLine1 = userId != null ? fields.get(1) : session.getCustomerDetails().getAddress().getLine1();
        String addressLine2 = userId != null ? null : session.getCustomerDetails().getAddress().getLine2();
        String postalCode = userId != null ? fields.get(2) : session.getCustomerDetails().getAddress().getPostalCode();

        for (Map.Entry<String, String> entry : session.getMetadata().entrySet()) {
            if (entry.getKey().startsWith("product_")) {
                processProductEntry(entry.getKey(), entry.getValue(), order, items);
            }
        }

        finalizeOrder(order, items, city, addressLine1, addressLine2, postalCode);
    }

    private void processProductEntry(String key, String value, Order order, List<OrderItem> items) {
        Long productId = Long.parseLong(key.replace("product_", ""));
        Product product = productService.findById(productId);

        for (String entry : value.split(",")) {
            String[] values = entry.split("\\|");
            int quantity = Integer.parseInt(values[0]);
            String size = values[1];

            product.getSize().computeIfPresent(size, (k, v) -> v - quantity);
            OrderItem item = new OrderItem(productId, quantity, size, order);
            orderItemRepository.save(item);
            items.add(item);
        }
    }

    private void finalizeOrder(Order order, List<OrderItem> items, String city, String addressLine1,
            String addressLine2, String postalCode) {
        order.setIdentifier(UUID.randomUUID().getMostSignificantBits());
        order.setStatus(OrderStatus.PAID);
        order.setItems(items);
        order.setCity(city);
        order.setAddressLine1(addressLine1);
        order.setAddressLine2(addressLine2);
        order.setPostalCode(postalCode);
        orderRepository.save(order);
    }

    private void handlePostOrderActions(Long userId, String email) {
        if (userId != null) {
            shoppingCartService.deleteShoppingCartUserItems(userId);
        }
        emailSenderService.sendPurchaseConfirmationEmail(email);
    }

    public Boolean checkProductAvailable(List<CartItemDTO> items) {
        for (CartItemDTO i : items) {
            Product product = productService.findById(i.getProduct().getId());
            if (product.getSize().get(i.getSize()) < i.getQuantity()) {
                return false;
            }
            if (product.getAvailable() == false) {
                return false;
            }
        }
        return true;
                
    }

}
