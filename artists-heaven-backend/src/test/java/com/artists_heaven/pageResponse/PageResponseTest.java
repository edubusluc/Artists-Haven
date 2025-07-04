package com.artists_heaven.pageResponse;

import org.junit.jupiter.api.Test;

import com.artists_heaven.page.PageResponse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class PageResponseTest {

    @Test
    void testPageResponseConstructor() {
        // Arrange
        List<String> content = List.of("item1", "item2", "item3");
        int pageNumber = 1;
        int pageSize = 10;
        long totalElements = 30;
        int totalPages = 3;
        boolean last = false;

        // Act
        PageResponse<String> pageResponse = new PageResponse<>(content, pageNumber, pageSize, totalElements, totalPages,
                last);

        // Assert
        assertEquals(content, pageResponse.getContent());
        assertEquals(pageNumber, pageResponse.getPageNumber());
        assertEquals(pageSize, pageResponse.getPageSize());
        assertEquals(totalElements, pageResponse.getTotalElements());
        assertEquals(totalPages, pageResponse.getTotalPages());
        assertEquals(last, pageResponse.isLast());
    }

    @Test
    void testPageResponseLastPage() {
        // Arrange
        List<String> content = List.of("item1", "item2", "item3");
        int pageNumber = 2;
        int pageSize = 10;
        long totalElements = 30;
        int totalPages = 3;
        boolean last = true;

        // Act
        PageResponse<String> pageResponse = new PageResponse<>(content, pageNumber, pageSize, totalElements, totalPages,
                last);

        // Assert
        assertTrue(pageResponse.isLast());
    }

}
