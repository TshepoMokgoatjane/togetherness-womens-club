package za.co.twc.togetherness.womens.club.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;
import za.co.twc.togetherness.womens.club.service.ContributionService;

import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ContributionService contributionService;

    @Autowired
    public ReportController(ContributionService contributionService) {
        this.contributionService = contributionService;
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

        // ADD COUNTS FOR CHARTS
        model.addAttribute("paidCount", paid.size());
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("missedCount", missed.size());

        // ADD LINE CHART DATA
        model.addAttribute("monthlyData", contributionService.getLast6MonthsTotal());

        return "report/contribution-report";
    }
}
