package com.artists_heaven.entities.artist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.artists_heaven.admin.MonthlySalesDTO;
import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.entities.user.UserRole;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderStatus;
import com.artists_heaven.verification.VerificationStatus;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    private final UserRepository userRepository;

    public ArtistService(ArtistRepository artistRepository, UserRepository userRepository) {
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
    }

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers a new artist in the system.
     *
     * @param artist the artist object containing registration details
     * @return the registered artist after being saved in the database
     * @throws IllegalArgumentException if the email or artist name already exists
     */
    public Artist registerArtist(ArtistRegisterDTO request, Artist artist) {
        // Check if the email is already registered in the user repository
        User userEmail = userRepository.findByEmail(request.getEmail());
        if (userEmail != null) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        // Check if the artist name is already in use
        if (artistRepository.existsByArtistName(request.getArtistName()) == true) {
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre registrado");
        }

        // Set the username to match the artist name
        String username = request.getArtistName();
        artist.setUsername(username);

        artist.setArtistName(username);

        // Set the role of the artist
        artist.setRole(UserRole.ARTIST);

        // Encrypt the artist's password for secure storage
        artist.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set email
        artist.setEmail(request.getEmail());

        // Set Last name
        artist.setLastName(request.getLastName());

        // Set Firts name
        artist.setFirstName(request.getFirstName());

        // set url
        artist.setUrl(request.getUrl());

        //set color
        artist.setMainColor(request.getColor());

        // Save the artist in the database and return the saved entity
        return artistRepository.save(artist);
    }

    public Artist findById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + id));
    }

    public String isArtistVerificated(Long id) {
        Boolean isVerified = artistRepository.isArtistVerificated(id);
        List<VerificationStatus> latestVerificationStatus = artistRepository.findLatestVerificationStatus(id);
        String result = "";
        if (isVerified) {
            result = "Verified";
        } else {
            result = "Not Verified";
        }

        if (latestVerificationStatus.size() != 0) {
            result = latestVerificationStatus.get(0).toString();
        }
        return result;
    }

    public Integer getFutureEvents(Long id, Integer year) {
        return artistRepository.findFutureEventsForArtist(id, year);
    }

    public Integer getPastEvents(Long id, Integer year) {
        return artistRepository.findPastEventsForArtist(id, year);
    }

    public Map<String, Integer> getOrderItemCount(Long id, Integer year) {
        Artist artist = findById(id);
        List<OrderItem> ordersItemsByYear = artistRepository.getOrdersPerYear(artist.getArtistName().toUpperCase(),
                year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (OrderItem orderItem : ordersItemsByYear) {
            String itemName = orderItem.getName();
            itemsCount.merge(itemName, 1, Integer::sum);
        }
        return itemsCount;
    }

    public Map<String, Integer> getMostCountrySold(Long id, Integer year) {
        Artist artist = findById(id);
        List<OrderItem> ordersItemsByYear = artistRepository.getOrdersPerYear(artist.getArtistName().toUpperCase(),
                year);
        Map<String, Integer> itemsCount = new HashMap<>();

        for (OrderItem orderItem : ordersItemsByYear) {
            itemsCount.merge(orderItem.getOrder().getCountry(), 1, Integer::sum);
        }
        return itemsCount;
    }

    public List<MonthlySalesDTO> getMonthlySalesDataPerArtist(Long id, int year) {
        Artist artist = findById(id);
        List<Object[]> results = artistRepository.findMonthlySalesData(artist.getArtistName().toUpperCase(), year,
                OrderStatus.RETURN_ACCEPTED);
        List<MonthlySalesDTO> monthlySalesDTOList = new ArrayList<>();
        for (Object[] result : results) {
            Integer month = (int) result[0];
            Long totalOrders = (Long) result[1]; // El número total de productos vendidos

            MonthlySalesDTO dto = new MonthlySalesDTO();
            dto.setMonth(month);
            dto.setTotalOrders(totalOrders);

            monthlySalesDTOList.add(dto);
        }

        return monthlySalesDTOList;
    }

    public List<Artist> getValidArtists() {
        return artistRepository.findValidaAritst();

    }

}
