package com.artists_heaven.returns;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ReturnRequestDTO", description = "Represents a request to return an order, including order ID, reason for return, and contact email.")
public class ReturnRequestDTO {

    @Schema(description = "Identifier of the order to be returned", example = "1001")
    private Long orderId;

    @Schema(description = "Reason for requesting the return", example = "The product arrived damaged")
    private String reason;

    @Schema(description = "Email address of the user submitting the return request", example = "user@example.com")
    private String email;

}
