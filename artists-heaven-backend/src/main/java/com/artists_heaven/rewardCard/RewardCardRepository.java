package com.artists_heaven.rewardCard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.artists_heaven.entities.user.User;

@Repository
public interface RewardCardRepository extends JpaRepository<RewardCard,Long>  {

    Optional<RewardCard> findFirstByUserAndRedeemedFalse(User user);

    List<RewardCard> findByUser(User user);

    List<RewardCard> findByUserAndRedeemedFalse(User user);

    boolean existsByUserAndRedeemedFalse(User user);

}
