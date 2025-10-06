package com.artists_heaven.returns;

import com.artists_heaven.order.OrderService;
import com.artists_heaven.order.OrderStatus;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;


import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.kernel.font.PdfFont;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.order.Order;

@Service
public class ReturnService {

    private final ReturnRepository returnRepository;

    private final OrderService orderService;

    private final MessageSource messageSource;

    public ReturnService(ReturnRepository returnRepository, OrderService orderService, MessageSource messageSource) {
        this.returnRepository = returnRepository;
        this.orderService = orderService;
        this.messageSource = messageSource;
    }

    /**
     * Saves a return request.
     *
     * @param returnRequest the return request to save
     */
    public void save(Return returnRequest) {
        returnRepository.save(returnRequest);
    }

    /**
     * Creates a return request for a specific order.
     *
     * Rules:
     * <ul>
     * <li>User must be authenticated or provide a matching email.</li>
     * <li>Return requests are only allowed within 30 days of order creation.</li>
     * <li>Duplicate return requests for the same order are not allowed.</li>
     * </ul>
     *
     * @param order  the order to return
     * @param reason reason for the return
     * @param email  user's email (used for unauthenticated users)
     * @throws AppExceptions.ForbiddenActionException if the rules are violated
     * @throws AppExceptions.DuplicateActionException if a return request already
     *                                                exists for the order
     */
    public void createReturnForOrder(Order order, String reason, String email, String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isUnauthenticated = (authentication == null
                || authentication.getPrincipal().equals("anonymousUser"));
        boolean isEmailMismatch = !order.getEmail().equals(email);

        Locale locale = new Locale(lang);

        if (isUnauthenticated && isEmailMismatch) {
            String msg = messageSource.getMessage("return.message.unauthenticated", null, locale);
            throw new AppExceptions.ForbiddenActionException(msg);
        }

        if (order.getReturnRequest() != null) {
             String msg = messageSource.getMessage("return.message.duplicated", null, locale);
            throw new AppExceptions.DuplicateActionException(msg);
        }

        if (order.getCreatedDate().isBefore(LocalDateTime.now().minusDays(30))) {
            String msg = messageSource.getMessage("return.message.pasted_deadline ", null, locale);
            throw new AppExceptions.ForbiddenActionException(
                    msg);
        }

        Return returnRequest = new Return();
        returnRequest.setReason(reason);
        save(returnRequest);

        order.setReturnRequest(returnRequest);
        order.setStatus(OrderStatus.RETURN_REQUEST);
        order.setLastUpdateDateTime(LocalDateTime.now());
        orderService.save(order);
    }

    /**
     * Generates a PDF return label for a specific order.
     *
     * @param orderId     the ID of the order
     * @param isAnonymous true if the user's name should be hidden (email only)
     * @return PDF as byte array
     */
    public byte[] generateReturnLabelPdf(Long orderId, boolean isAnonymous) {
        Order order = orderService.findOrderById(orderId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        try {
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            // Title
            Paragraph title = new Paragraph("Etiqueta de Devolución")
                    .setFont(bold)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Order Info Table
            Table infoTable = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            infoTable.addCell(createLabelCell("ID del pedido:", bold));
            infoTable.addCell(createValueCell(order.getIdentifier().toString(), regular));

            if (!isAnonymous) {
                infoTable.addCell(createLabelCell("Cliente:", bold));
                infoTable.addCell(
                        createValueCell(order.getUser().getFirstName() + " " + order.getUser().getLastName(), regular));
            } else {
                infoTable.addCell(createLabelCell("Cliente:", bold));
                infoTable.addCell(
                        createValueCell(order.getEmail(), regular));
            }

            infoTable.addCell(createLabelCell("Dirección:", bold));
            infoTable.addCell(createValueCell(formatAddress(order), regular));

            infoTable.addCell(createLabelCell("ID del pago:", bold));
            infoTable.addCell(createValueCell(order.getPaymentIntent(), regular));

            document.add(infoTable);

            // Return Address
            Paragraph returnAddress = new Paragraph(
                    "Dirección de devolución:\nArtists Heaven\nCalle Verdejo 114\nSevilla, España")
                    .setFont(regular)
                    .setFontSize(12)
                    .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 1))
                    .setPadding(10)
                    .setMarginBottom(20);
            document.add(returnAddress);

            // Instruction note
            Paragraph note = new Paragraph(
                    "🔁 Esta etiqueta debe ser INCLUIDA dentro del paquete del pedido para procesar la devolución.")
                    .setFont(bold)
                    .setFontSize(12)
                    .setFontColor(ColorConstants.RED)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(note);

        } catch (Exception e) {
            throw new RuntimeException("No se ha podido generar la etiqueta de devolución", e);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private Cell createLabelCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11))
                .setBorder(Border.NO_BORDER);
    }

    private Cell createValueCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11))
                .setBorder(Border.NO_BORDER);
    }

    private String formatAddress(Order order) {
        StringBuilder sb = new StringBuilder();
        if (order.getAddressLine1() != null)
            sb.append(order.getAddressLine1()).append("\n");
        if (order.getAddressLine2() != null)
            sb.append(order.getAddressLine2()).append("\n");
        if (order.getPostalCode() != null || order.getCity() != null)
            sb.append(order.getPostalCode()).append(" ").append(order.getCity()).append("\n");
        if (order.getCountry() != null)
            sb.append(order.getCountry());
        return sb.toString();
    }

    /**
     * Finds a return request by ID.
     *
     * @param returnId the ID of the return request
     * @return the Return object
     * @throws AppExceptions.ResourceNotFoundException if not found
     */
    public Return findById(Long returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Return not found"));
    }

}
