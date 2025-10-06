package com.artists_heaven.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "ChatMessageRequestDTO", description = "Represents a chat message request sent by a user, including validation rules.")
public class ChatMessageRequestDTO {

    @Schema(
        description = "Content of the chat message. Cannot be blank and has a maximum length of 500 characters.",
        example = "Hello, I need help with my order."
    )
    @NotBlank
    @Size(max = 500)
    private String message;

}
