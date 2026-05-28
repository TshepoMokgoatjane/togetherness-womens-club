package za.co.twc.togetherness.womens.club.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;
import za.co.twc.togetherness.womens.club.service.ContributionService;
import za.co.twc.togetherness.womens.club.service.ExcelExportService;
import za.co.twc.togetherness.womens.club.service.PdfExportService;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ContributionService contributionService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;

    @Autowired
    public ReportController(ContributionService contributionService,
                            PdfExportService pdfExportService,
                            ExcelExportService excelExportService) {
        this.contributionService = contributionService;
        this.pdfExportService = pdfExportService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/contributions")
    public String report(Model model) {

        YearMonth currentMonth = YearMonth.now();

        model.addAttribute("pageTitle", "Contribution Report");
        model.addAttribute("total", contributionService.getTotalContributionsForTheMonth(currentMonth));

        var paid = contributionService.getByStatus(currentMonth, ContributionStatus.PAID);
        var pending = contributionService.getByStatus(currentMonth, ContributionStatus.PENDING);
        var missed = contributionService.getByStatus(currentMonth, ContributionStatus.MISSED);

        model.addAttribute("paid", paid);
        model.addAttribute("pending", pending);
        model.addAttribute("missed", missed);

        model.addAttribute("paidCount", paid.size());
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("missedCount", missed.size());

        model.addAttribute("monthlyData", contributionService.getLast6MonthsTotal());

        return "report/contribution-report";
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf() throws IOException {

        YearMonth currentMonth = YearMonth.now();
        List<Contribution> allContributions = getAllContributionsForMonth(currentMonth);

        byte[] pdfBytes = pdfExportService.generateContributionReport(allContributions, currentMonth);

        String filename = "contributions_" + currentMonth.format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {

        YearMonth currentMonth = YearMonth.now();
        List<Contribution> allContributions = getAllContributionsForMonth(currentMonth);

        byte[] excelBytes = excelExportService.generateContributionReport(allContributions, currentMonth);

        String filename = "contributions_" + currentMonth.format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    private List<Contribution> getAllContributionsForMonth(YearMonth month) {
        List<Contribution> all = new ArrayList<>();
        all.addAll(contributionService.getByStatus(month, ContributionStatus.PAID));
        all.addAll(contributionService.getByStatus(month, ContributionStatus.PENDING));
        all.addAll(contributionService.getByStatus(month, ContributionStatus.MISSED));
        return all;
    }
}
