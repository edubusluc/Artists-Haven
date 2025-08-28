package com.artists_heaven.rewardCard;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.standardResponse.StandardResponse;

@RestController
@RequestMapping("/api/reward-cards")
public class RewardCardController {

    private final RewardCardRepository rewardCardRepository;
    private final RewardCardService rewardCardService;

    public RewardCardController(RewardCardRepository rewardCardRepository, RewardCardService rewardCardService) {
        this.rewardCardRepository = rewardCardRepository;
        this.rewardCardService = rewardCardService;
    }

    @GetMapping("/me")
    public StandardResponse<Integer> getMyPoints() {
        User user = getAuthenticatedUser();
        return new StandardResponse<>("User points fetched successfully", user.getPoints(), HttpStatus.OK.value());
    }

    // 🔹 Listar reward cards del usuario
    @GetMapping("/my")
    public StandardResponse<List<RewardCardDTO>> getMyRewardCards() {
        User user = getAuthenticatedUser();

        List<RewardCardDTO> cards = rewardCardRepository.findByUser(user)
                .stream()
                .map(RewardCardDTO::new)
                .toList();

        return new StandardResponse<>("Reward cards fetched successfully", cards, HttpStatus.OK.value());
    }

    // 🔹 Canjear puntos por una reward card
    @PostMapping("/redeem")
    public ResponseEntity<StandardResponse<RewardCard>> redeemRewardCard(
            @RequestBody RedeemRequest request,
            @RequestParam String lang) {

        StandardResponse<RewardCard> response = rewardCardService.redeemRewardCard(request, lang);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // 🔹 Método auxiliar para obtener usuario autenticado
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof User user)
            return user;
        throw new AppExceptions.BadRequestException("User not authenticated");
    }

    // DTO para el request de redeem
    public static class RedeemRequest {
        private int requiredPoints;

        public int getRequiredPoints() {
            return requiredPoints;
        }

        public void setRequiredPoints(int requiredPoints) {
            this.requiredPoints = requiredPoints;
        }
    }

}
