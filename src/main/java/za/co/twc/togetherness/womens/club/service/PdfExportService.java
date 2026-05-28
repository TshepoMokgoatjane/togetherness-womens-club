package za.co.twc.togetherness.womens.club.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Contribution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generateContributionReport(List<Contribution> contributions, YearMonth month) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        document.add(new Paragraph("Togetherness Women's Club")
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Contribution Report - " + month.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(" ")); // Spacer

        // Table with 6 columns
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 20, 20, 15, 15, 15, 10}))
                .useAllAvailableWidth();

        // Header
        String[] headers = {"#", "Member No", "Member Name", "Amount (R)", "Payment Date", "Reference", "Status"};
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Data rows
        int rowNum = 1;
        for (Contribution c : contributions) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(rowNum++))));
            table.addCell(new Cell().add(new Paragraph(c.getMember().getMemberNumber())));
            table.addCell(new Cell().add(new Paragraph(c.getMember().getFirstName() + " " + c.getMember().getLastName())));
            table.addCell(new Cell().add(new Paragraph("R " + c.getAmount())).setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(c.getPaymentDate() != null ? c.getPaymentDate().toString() : "-")));
            table.addCell(new Cell().add(new Paragraph(c.getReference() != null ? c.getReference() : "-")));
            table.addCell(new Cell().add(new Paragraph(c.getStatus().name())));
        }

        document.add(table);

        // Footer
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total records: " + contributions.size())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT));

        document.close();
        return baos.toByteArray();
    }
}
