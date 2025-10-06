package com.artists_heaven.returns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.artists_heaven.entities.user.User;
import com.artists_heaven.exception.AppExceptions.DuplicateActionException;
import com.artists_heaven.exception.AppExceptions.ForbiddenActionException;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderService;

class ReturnServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ReturnRepository returnRepository;

    @Mock 
    private MessageSource messageSource;

    @InjectMocks
    private ReturnService returnService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveReturn() {
        Return returns = new Return();
        returns.setId(1L);

        returnRepository.save(returns);

        verify(returnRepository, times(1)).save(returns);

    }

    @Test
    void testCreateReturnForOrder_Success() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        Order order = new Order();
        order.setEmail("test@example.com");
        order.setCreatedDate(LocalDateTime.now().minusDays(10));
        order.setUser(user);

        returnService.createReturnForOrder(order, "Producto dañado", "test@example.com", "es");

        verify(returnRepository, times(1)).save(any(Return.class));
        verify(orderService, times(1)).save(order);
    }

    @Test
    void testCreateReturnForOrder_TooLate() {
        Order order = new Order();
        order.setEmail("test@example.com");
        order.setCreatedDate(LocalDateTime.now().minusDays(40));

        when(messageSource.getMessage("return.message.pasted_deadline ", null, new Locale("en")))
                .thenReturn("The 30-day deadline to request a return has passed.");

        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            returnService.createReturnForOrder(order, "Producto tarde", "test@example.com", "en");
        });

        assertEquals("The 30-day deadline to request a return has passed.", exception.getMessage());
    }

    @Test
    void testCreateReturnForOrder_AlreadyHasReturn() {
        Return returns = new Return();

        Order order = new Order();
        order.setEmail("test@example.com");
        order.setCreatedDate(LocalDateTime.now().minusDays(10));
        order.setReturnRequest(returns);

        when(messageSource.getMessage("return.message.duplicated", null, new Locale("en")))
                .thenReturn("A return request for this order already exists.");

        DuplicateActionException exception = assertThrows(DuplicateActionException.class, () -> {
            returnService.createReturnForOrder(order, "Motivo duplicado", "test@example.com", "en");
        });

        assertEquals("A return request for this order already exists.", exception.getMessage());
    }

    @Test
    void testGenerateReturnLabelPdf_NotNullAndNotEmpty() {
        Order order = new Order();
        order.setIdentifier(123L);
        order.setPaymentIntent("PAY_001");
        order.setAddressLine1("Calle A");
        order.setAddressLine2("Depto 5");
        order.setPostalCode("12345");
        order.setCity("Ciudad X");
        order.setCountry("País Y");

        User user = new User();
        user.setFirstName("Ana");
        user.setLastName("García");
        order.setUser(user);

        when(orderService.findOrderById(1L)).thenReturn(order);

        byte[] pdf = returnService.generateReturnLabelPdf(1L, false);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGenerateReturnLabelPdf_Anonymous_UsesEmail() {
        Order order = new Order();
        order.setIdentifier(456L);
        order.setPaymentIntent("PAY_002");
        order.setAddressLine1("Calle B");
        order.setPostalCode("67890");
        order.setCity("Ciudad Y");
        order.setCountry("País Z");
        order.setEmail("anon@example.com");

        // 👀 no seteamos el user aquí, porque no se usa en el else
        when(orderService.findOrderById(2L)).thenReturn(order);

        byte[] pdf = returnService.generateReturnLabelPdf(2L, true);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testCreateReturnForOrder_EmailMismatchAndUnauthenticated() {
        Order order = new Order();
        order.setEmail("order@example.com");
        order.setCreatedDate(LocalDateTime.now().minusDays(10));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(messageSource.getMessage("return.message.unauthenticated", null, new Locale("en")))
                .thenReturn("This email is not associated with the order.");

        ForbiddenActionException exception = assertThrows(ForbiddenActionException.class, () -> {
            returnService.createReturnForOrder(order, "Razón inválida", "user@otrodominio.com", "en");
        });

        assertEquals("This email is not associated with the order.", exception.getMessage());
    }

    @Test
    void testCreateReturnForOrder_AuthenticatedUserWithDifferentEmail() {
        User user = new User();
        user.setFirstName("Carlos");
        user.setLastName("López");

        Order order = new Order();
        order.setEmail("order@example.com");
        order.setCreatedDate(LocalDateTime.now().minusDays(10));
        order.setUser(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        returnService.createReturnForOrder(order, "Razón válida", "otro@example.com", "en");

        verify(returnRepository, times(1)).save(any(Return.class));
        verify(orderService, times(1)).save(order);
    }

    @Test
    void testFindById() {
        Return result = new Return();
        when(returnRepository.findById(1L)).thenReturn(Optional.of(result));
        Return response = returnService.findById(1L);
        assertNotNull(response);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

}
