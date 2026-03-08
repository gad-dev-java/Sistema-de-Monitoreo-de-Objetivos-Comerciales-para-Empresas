package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/export")
    public ResponseEntity<DataResponse<String>> ExportReportExcel() {
        DataResponse<String> response = DataResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully generated excel file")
                .data(reportService.generateComplianceCsv())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

}
