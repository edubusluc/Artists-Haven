package com.artists_heaven.email;

import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderItemRepository;
import com.artists_heaven.product.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PdfGeneratorServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private PdfGeneratorService pdfGeneratorService;

    private Order order;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void testSetUp(){
        order = new Order();
        order.setId(1L);
        order.setCreatedDate(LocalDate.now());

        Product product = new Product();
        product.setId(1l);

        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        item1.setId(1l);
        item1.setOrder(order);
        item1.setProductId(product.getId());
        item1.setName("Test Product 1");
        item1.setQuantity(10);
        item1.setSize("L");
        item1.setPrice(10.0f);

        item2.setId(2L);
        item2.setOrder(order);
        item2.setProductId(product.getId());
        item2.setName("Test Product 2");
        item2.setQuantity(10);
        item2.setSize("M");
        item2.setPrice(10.0f);

        orderItems = Arrays.asList(item1, item2);

        order.setItems(orderItems);
    }

    @Test
    void testGenerateInvoice() {

        // Simular una orden sin productos
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(orderItems);
        // Mock de OrderReference y Total
        Long orderReference = 12345L;
        Float total = 69.97f;

        // Generar el PDF
        byte[] pdfContent = pdfGeneratorService.generateInvoice(orderReference, order, total);

        // Comprobar que se genera el PDF correctamente
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0);

        // Verificar que el repositorio fue llamado una vez
        verify(orderItemRepository, times(1)).findByOrderId(order.getId());
    }

    @Test
    void testGenerateInvoiceNoItems() {
        // Simular una orden sin productos
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(Arrays.asList());

        Long orderReference = 12345L;
        Float total = 0.0f;

        // Generar el PDF
        byte[] pdfContent = pdfGeneratorService.generateInvoice(orderReference, order, total);

        // Comprobar que se genera el PDF correctamente incluso si no hay productos
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0);

        // Verificar que el repositorio fue llamado
        verify(orderItemRepository, times(1)).findByOrderId(order.getId());
    }
}
