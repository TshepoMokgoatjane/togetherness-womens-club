package za.co.twc.togetherness.womens.club.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Contribution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] generateContributionReport(List<Contribution> contributions, YearMonth month) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contributions - " + month.format(DateTimeFormatter.ofPattern("MMM yyyy")));

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"#", "Member No", "Member Name", "Amount (R)", "Payment Date", "Reference", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Contribution c : contributions) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue(c.getMember().getMemberNumber());
            row.createCell(2).setCellValue(c.getMember().getFirstName() + " " + c.getMember().getLastName());
            row.createCell(3).setCellValue(c.getAmount() != null ? c.getAmount().doubleValue() : 0.0);
            row.createCell(4).setCellValue(c.getPaymentDate() != null ? c.getPaymentDate().toString() : "-");
            row.createCell(5).setCellValue(c.getReference() != null ? c.getReference() : "-");
            row.createCell(6).setCellValue(c.getStatus().name());
            rowNum++;
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }
}
