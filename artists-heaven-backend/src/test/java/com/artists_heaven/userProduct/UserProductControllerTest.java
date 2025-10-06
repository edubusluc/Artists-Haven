package com.artists_heaven.userProduct;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.images.ImageServingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserProductService userProductService;

    @Mock
    private ImageServingUtil imageServingUtil;

    @InjectMocks
    private UserProductController userProductController;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userProductController)
                .setControllerAdvice(new com.artists_heaven.exception.GlobalExceptionHandler())
                .build();

        user = new User();
        user.setId(1L);
    }

    // ==========================
    // Test /create
    // ==========================
    @Test
    void createUserProduct_success() throws Exception {
        UserProductDTO dto = new UserProductDTO();
        dto.setName("Test Product");

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake".getBytes());

        MockMultipartFile dtoPart = new MockMultipartFile(
                "userProductDTO",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                new ObjectMapper().writeValueAsBytes(dto));

        UserProduct createdProduct = new UserProduct();
        createdProduct.setName("Test Product");

        when(userProductService.saveImages(anyList()))
                .thenReturn(List.of("/userProduct_media/test.png"));
        when(userProductService.createUserProduct(any(), anyLong(), anyString()))
                .thenReturn(createdProduct);

        mockMvc.perform(multipart("/api/user-products/create")
                .file(dtoPart)
                .file(imageFile)
                .param("lang", "en")
                .principal(() -> "user"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("UserProduct created successfully"))
                .andExpect(jsonPath("$.status").value(201));
    }

    // ==========================
    // Test /all
    // ==========================
    @Test
    void getAllUserProducts_success() throws Exception {
        UserProductDetailsDTO detailsDTO = new UserProductDetailsDTO();
        when(userProductService.getAllUserProductDetails(user.getId())).thenReturn(List.of(detailsDTO));

        mockMvc.perform(get("/api/user-products/all")
                .principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("UserProducts retrieved successfully"));
    }


    // ==========================
    // Test /myUserProducts
    // ==========================
    @Test
    void getMyUserProducts_success() throws Exception {
        UserProductDetailsDTO detailsDTO = new UserProductDetailsDTO();
        when(userProductService.findMyUserProducts(user.getId())).thenReturn(List.of(detailsDTO));

        mockMvc.perform(get("/api/user-products/myUserProducts")
                .principal(() -> "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("UserProducts retrieved successfully"));
    }
}
