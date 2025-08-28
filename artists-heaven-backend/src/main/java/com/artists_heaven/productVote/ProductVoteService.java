package com.artists_heaven.productVote;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.entities.user.UserRepository;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.userProduct.UserProduct;
import com.artists_heaven.userProduct.UserProductRepository;

@Service
public class ProductVoteService {

    private final ProductVoteRepository productVoteRepository;
    private final UserProductRepository userProductRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public ProductVoteService(ProductVoteRepository productVoteRepository, UserProductRepository userProductRepository,
            UserRepository userRepository, MessageSource messageSource) {
        this.productVoteRepository = productVoteRepository;
        this.userProductRepository = userProductRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public void votePositive(Long productId, Long userId, String lang) {
        Locale locale = new Locale(lang);
        String userNotFound = messageSource.getMessage("user.NotFound", null, locale);
        String productNotFound = messageSource.getMessage("product.not_found", null, locale);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(userNotFound));
        UserProduct product = userProductRepository.findById(productId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(productNotFound));

        // Validar que no sea el mismo dueño
        String voteOwnProduct = messageSource.getMessage("vote.ownProduct", null, locale);
        if (product.getOwner().getId().equals(user.getId())) {
            throw new AppExceptions.BadRequestException(voteOwnProduct);
        }

        // Validar si ya votó
        String alreadyVote = messageSource.getMessage("vote.alreadyVote", null, locale);
        if (productVoteRepository.existsByUserAndProduct(user, product)) {
            throw new AppExceptions.BadRequestException(alreadyVote);
        }

        // Guardar voto
        ProductVote vote = new ProductVote();
        vote.setUser(user);
        vote.setProduct(product);
        productVoteRepository.save(vote);

        // Actualizar numVotes y puntos
        product.setNumVotes(product.getNumVotes() == null ? 1 : product.getNumVotes() + 1);
        userProductRepository.save(product);

        User owner = product.getOwner();
        owner.setPoints(owner.getPoints() + 5);
        userRepository.save(owner);
    }
}
