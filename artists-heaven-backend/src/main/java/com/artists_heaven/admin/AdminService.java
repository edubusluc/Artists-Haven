package com.artists_heaven.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.VerificationStatus;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    private final ProductService productService;

    public AdminService(AdminRepository adminRepository, ProductService productService) {
        this.adminRepository = adminRepository;
        this.productService = productService;
    }

    public int countUsers() {
        return adminRepository.countUsers();
    }

    public Integer countArtists() {
        return adminRepository.countArtist();
    }

    public List<MonthlySalesDTO> getMonthlySalesData(int year) {
        List<Object[]> results = adminRepository.findMonthlySalesData(year, OrderStatus.PAID);
        List<MonthlySalesDTO> monthlySalesDTOList = new ArrayList<>();
        for (Object[] result : results) {
            Integer month = (int) result[0]; // El mes en formato "YYYY-MM"
            Long totalOrders = (Long) result[1]; // El número total de productos vendidos
            Double totalRevenue = (Double) result[2]; // El total de ingresos

            MonthlySalesDTO dto = new MonthlySalesDTO(month, totalOrders, totalRevenue);
            monthlySalesDTOList.add(dto);
        }

        return monthlySalesDTOList;
    }

    public Map<OrderStatus, Integer> getOrderStatusCounts(int year) {
        List<Object[]> results = adminRepository.findOrderStatusCounts(year);
        Map<OrderStatus, Integer> orderStatusMap = new HashMap<>();
        for (Object[] result : results) {
            OrderStatus status = OrderStatus.valueOf(result[0].toString());
            int count = Integer.parseInt(result[1].toString());
            orderStatusMap.put(status, count);
        }
        return orderStatusMap;
    }

    public Map<VerificationStatus, Integer> getVerificationStatusCount(int year) {
        List<Object[]> results = adminRepository.findVerificationStatusCounts(year);
        Map<VerificationStatus, Integer> verificationSatusMap = new HashMap<>();
        for (Object[] result : results) {
            VerificationStatus status = VerificationStatus.valueOf(result[0].toString());
            int count = Integer.parseInt(result[1].toString());
            verificationSatusMap.put(status, count);
        }
        return verificationSatusMap;
    }

    public Map<String, Integer> getMostSoldItems(int year) {
        List<Order> ordersByYear = adminRepository.getOrdersPerYear(year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (Order order : ordersByYear) {
            for (OrderItem item : order.getItems()) {
                String itemName = item.getName();
                itemsCount.merge(itemName, 1, Integer::sum);
            }
        }
        return itemsCount;
    }

    public Map<String, Integer> getCountrySold(int year) {
        List<Order> ordersByYear = adminRepository.getOrdersPerYear(year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (Order order : ordersByYear) {
                itemsCount.merge(order.getCountry(), 1, Integer::sum);
            }
            return itemsCount;
        }
        

    public Map<String, Integer> getMostCategory(int year) {
        List<Order> ordersByYear = adminRepository.getOrdersPerYear(year);
        Map<String, Integer> categoryCount = new HashMap<>();

        for (Order order : ordersByYear) {
            for (OrderItem item : order.getItems()) {
                Product product = productService.findById(item.getProductId());
                countProductCategories(product, categoryCount);
            }
        }

        return categoryCount;
    }

    private void countProductCategories(Product product, Map<String, Integer> categoryCount) {
        for (Category category : product.getCategories()) {
            categoryCount.merge(category.getName(), 1, Integer::sum);
        }
    }

}
