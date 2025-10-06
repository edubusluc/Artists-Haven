package com.artists_heaven.payment_gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.email.EmailSenderService;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserService;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderRepository;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductColor;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.product.Section;
import com.artists_heaven.rewardCard.RewardCardRepository;
import com.artists_heaven.shopping_cart.CartItemDTO;
import com.artists_heaven.shopping_cart.ShoppingCartService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.annotation.Nullable;

@Service
public class PaymentGatewayService {

    Dotenv dotenv = Dotenv.load();
    private String endpointSecret = dotenv.get("STRIPE_KEY");
    private String webhookSecret = dotenv.get("STRIPE_WEBHOOK");

    private final UserService userService;

    private final OrderRepository orderRepository;

    private final ProductService productService;

    private final ShoppingCartService shoppingCartService;

    private final EmailSenderService emailSenderService;

    private final RewardCardRepository rewardCardRepository;

    private static final String EVENT_TYPE = "checkout.session.completed";

    private static final String PRODUCT = "product_";

    public PaymentGatewayService(UserService userService, OrderRepository orderRepository,
            ProductService productService,
            ShoppingCartService shoppingCartService,
            EmailSenderService emailSenderService,
            RewardCardRepository rewardCardRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.emailSenderService = emailSenderService;
        this.shoppingCartService = shoppingCartService;
        Stripe.apiKey = endpointSecret;
        this.rewardCardRepository = rewardCardRepository;
    }

    public String createOrFetchCoupon(int discountPercentage) throws StripeException {
        // Para simplificar, creamos siempre un cupón (puedes cachearlo si prefieres)
        CouponCreateParams params = CouponCreateParams.builder()
                .setAmountOff((long) discountPercentage)
                .setDuration(CouponCreateParams.Duration.ONCE)
                .build();

        Coupon coupon = Coupon.create(params);
        return coupon.getId(); // ID que usaremos en la session
    }

    /**
     * Processes the checkout for a list of cart items.
     *
     * @param items the list of cart items to be checked out.
     * @param id    the ID of the user initiating the checkout process.
     * @return a message indicating whether the payment was successful or if an
     *         issue occurred.
     * @throws Exception
     * @throws IllegalArgumentException if any of the products in the cart are
     *                                  unavailable.
     */
    public String checkoutProducts(List<CartItemDTO> items, Long id) throws Exception {
        // Check if all products in the cart are available for purchase.
        boolean productAvailable = checkProductAvailable(items);

        // If any product is unavailable, return an error message.
        if (!productAvailable) {
            throw new Exception("No se ha completado el pago: Producto no disponible");
        }

        // Process the payment session if all products are available.
        return processPaymentSession(items, id);
    }

    /**
     * Processes the payment session for a list of cart items.
     *
     * @param items the list of cart items to be purchased.
     * @param id    the ID of the user initiating the payment.
     * @return a URL for the payment session or an error message if the session
     *         creation fails.
     * @throws StripeException if an error occurs while interacting with the Stripe
     *                         API.
     */
    private String processPaymentSession(List<CartItemDTO> items, Long id) {
        try {
            User user = null;

            if (id != null) {
                user = userService.getUserById(id);
            }

            // Build the line items for the payment session using the cart items.
            List<SessionCreateParams.LineItem> lineItems = buildLineItems(items);

            // Build metadata for the session, including user and cart details.
            Map<String, String> metadata = buildMetadata(items, id);

            // Create session parameters with the line items and metadata.

            SessionCreateParams.Builder params = buildSessionParams(lineItems, metadata, user);

            // Add user details to the session parameters (e.g., shipping address, email).
            addUserDetails(params);

            // Create the session in Stripe and return the URL for the user to complete
            // payment.
            Session session = Session.create(params.build());
            return session.getUrl();
        } catch (StripeException e) {
            // Return an error message if Stripe encounters an issue while creating the
            // session.
            return "No se ha completado el pago: " + e.getMessage();
        }
    }

    /**
     * Builds a list of line items for the payment session based on the cart items.
     *
     * @param items the list of cart items to be converted into line items.
     * @return a list of SessionCreateParams.LineItem objects representing the cart
     *         items.
     */
    public List<SessionCreateParams.LineItem> buildLineItems(List<CartItemDTO> items) {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (CartItemDTO item : items) {
            String productName = "Artists Heaven - " + item.getProduct().getName();
            if (!Section.ACCESSORIES.equals(item.getProduct().getSection())) {
                productName += " - Size: " + item.getSize() + " - Color: " + item.getColor();
            }

            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData
                    .builder()
                    .setName(productName)
                    .build();

            long unitPriceCents = item.getProduct().getPrice().longValue() * 100; // precio en céntimos

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("EUR")
                    .setUnitAmount(unitPriceCents)
                    .setProductData(productData)
                    .build();

            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setQuantity(item.getQuantity().longValue())
                    .setPriceData(priceData)
                    .build());
        }

