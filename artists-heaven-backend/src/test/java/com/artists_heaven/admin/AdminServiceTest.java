package com.artists_heaven.admin;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.VerificationStatus;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;

class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCountUsers() {
        when(adminRepository.countUsers()).thenReturn(42);
        int count = adminService.countUsers();
        assertEquals(42, count);
        verify(adminRepository).countUsers();
    }

    @Test
    void testCountArtists() {
        when(adminRepository.countArtist()).thenReturn(10);
        int count = adminService.countArtists();
        assertEquals(10, count);
        verify(adminRepository).countArtist();
    }

    @Test
    void testGetMonthlySalesData() {
        int year = 2024;
        List<Object[]> mockResults = List.of(
                new Object[] { 1, 5L, 1000.0 },
                new Object[] { 2, 10L, 2000.0 });

        when(adminRepository.findMonthlySalesData(year, OrderStatus.RETURN_ACCEPTED)).thenReturn(mockResults);

        List<MonthlySalesDTO> result = adminService.getMonthlySalesData(year);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getMonth());
        assertEquals(5L, result.get(0).getTotalOrders());
        assertEquals(1000.0, result.get(0).getTotalRevenue());
    }

    @Test
    void testGetOrderStatusCounts() {
        int year = 2024;
        List<Object[]> mockResults = List.of(
                new Object[] { "DELIVERED", 5 },
                new Object[] { "SENT", 2 });

        when(adminRepository.findOrderStatusCounts(year)).thenReturn(mockResults);

        Map<OrderStatus, Integer> result = adminService.getOrderStatusCounts(year);

        assertEquals(2, result.size());
        assertEquals(5, result.get(OrderStatus.DELIVERED));
        assertEquals(2, result.get(OrderStatus.SENT));
    }

    @Test
    void testGetVerificationStatusCount() {
        int year = 2024;
        List<Object[]> mockResults = List.of(
                new Object[] { "ACCEPTED", 3 },
                new Object[] { "REJECTED", 1 });

        when(adminRepository.findVerificationStatusCounts(year)).thenReturn(mockResults);

        Map<VerificationStatus, Integer> result = adminService.getVerificationStatusCount(year);

        assertEquals(2, result.size());
        assertEquals(3, result.get(VerificationStatus.ACCEPTED));
        assertEquals(1, result.get(VerificationStatus.REJECTED));
    }

    @Test
    void testGestMostSolditems() {
        int year = 2024;

        OrderItem orderItem = new OrderItem();
        orderItem.setName("Item Test");
        List<OrderItem> orderItems = new ArrayList<>(Arrays.asList(orderItem));

        Order order = new Order();
        order.setItems(orderItems);
        List<Order> orders = new ArrayList<>(Arrays.asList(order));

        when(adminRepository.getOrdersPerYear(year)).thenReturn(orders);

        Map<String, Integer> result = adminService.getMostSoldItems(year);
        assertEquals(1, result.size());
        assertEquals(1, result.get("Item Test"));
    }

    @Test
    void testGetCountrySold() {
        int year = 2024;

        Order order = new Order();
        order.setCountry("España");
        List<Order> orders = new ArrayList<>(Arrays.asList(order));

        when(adminRepository.getOrdersPerYear(year)).thenReturn(orders);

        Map<String, Integer> result = adminService.getCountrySold(year);
        assertEquals(1, result.size());
        assertEquals(1, result.get("España"));

    }

    @Test
    void testGetMostCategory() {
        int year = 2024;
        Category category = new Category();
        category.setId(1L);
        category.setName("Test");
        Set<Category> categories = new HashSet<>(Set.of(category));

        Product product = new Product();
        product.setId(2L);
        product.setCategories(categories);

        OrderItem orderItem = new OrderItem();
        orderItem.setName("Item Test");
        orderItem.setProductId(2L);
        List<OrderItem> orderItems = new ArrayList<>(Arrays.asList(orderItem));

        Order order = new Order();
        order.setItems(orderItems);
        List<Order> orders = new ArrayList<>(Arrays.asList(order));

        when(adminRepository.getOrdersPerYear(year)).thenReturn(orders);
        when(productService.findById(orderItem.getProductId())).thenReturn(product);

        Map<String, Integer> result = adminService.getMostCategory(year);
        assertEquals(1, result.size());
        assertEquals(1, result.get("Test"));
    }

    @Test
    void testGetNotAvailableProducts() {
        when(adminRepository.findNotAvailableProducts()).thenReturn(1);
        Integer result = adminService.getNotAvailableProducts();

        assertNotNull(result);
    }

    @Test
    void testGetAvailableProducts() {
        when(adminRepository.findAvailableProducts()).thenReturn(1);
        Integer result = adminService.getAvailableProducts();

        assertNotNull(result);
    }

    @Test
    void testGetPromotedProducts() {
        when(adminRepository.findPromotedProducts()).thenReturn(1);
        Integer result = adminService.getPromotedProducts();

        assertNotNull(result);
    }

    @Test
    void testGetTotalProducts() {
        when(adminRepository.findTotalProductsCount()).thenReturn(1);
        Integer result = adminService.getTotalProducts();

        assertNotNull(result);
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        String search = "john";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        User user = new User();
        user.setId(1L);
        user.setFirstName("John Doe");
        user.setRole(UserRole.USER);

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(adminRepository.findAllSort(search, pageable)).thenReturn(userPage);

        // Act
        Page<UserProfileDTO> result = adminService.getAllUsers(search, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getFirstName());
    }

    @Test
    void testGetAllOrderSortByDate() {
        Order order = new Order();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").ascending());

        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
        when(adminRepository.findAllOrderSortByDate(pageable)).thenReturn(orderPage);

        Page<Order> result = adminService.getAllOrderSortByDate(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testUpdateOrderStatus() {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.SENT;
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);

        when(orderService.findOrderById(orderId)).thenReturn(order);

        adminService.updateOrderStatus(orderId, newStatus);

        assertEquals(newStatus, order.getStatus());
        verify(orderService, times(1)).save(order);
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        // Datos de prueba
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PAID;

        // Simulamos que el repositorio no encuentra la orden y lanza una excepción
        when(orderService.findOrderById(orderId))
                .thenThrow(new EntityNotFoundException("Order not found with id: " + orderId));

        // Llamamos al método updateOrderStatus y verificamos que se lanza la excepción
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            adminService.updateOrderStatus(orderId, newStatus);
        });

        // Verificamos el mensaje de la excepción
        assertEquals("Order not found with id: " + orderId, exception.getMessage());
    }
}
