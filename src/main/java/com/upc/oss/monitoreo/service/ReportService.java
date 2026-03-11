package com.upc.oss.monitoreo.service;

public interface ReportService {
    byte[] generateComplianceCsv(Long companyId);
}