        return lineItems;
    }

    /**
     * Builds a map of metadata for the payment session based on the cart items and
     * user ID.
     *
     * @param items the list of cart items to be included in the metadata.
     * @param id    the ID of the user initiating the payment session.
     * @return a map containing metadata for the session, including user ID and cart
     *         details.
     */
    private Map<String, String> buildMetadata(List<CartItemDTO> items, Long id) {
        Map<String, String> metadata = new HashMap<>();

        // Add the user ID to the metadata (or "Anonymous" if no ID is provided).
        metadata.put("userId", id != null ? id.toString() : "Anonymous");

        // Iterate through each cart item and add product-specific metadata.
        for (CartItemDTO item : items) {
            // Build a unique key for each product using the product's ID.
            String key = PRODUCT + item.getProduct().getId();

            // Create a string that includes the product quantity and size.
            String productMetadata = "";
            if (item.getProduct().getSection().equals(Section.ACCESSORIES)) {
                productMetadata = item.getQuantity() + "|" + (item.getColor() != null ? item.getColor() : "");

            } else {
                productMetadata = item.getQuantity() + "|" + item.getSize() + "|"
                        + (item.getColor() != null ? item.getColor() : "");
            }

            // Merge the metadata for the product, appending new values if the product
            // already exists.
            metadata.merge(key, productMetadata, (oldValue, newValue) -> oldValue + "," + newValue);
        }

        // Return the metadata map containing all relevant information.
        return metadata;
    }

    /**
     * Builds the session parameters for creating a Stripe payment session.
     *
     * @param lineItems the list of line items to be included in the payment
     *                  session.
     * @param metadata  the metadata to be included with the session (e.g., user and
     *                  product details).
     * @return a builder with the configured session parameters.
     */
    private SessionCreateParams.Builder buildSessionParams(
            List<SessionCreateParams.LineItem> lineItems,
            Map<String, String> metadata,
            @Nullable User user) throws StripeException {

        // Inicializamos los parámetros de sesión
        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:3000/cancel")
                .addAllLineItem(lineItems);

        metadata.forEach(params::putMetadata);

        if (user != null) {
            rewardCardRepository.findFirstByUserAndRedeemedFalse(user).ifPresent(card -> {
                try {
                    CouponCreateParams couponParams = CouponCreateParams.builder()
                            .setPercentOff(BigDecimal.valueOf(card.getDiscountPercentage()))
                            .setDuration(CouponCreateParams.Duration.ONCE)
                            .build();

                    String couponId = Coupon.create(couponParams).getId();

                    params.addDiscount(
                            SessionCreateParams.Discount.builder()
                                    .setCoupon(couponId)
                                    .build());
                } catch (StripeException e) {
                    throw new RuntimeException("Error creando cupón en Stripe", e);
                }
            });
        }

        return params;
    }

    /**
     * Adds user details to the Stripe payment session, including email and shipping
     * address.
     * If the user is authenticated, their email and shipping address are added to
     * the session.
     * If the user is not authenticated, a limited set of shipping address options
     * is provided.
     *
     * @param params the builder for creating the payment session, where user
     *               details will be added.
     */
    private void addUserDetails(SessionCreateParams.Builder params) {
        // Retrieve the current authentication information.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Add phone number form
        params.setPhoneNumberCollection(SessionCreateParams.PhoneNumberCollection.builder().setEnabled(true).build());

        // If the user is authenticated and is an instance of User, add their details to
        // the session.
        // If the user is authenticated by Google, they will be treated as an anonymous
        // user because they don't have an address
        if (authentication != null && authentication.getPrincipal() instanceof User user && user.getAddress() != null) {
            // Set the user's email for the Stripe session.
            params.setCustomerEmail(user.getEmail());
            // Add the user's shipping address to the session (if available).
            addUserShippingAddress(params, user);
        } else {
            // If the user is not authenticated, set the allowed countries for shipping
            // address collection.
            params.setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.US)
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.ES)
                    .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.FR)
                    .build());
        }
    }

    /**
     * Adds the user's shipping address details to the Stripe payment session if
     * available.
     * If the user has an address, it creates custom fields for the shipping details
     * and adds them to the session.
     *
     * @param params the builder for creating the payment session, where shipping
     *               address details will be added.
     * @param user   the authenticated user whose shipping address details will be
     *               added to the session.
     */
    private void addUserShippingAddress(SessionCreateParams.Builder params, User user) {
        // Check if the user has a shipping address.
        if (user.getAddress() != null) {
            // Create a map with custom fields for the user's city, address, and postal
            // code.
            Map<String, String[]> customFieldsData = Map.of(
                    "city", new String[] { "Ciudad", user.getCity() },
                    "shipping_address", new String[] { "Dirección de Envío", user.getAddress() },
                    "postal_code", new String[] { "Código Postal", user.getPostalCode() });

            // Iterate over the custom fields data and add each field to the session
            // builder.
            customFieldsData.forEach((key, value) -> params.addCustomField(createCustomField(key, value[0], value[1])));
        }
    }

    /**
     * Creates a custom field for the Stripe payment session, which will be
     * displayed as a text input field.
     * The custom field can be used to capture additional information, such as
     * shipping address details.
     *
     * @param key          the unique key identifying the custom field.
     * @param label        the label to be displayed next to the custom field.
     * @param defaultValue the default value to be shown in the custom field (e.g.,
     *                     user's address).
     * @return a custom field object that can be added to the Stripe payment
     *         session.
     */
    private SessionCreateParams.CustomField createCustomField(String key, String label, String defaultValue) {
        // Build the custom field with the provided key, label, and default value.
        return SessionCreateParams.CustomField.builder()
                // Set the unique key for the custom field (used to identify it).
                .setKey(key)
                // Set the label for the custom field, indicating what information should be
                // entered.
                .setLabel(
                        SessionCreateParams.CustomField.Label.builder()
                                // Set the type of label as custom text.
                                .setType(SessionCreateParams.CustomField.Label.Type.CUSTOM)
                                // Set the custom label text (e.g., "Ciudad", "Dirección de Envío").
                                .setCustom(label)
                                .build())
                // Set the type of the custom field as text input.
                .setType(SessionCreateParams.CustomField.Type.TEXT)
                // Set the default text value for the custom field (e.g., user's city, address).
                .setText(
                        SessionCreateParams.CustomField.Text.builder()
                                // Set the default value for the text input.
                                .setDefaultValue(defaultValue)
                                .build())
                // Build and return the custom field object.
                .build();
    }

    /**
     * Processes a Stripe event by verifying the signature and performing actions
     * based on the event type.
     * Specifically, it handles events related to successful payments by retrieving
     * the session,
     * creating an order, and performing post-order actions.
     *
     * @param payload   the payload of the Stripe event.
     * @param sigHeader the signature header of the Stripe event, used to verify the
     *                  event's authenticity.
     * @throws MessagingException
     */
    @Transactional
    public void processStripeEvent(String payload, String sigHeader) throws MessagingException {
        Event event = verifySignature(payload, sigHeader);

        if (EVENT_TYPE.equals(event.getType())) {
            Session session = getSession(event);
            if (session == null) {
                return;
            }

            Long userId = getUserId(session);
            User user = userId == null ? null : userService.getUserById(userId);
            String email = (user != null) ? user.getEmail() : session.getCustomerDetails().getEmail();

            Session.TotalDetails totalDetails = session.getTotalDetails();
            Long discountAmount = 0L;
            if (totalDetails != null) {
                discountAmount = totalDetails.getAmountDiscount() / 100;
            }

            if (user != null) {
                rewardCardRepository.findFirstByUserAndRedeemedFalse(user).ifPresent(card -> {
                    card.setRedeemed(true);
                    card.setRedeemedAt(LocalDateTime.now());
                    rewardCardRepository.save(card);
                });
            }

            Order order = createOrder(session, userId, user, discountAmount);
            createOrderItems(session, order, userId);
            handlePostOrderActions(userId, email, order, discountAmount);
        }
    }

    /**
     * Verifies the signature of a Stripe webhook event to ensure its authenticity.
     * The method uses the provided payload, signature header, and webhook secret to
     * validate the event's signature.
     *
     * @param payload   the payload of the Stripe event to be verified.
     * @param sigHeader the signature header of the Stripe event used for
     *                  verification.
     * @return the verified Stripe event if the signature is valid.
     * @throws IllegalArgumentException if the signature verification fails.
     */
    private Event verifySignature(String payload, String sigHeader) {
        try {
            // Verify the event's signature using the payload, signature header, and webhook
            // secret.
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // If signature verification fails, throw an exception with the error message.
            throw new IllegalArgumentException("Error de verificación de firma: " + e.getMessage());
        }
    }

    /**
     * Retrieves the session from the Stripe event data.
     * The session is extracted from the event's data object.
     *
     * @param event the Stripe event containing the session data.
     * @return the session object extracted from the event, or null if not found.
     */
    private Session getSession(Event event) {
        // Extract and return the session from the event's data.
        return (Session) event.getDataObjectDeserializer().getObject().orElse(null);
    }

    /**
     * Retrieves the user ID from the session metadata.
     * If the user ID is marked as "Anonymous", it returns null to indicate the
     * absence of a valid user ID.
     *
     * @param session the session object containing the metadata.
     * @return the user ID extracted from the session metadata, or null if the user
     *         ID is "Anonymous".
     */
    private Long getUserId(Session session) {
        // Retrieve the user ID string from the session's metadata.
        String userIdStr = session.getMetadata().get("userId");
        // If the user ID is "Anonymous", return null, otherwise parse the user ID to a
        // Long.
        return "Anonymous".equals(userIdStr) ? null : Long.parseLong(userIdStr);
    }

    /**
     * Creates a new order based on the Stripe session and user details.
     * The order's total price is calculated from the session's total amount
     * (converted from cents to the appropriate currency).
     * The user ID and country are also set for the order if a valid user is
     * provided.
     *
     * @param session the Stripe session containing payment details.
     * @param userId  the ID of the user placing the order.
     * @param user    the user object containing additional details like country.
     * @return the saved order object after being persisted in the database.
     */
    private Order createOrder(Session session, Long userId, User user, Long disscount) {
        // Create a new Order object.
        Order order = new Order();
        // Convert the session's total amount (in cents) to the appropriate currency
        // value.
        Long amount = session.getAmountTotal() / 100;

        // Set the total price for the order.
        order.setTotalPrice(amount.floatValue());
        order.setPaymentIntent(session.getPaymentIntent());
        // Set the user ID associated with the order.
        order.setUser(user);
        order.setDiscountApplied(disscount);
        // If a user is provided, set the user's country in the order.
        if (user != null) {
            order.setCountry(user.getCountry());
        }
        // Save the order to the database and return the persisted order.
        return order;
    }

    /**
     * Creates the order items based on the data from the Stripe session.
     * The method processes custom fields and metadata to create individual order
     * items,
     * and then associates them with the order, including customer shipping details.
     *
     * @param session the Stripe session containing order and customer details.
     * @param order   the order object to which the items will be added.
     * @param userId  the ID of the user placing the order, used to determine
     *                shipping details.
     */
    private void createOrderItems(Session session, Order order, Long userId) {
        List<OrderItem> items = new ArrayList<>();

        List<String> fields = session.getCustomFields().stream()
                .map(field -> field.getText().getValue())
                .toList();

        boolean hasCustomFields = userId != null && fields.size() >= 3;

        String city = hasCustomFields ? fields.get(0) : session.getCustomerDetails().getAddress().getCity();
        String addressLine1 = hasCustomFields ? fields.get(1) : session.getCustomerDetails().getAddress().getLine1();
        String addressLine2 = hasCustomFields ? null : session.getCustomerDetails().getAddress().getLine2();
        String postalCode = hasCustomFields ? fields.get(2) : session.getCustomerDetails().getAddress().getPostalCode();
        String phone = session.getCustomerDetails().getPhone();
        String email = session.getCustomerDetails().getEmail();
        String country = getCountryName(session.getCustomerDetails().getAddress().getCountry());

        for (Map.Entry<String, String> entry : session.getMetadata().entrySet()) {
            if (entry.getKey().startsWith(PRODUCT)) {
                processProductEntry(entry.getKey(), entry.getValue(), order, items);
            }
        }

        finalizeOrder(order, items, city, addressLine1, addressLine2, postalCode, country, phone, email);
    }

    private String getCountryName(String countryCode) {
        return switch (countryCode) {
            case "US" -> "Estados Unidos";
            case "ES" -> "España";
            case "FR" -> "Francia";
            default -> null;
        };
    }

    /**
     * Processes a product entry from the session's metadata and creates an order
     * item.
     * The product ID is extracted from the key, and the quantity and size are
     * parsed from the value.
     * The product stock is updated accordingly, and the order item is saved and
     * added to the list of items.
     *
     * @param key   the key representing the product in the session's metadata.
     * @param value the value containing the quantity and size information for the
     *              product.
     * @param order the order to which the order item will be added.
     * @param items the list of order items being created for the order.
     */
    private void processProductEntry(String key, String value, Order order, List<OrderItem> items) {
        Long productId = Long.parseLong(key.replace(PRODUCT, ""));
        Product product = productService.findById(productId);

        for (String entry : value.split(",")) {
            String[] values = entry.split("\\|");
            int quantity = Integer.parseInt(values[0]);
            String size = "";

            if (product.getSection().equals(Section.ACCESSORIES)) {
                final String colorValue = values.length > 1 ? values[1] : "";

                ProductColor productColor = product.getColors().stream()
                        .filter(c -> c.getColorName().equalsIgnoreCase(colorValue))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "Color " + colorValue + " no encontrado para el producto " + product.getName()));

                int available = productColor.getAvailableUnits() != null ? productColor.getAvailableUnits() : 0;
                productColor.setAvailableUnits(available - quantity);

                OrderItem item = new OrderItem(productId, quantity, "", product.getName(), product.getPrice(),
                        order, product.getSection(), colorValue);
                items.add(item);
            } else {
                size = values.length > 1 ? values[1] : "";
                final String colorValue = values.length > 2 ? values[2] : "";

                ProductColor productColor = product.getColors().stream()
                        .filter(c -> c.getColorName().equalsIgnoreCase(colorValue))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException(
                                "Color " + colorValue + " no encontrado para el producto " + product.getName()));

                productColor.getSizes().computeIfPresent(size, (k, v) -> v - quantity);

                OrderItem item = new OrderItem(productId, quantity, size, product.getName(), product.getPrice(),
                        order, product.getSection(), colorValue);
                items.add(item);
            }
        }
    }

    /**
     * Finalizes the order by setting its unique identifier, status, associated
     * items, and shipping details.
     * The order is then saved to the repository.
     *
     * @param order        the order to finalize.
     * @param items        the list of items in the order.
     * @param city         the city for the shipping address.
     * @param addressLine1 the first line of the shipping address.
     * @param addressLine2 the second line of the shipping address (optional).
     * @param postalCode   the postal code for the shipping address.
     */
    private void finalizeOrder(Order order, List<OrderItem> items, String city, String addressLine1,
            String addressLine2, String postalCode, String country, String phone, String email) {

        long id = UUID.randomUUID().getMostSignificantBits();
        id = Math.abs(id);
        order.setIdentifier(id);
        order.setStatus(OrderStatus.PAID);
        order.setCity(city);
        order.setAddressLine1(addressLine1);
        order.setAddressLine2(addressLine2);
        order.setPostalCode(postalCode);
        order.setPhone(phone);
        order.setEmail(email);

        order.setItems(items);

        if (!country.isEmpty()) {
            order.setCountry(country);
        }

        orderRepository.save(order);
    }

    /**
     * Handles post-order actions such as deleting the user's shopping cart items
     * and sending a purchase confirmation email.
     *
     * @param userId the ID of the user who made the purchase.
     * @param email  the email address of the user to send the confirmation.
     * @throws MessagingException
     */
    private void handlePostOrderActions(Long userId, String email, Order order, Long discount)
            throws MessagingException {
        // If the user is registered, delete the items from their shopping cart.
        if (userId != null) {
            shoppingCartService.deleteShoppingCartUserItems(userId);
        }
        // Send a confirmation email to the user regarding their purchase.
        emailSenderService.sendPurchaseConfirmationEmail(email, order, discount);
    }

    /**
     * Checks if all products in the cart are available in the required quantities
     * and if they are in stock.
     * 
     * @param items the list of cart items to check for availability.
     * @return true if all products are available, false otherwise.
     */
    public boolean checkProductAvailable(List<CartItemDTO> items) {
        for (CartItemDTO i : items) {
            Product product = productService.findById(i.getProduct().getId());
            String colorValue = i.getColor();

            ProductColor productColor = product.getColors().stream()
                    .filter(c -> c.getColorName().equalsIgnoreCase(colorValue))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Color " + colorValue + " no encontrado para el producto " + product.getName()));

            if (Section.ACCESSORIES.equals(product.getSection())) {
                if (productColor.getAvailableUnits() < i.getQuantity()) {
                    return false;
                }
            } else {
                if (productColor.getSizes().get(i.getSize()) < i.getQuantity()) {
                    return false;
                }
            }
        }
        return true;
    }
}
