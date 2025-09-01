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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.order.Order;

@Service
public class ReturnService {

    private final ReturnRepository returnRepository;

    private final OrderService orderService;

    public ReturnService(ReturnRepository returnRepository, OrderService orderService) {
        this.returnRepository = returnRepository;
        this.orderService = orderService;
    }

    public void save(Return returnRequest) {
        returnRepository.save(returnRequest);
    }

    public void createReturnForOrder(Order order, String reason, String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isUnauthenticated = (authentication == null || !authentication.isAuthenticated());
        boolean isEmailMismatch = !order.getEmail().equals(email);

        if (isUnauthenticated && isEmailMismatch) {
            throw new AppExceptions.ForbiddenActionException("Invalid email or unauthenticated user");
        }

        if (order.getReturnRequest() != null) {
            throw new AppExceptions.DuplicateActionException("This order already has a return request.");
        }

        if (order.getCreatedDate().isBefore(LocalDateTime.now().minusDays(30))) {
            throw new AppExceptions.ForbiddenActionException(
                    "Return request can only be created within 30 days of order creation.");
        }

        Return returnRequest = new Return();
        returnRequest.setReason(reason);
        save(returnRequest);

        order.setReturnRequest(returnRequest);
        order.setStatus(OrderStatus.RETURN_REQUEST);
        order.setLastUpdateDateTime(LocalDateTime.now());
        orderService.save(order);
    }

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

    public Return findById(Long returnId) {
        return returnRepository.findById(returnId)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Return not found"));
    }

}
