package com.artists_heaven.email;

import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.order.OrderItemRepository;
import com.artists_heaven.product.Section;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

        private final OrderItemRepository orderItemRepository;

        public PdfGeneratorService(OrderItemRepository orderItemRepository) {
                this.orderItemRepository = orderItemRepository;
        }

        /**
         * Generates an invoice PDF for the given order.
         *
         * @param orderReference the unique reference number of the order
         * @param order          the {@link Order} containing order details
         * @param total          the total price of the order
         * @param discount       the discount applied to the order (can be null)
         * @return a byte array containing the PDF file
         * @throws RuntimeException if the PDF cannot be generated
         */
        public byte[] generateInvoice(Long orderReference, Order order, Float total, Long discount) {
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                                InputStream templateStream = new ClassPathResource("Plantilla-Factura.pdf")
                                                .getInputStream()) {

                        PdfWriter writer = new PdfWriter(out);
                        PdfDocument pdfDoc = new PdfDocument(writer);

                        PdfDocument templateDoc = new PdfDocument(new PdfReader(templateStream));

                        PdfPage templatePage = templateDoc.getFirstPage();
                        pdfDoc.addPage(templatePage.copyTo(pdfDoc));
                        templateDoc.close();

                        Document document = new Document(pdfDoc);
                        document.setMargins(40, 40, 40, 40);

                        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                        addHeader(document, bold, normal);
                        addTitle(document, bold);
                        addInvoiceInfo(document, orderReference, order, bold, normal);
                        addProductTable(document, items, bold, normal);
                        addTotal(document, total, discount, bold, normal);
                        addFooter(document, normal);

                        document.close();
                        return out.toByteArray();

                } catch (Exception e) {
                        throw new RuntimeException("No se ha podido generar la factura", e);
                }
        }

        /**
         * Adds the company header to the PDF document.
         *
         * @param document the PDF {@link Document} to add content to
         * @param bold     the bold {@link PdfFont}
         * @param normal   the normal {@link PdfFont}
         */
        private void addHeader(Document document, PdfFont bold, PdfFont normal) {
                try {
                        Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 3, 7 }))
                                        .useAllAvailableWidth();

                        Cell emptyCell = new Cell().setBorder(Border.NO_BORDER);
                        headerTable.addCell(emptyCell);

                        Cell companyInfoCell = new Cell()
                                        .add(new Paragraph("ARTISTS HEAVEN").setFont(bold).setFontSize(14))
                                        .add(new Paragraph("Email: mod.artistheaven@gmail.com").setFont(normal))
                                        .setTextAlignment(TextAlignment.RIGHT)
                                        .setBorder(Border.NO_BORDER);

                        headerTable.addCell(companyInfoCell);

                        document.add(headerTable);
                } catch (Exception e) {
                        document.add(new Paragraph(
                                        "ARTISTS HEAVEN\nDirección: España, Sevilla\nEmail: mod.artistheaven@gmail.com")
                                        .setFontSize(10)
                                        .setTextAlignment(TextAlignment.RIGHT));
                }
        }

        /**
         * Adds the invoice title to the PDF document.
         *
         * @param document the PDF {@link Document}
         * @param bold     the bold {@link PdfFont} to use
         */
        private void addTitle(Document document, PdfFont bold) {
                document.add(new Paragraph("Detalles de la compra")
                                .setFont(bold)
                                .setFontSize(20)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginTop(20));
        }

        /**
         * Adds invoice details such as reference number, date, and shipping address.
         *
         * @param document       the PDF {@link Document}
         * @param orderReference the order reference number
         * @param order          the {@link Order} object
         * @param bold           bold {@link PdfFont} for labels
         * @param normal         normal {@link PdfFont} for values
         */
        private void addInvoiceInfo(Document document, Long orderReference, Order order, PdfFont bold, PdfFont normal) {
                Table infoTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 }))
                                .useAllAvailableWidth()
                                .setMarginTop(20)
                                .setBorder(new SolidBorder(1));

                infoTable.addCell(new Cell().add(new Paragraph("Número de Seguimiento:").setFont(bold))
                                .setBorder(Border.NO_BORDER));
                infoTable.addCell(new Cell().add(new Paragraph(orderReference.toString()))
                                .setBorder(Border.NO_BORDER));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm");
                String formattedDate = order.getCreatedDate().format(formatter);

                infoTable.addCell(new Cell().add(new Paragraph("Fecha de emisión:").setFont(bold))
                                .setBorder(Border.NO_BORDER));
                infoTable.addCell(new Cell().add(new Paragraph(formattedDate))
                                .setBorder(Border.NO_BORDER));

                infoTable.addCell(new Cell().add(new Paragraph("Dirección de Envío:").setFont(bold))
                                .setBorder(Border.NO_BORDER));

                StringBuilder addressBuilder = new StringBuilder();
                addressBuilder.append(order.getAddressLine1()).append("\n");
                if (order.getAddressLine2() != null && !order.getAddressLine2().isBlank()) {
                        addressBuilder.append(order.getAddressLine2()).append("\n");
                }
                addressBuilder.append(order.getPostalCode()).append(" ").append(order.getCity()).append("\n");
                addressBuilder.append(order.getCountry());

                infoTable.addCell(new Cell().add(new Paragraph(addressBuilder.toString()))
                                .setBorder(Border.NO_BORDER));

                document.add(infoTable);
        }

        /**
         * Adds a table listing all order items.
         *
         * @param document the PDF {@link Document}
         * @param items    the list of {@link OrderItem} to display
         * @param bold     bold {@link PdfFont} for headers
         * @param normal   normal {@link PdfFont} for cells
         */
        private void addProductTable(Document document, List<OrderItem> items, PdfFont bold, PdfFont normal) {
                document.add(new Paragraph("\n"));

                Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 2, 2, 2 }))
                                .useAllAvailableWidth()
                                .setMarginTop(10);

                // Cabecera
                String[] headers = { "Descripción", "Cantidad", "Talla", "Precio (€)" };
                for (String header : headers) {
                        table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(bold))
                                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                        .setTextAlignment(TextAlignment.CENTER));
                }

                if (items == null || items.isEmpty()) {
                        document.add(new Paragraph("No hay productos en esta factura.")
                                        .setTextAlignment(TextAlignment.CENTER));
                } else {
                        for (OrderItem product : items) {
                                Color rowColor = ColorConstants.WHITE;

                                table.addCell(new Cell().add(new Paragraph(sanitize(product.getName())))
                                                .setBackgroundColor(rowColor));

                                table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getQuantity())))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setBackgroundColor(rowColor));

                                String sizeValue = "-";
                                if (product.getSection() != Section.ACCESSORIES) {
                                        sizeValue = sanitize(String.valueOf(product.getSize()));
                                }

                                table.addCell(new Cell().add(new Paragraph(sizeValue))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setBackgroundColor(rowColor));

                                table.addCell(new Cell().add(new Paragraph(formatPrice(product.getPrice()) + " €"))
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .setBackgroundColor(rowColor));
                        }
                }

                document.add(table);
        }

        /**
         * Adds subtotal, discount, and total to the invoice.
         *
         * @param document the PDF {@link Document}
         * @param total    total price of the order
         * @param discount discount applied (can be null)
         * @param bold     bold {@link PdfFont} for total
         * @param normal   normal {@link PdfFont} for other values
         */
        private void addTotal(Document document, Float total, Long discount, PdfFont bold, PdfFont normal) {
                document.add(new Paragraph("\n"));

                Table totalTable = new Table(UnitValue.createPercentArray(new float[] { 6, 2 }))
                                .useAllAvailableWidth();

                if (discount != null && discount > 0) {
                        float subtotal = total + discount;

                        totalTable.addCell(new Cell().add(new Paragraph("Subtotal:").setFont(normal))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.RIGHT));
                        totalTable.addCell(new Cell()
                                        .add(new Paragraph(formatPrice(subtotal) + " €").setFont(normal))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.RIGHT));

                        totalTable.addCell(new Cell().add(new Paragraph("Descuento:").setFont(normal))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.RIGHT));
                        totalTable.addCell(new Cell()
                                        .add(new Paragraph("-" + formatPrice(discount.floatValue()) + " €")
                                                        .setFont(normal))
                                        .setBorder(Border.NO_BORDER)
                                        .setTextAlignment(TextAlignment.RIGHT));
                }

                totalTable.addCell(new Cell().add(new Paragraph("TOTAL A PAGAR:").setFont(bold).setFontSize(14))
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                totalTable.addCell(new Cell()
                                .add(new Paragraph(formatPrice(total) + " €").setFont(bold).setFontSize(14))
                                .setBorder(Border.NO_BORDER)
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setTextAlignment(TextAlignment.RIGHT));

                document.add(totalTable);
        }

        /**
         * Formats a price as a string with two decimal places.
         *
         * @param price the price to format
         * @return formatted price string
         */
        private String formatPrice(Float price) {
                if (price == null) {
                        return "0.00";
                }
                DecimalFormat df = new DecimalFormat("#0.00");
                return df.format(price);
        }

        /**
         * Sanitizes text for PDF output to prevent errors.
         *
         * @param input the text to sanitize
         * @return sanitized text
         */
        private String sanitize(String input) {
                return input == null ? "" : input.replaceAll("%", "%%");
        }

        /**
         * Adds a footer message to the invoice PDF.
         *
         * @param document the PDF {@link Document}
         * @param normal   normal {@link PdfFont} to use
         */
        private void addFooter(Document document, PdfFont normal) {
                document.add(new Paragraph("\nGracias por su compra en Artists Heaven!")
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontSize(12)
                                .setMarginTop(30));
        }
}
