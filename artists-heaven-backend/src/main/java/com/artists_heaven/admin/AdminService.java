package com.artists_heaven.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public int countUsers() {
        return adminRepository.countUsers();
    }

    public Integer countArtists() {
        return adminRepository.countArtist();
    }

    public List<MonthlySalesDTO> getMonthlySalesData(int year) {
        List<Object[]> results = adminRepository.findMonthlySalesData(year, OrderStatus.DELIVERED);
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

}
