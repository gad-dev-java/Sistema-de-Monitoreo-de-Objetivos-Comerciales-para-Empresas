package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.jwt.JwtUtil;
import com.upc.oss.monitoreo.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReportExcel(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long companyId = jwtUtil.extractCompanyId(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compliance_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(reportService.generateComplianceCsv(companyId));
    }

}
