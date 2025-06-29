package com.artists_heaven.returns;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class ReturnRequestDTO {
    private Long orderId;
    private String reason;
    private String email;
    
}
