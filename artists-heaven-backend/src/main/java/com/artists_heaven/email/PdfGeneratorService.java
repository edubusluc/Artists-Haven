package com.artists_heaven.email;

import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderItemRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
public class PdfGeneratorService {

    private final OrderItemRepository orderItemRepository;
    private final PdfFont bold;

    public PdfGeneratorService(OrderItemRepository orderItemRepository) throws Exception {
        this.orderItemRepository = orderItemRepository;
        this.bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
    }

    public byte[] generateInvoice(Long orderReference, Order order, Float total) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             InputStream templateStream = new ClassPathResource("Plantilla-Factura.pdf").getInputStream()) {

            PdfReader pdfReader = new PdfReader(templateStream);
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(pdfReader, writer);
            Document document = new Document(pdfDoc);

            addTitle(document);
            addHeader(document);
            addInvoiceInfo(document, orderReference, order);
            addProductTable(document, items);
            addTotal(document, total);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addTitle(Document document) {
        document.add(new Paragraph("FACTURA")
                .setFont(bold)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addHeader(Document document) {
        Paragraph header = new Paragraph("ARTISTS HEAVEN\nDirección: Calle Principal 123, Ciudad\nEmail: contacto@artistsheaven.com")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        document.add(header);
    }

    private void addInvoiceInfo(Document document, Long orderReference, Order order) {
        Paragraph invoiceInfo = new Paragraph()
                .add(new Text("Número de Factura: ").setFont(bold))
                .add(orderReference.toString()).add("\n")
                .add(new Text("Fecha: ").setFont(bold)).add(order.getCreatedDate().toString())
                .add("\n");
        document.add(invoiceInfo);
    }

    private void addProductTable(Document document, List<OrderItem> items) {
        Table table = new Table(new float[]{4, 2, 2});
        table.setWidth(UnitValue.createPercentValue(100));

        addTableHeaders(table);

        if (items == null || items.isEmpty()) {
            document.add(new Paragraph("No hay productos en esta factura.")
                    .setTextAlignment(TextAlignment.CENTER));
        } else {
            items.forEach(product -> addProductRow(table, product));
        }

        document.add(table);
    }

    private void addTableHeaders(Table table) {
        table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setFont(bold))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(Border.NO_BORDER));
        table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setFont(bold))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(Border.NO_BORDER));
        table.addHeaderCell(new Cell().add(new Paragraph("Talla").setFont(bold))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(Border.NO_BORDER));
        table.addHeaderCell(new Cell().add(new Paragraph("Precio (€)").setFont(bold))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(Border.NO_BORDER));
    }

    private void addProductRow(Table table, OrderItem product) {
        table.addCell(new Cell().add(new Paragraph(product.getName()).setFontSize(12))
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getQuantity())).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getSize())).setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", product.getPrice()))
                .setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));
    }

    private void addTotal(Document document, Float total) {
        document.add(new Paragraph("TOTAL A PAGAR: " + String.format("%.2f €", total))
                .setFontSize(16)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(10));
    }
}
