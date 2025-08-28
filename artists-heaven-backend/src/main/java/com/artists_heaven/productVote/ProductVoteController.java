package com.artists_heaven.productVote;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.standardResponse.StandardResponse;

@RestController
@RequestMapping("/api/productVote")
public class ProductVoteController {

    private final ProductVoteService productVoteService;

    private final MessageSource messageSource;

    public ProductVoteController(ProductVoteService productVoteService, MessageSource messageSource) {
        this.productVoteService = productVoteService;
        this.messageSource = messageSource;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<StandardResponse<String>> votePositive(@PathVariable Long productId,
            @AuthenticationPrincipal User user, String lang) {
        productVoteService.votePositive(productId, user.getId(), lang);
        Locale locale = new Locale(lang);
        String voteRegistered = messageSource.getMessage("vote.registeredSuccessfully", null, locale);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new StandardResponse<>(voteRegistered, null,
                                                HttpStatus.CREATED.value()));
    }

}
