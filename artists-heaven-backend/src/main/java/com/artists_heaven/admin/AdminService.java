package com.artists_heaven.admin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserProfileDTO;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.product.Category;
import com.artists_heaven.product.Product;
import com.artists_heaven.product.ProductService;
import com.artists_heaven.verification.VerificationStatus;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    private final ProductService productService;

    private final OrderService orderService;

    public AdminService(AdminRepository adminRepository, ProductService productService, OrderService orderService) {
        this.adminRepository = adminRepository;
        this.productService = productService;
        this.orderService = orderService;
    }

    /**
     * Counts the total number of users in the system.
     *
     * @return the total number of users
     */
    public int countUsers() {
        return adminRepository.countUsers();
    }

    /**
     * Counts the total number of registered artists.
     *
     * @return the number of artists
     */
    public Integer countArtists() {
        return adminRepository.countArtist();
    }

    /**
     * Retrieves monthly sales data for a given year.
     *
     * @param year the year to fetch sales data for
     * @return a list of {@link MonthlySalesDTO} containing sales details
     */
    public List<MonthlySalesDTO> getMonthlySalesData(int year) {
        List<Object[]> results = adminRepository.findMonthlySalesData(year, OrderStatus.RETURN_ACCEPTED);
        List<MonthlySalesDTO> monthlySalesDTOList = new ArrayList<>();

        for (Object[] result : results) {
            Integer month = (result[0] != null) ? ((Number) result[0]).intValue() : null;
            Long totalOrders = (result[1] != null) ? ((Number) result[1]).longValue() : 0L;
            Double totalRevenue = (result[2] != null) ? ((Number) result[2]).doubleValue() : 0.0;

            MonthlySalesDTO dto = new MonthlySalesDTO(month, totalOrders, totalRevenue);
            monthlySalesDTOList.add(dto);
        }

        return monthlySalesDTOList;
    }

    /**
     * Retrieves the count of orders grouped by their status for a given year.
     *
     * @param year the year to filter orders by
     * @return a map of {@link OrderStatus} to their respective counts
     */
    public Map<OrderStatus, Integer> getOrderStatusCounts(int year) {
        List<Object[]> results = adminRepository.findOrderStatusCounts(year);
        Map<OrderStatus, Integer> orderStatusMap = new EnumMap<>(OrderStatus.class);
        for (Object[] result : results) {
            OrderStatus status = OrderStatus.valueOf(result[0].toString());
            int count = Integer.parseInt(result[1].toString());
            orderStatusMap.put(status, count);
        }
        return orderStatusMap;
    }

    /**
     * Retrieves the verification status counts for users in a given year.
     *
     * @param year the year to filter verification statuses
     * @return a map of {@link VerificationStatus} to their respective counts
     */
    public Map<VerificationStatus, Integer> getVerificationStatusCount(int year) {
        List<Object[]> results = adminRepository.findVerificationStatusCounts(year);
        Map<VerificationStatus, Integer> verificationStatusMap = new EnumMap<>(VerificationStatus.class);
        for (Object[] result : results) {
            VerificationStatus status = VerificationStatus.valueOf(result[0].toString());
            int count = Integer.parseInt(result[1].toString());
            verificationStatusMap.put(status, count);
        }
        return verificationStatusMap;
    }

    /**
     * Retrieves the most sold items in a given year.
     *
     * @param year the year to filter orders by
     * @return a map of item names to the total quantity sold
     */
    public Map<String, Integer> getMostSoldItems(int year) {
        List<Order> ordersByYear = adminRepository.getOrdersPerYear(year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (Order order : ordersByYear) {
            for (OrderItem item : order.getItems()) {
                String itemName = item.getName();
                itemsCount.merge(itemName, item.getQuantity(), Integer::sum);
            }
        }
        return itemsCount;
    }

    /**
     * Retrieves the number of orders placed per country in a given year.
     *
     * @param year the year to filter orders by
     * @return a map of country names to their respective order counts
     */
    public Map<String, Integer> getCountrySold(int year) {
        List<Order> ordersByYear = adminRepository.getOrdersPerYear(year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (Order order : ordersByYear) {
            itemsCount.merge(order.getCountry(), 1, Integer::sum);
        }
        return itemsCount;
    }

    /**
     * Retrieves the most sold product categories in a given year.
     *
     * @param year the year to filter orders by
     * @return a map of category names to their respective counts
     */
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

    /**
     * Counts categories for a given product and updates the provided map.
     *
     * @param product       the product to extract categories from
     * @param categoryCount the map to update with category counts
     */
    private void countProductCategories(Product product, Map<String, Integer> categoryCount) {
        for (Category category : product.getCategories()) {
            categoryCount.merge(category.getName(), 1, Integer::sum);
        }
    }

    /**
     * Retrieves the number of products that are not available.
     *
     * @return the count of unavailable products
     */
    public Integer getNotAvailableProducts() {
        return adminRepository.findNotAvailableProducts();
    }

    /**
     * Retrieves the number of available products.
     *
     * @return the count of available products
     */
    public Integer getAvailableProducts() {
        return adminRepository.findAvailableProducts();
    }

    /**
     * Retrieves the number of promoted products.
     *
     * @return the count of promoted products
     */
    public Integer getPromotedProducts() {
        return adminRepository.findPromotedProducts();
    }

    /**
     * Retrieves the total number of products.
     *
     * @return the total count of products
     */
    public Integer getTotalProducts() {
        return adminRepository.findTotalProductsCount();
    }

    /**
     * Retrieves all users with optional search filtering and pagination.
     *
     * @param search   the search term to filter users by (can be null)
     * @param pageable pagination and sorting information
     * @return a paginated list of {@link UserProfileDTO}
     */
    public Page<UserProfileDTO> getAllUsers(String search, Pageable pageable) {
        Page<User> users = adminRepository.findAllSort(search, pageable);
        return users.map(UserProfileDTO::new);
    }

    /**
     * Retrieves orders filtered by status, search term, and pagination.
     *
     * @param status   the status to filter by (can be null)
     * @param search   the search term to filter by (can be null)
     * @param pageable pagination and sorting information
     * @return a paginated list of {@link Order}
     */
    public Page<Order> getOrdersFiltered(String status, String search, Pageable pageable) {
        if (status != null && search != null) {
            return adminRepository.findByStatusAndSearch(status, search, pageable);
        } else if (status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return adminRepository.findByStatus(orderStatus, pageable);
        } else if (search != null) {
            return adminRepository.findBySearch(search, pageable);
        } else {
            return adminRepository.findAllOrderSortByDate(pageable);
        }
    }

    /**
     * Updates the status of a specific order.
     *
     * @param id          the ID of the order
     * @param orderStatus the new status to set
     * @throws IllegalArgumentException if the order is not found
     */
    public void updateOrderStatus(Long id, OrderStatus orderStatus) {
        Order order = orderService.findOrderById(id);
        order.setStatus(orderStatus);
        orderService.save(order);
    }

}
