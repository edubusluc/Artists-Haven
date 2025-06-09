package com.artists_heaven.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import org.junit.jupiter.api.Test;

public class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

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

        when(adminRepository.findMonthlySalesData(year, OrderStatus.DELIVERED)).thenReturn(mockResults);

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

}
