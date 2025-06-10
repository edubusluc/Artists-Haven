package com.artists_heaven.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.VerificationStatus;

import org.junit.jupiter.api.Test;

public class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private ProductService productService;

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

        when(adminRepository.findMonthlySalesData(year, OrderStatus.PAID)).thenReturn(mockResults);

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

}
