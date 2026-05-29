package za.co.twc.togetherness.womens.club.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import za.co.twc.togetherness.womens.club.domain.AuditLog;
import za.co.twc.togetherness.womens.club.repository.AuditLogRepository;

@Controller
@RequestMapping("/admin/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String viewAuditLog(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model) {
        Page<AuditLog> auditPage = auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));

        model.addAttribute("auditLogs", auditPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditPage.getTotalPages());
        model.addAttribute("pageSize", size);
        return "admin/audit";
    }
}
