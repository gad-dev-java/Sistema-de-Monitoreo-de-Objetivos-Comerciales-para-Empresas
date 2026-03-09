package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import com.upc.oss.monitoreo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final MonitoringService monitoringService;
    private final StoreRepository storeRepository;

    @Override
    public byte[] generateComplianceCsv() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Compliance Report");

            // Crear encabezados (HU08)
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Store Name", "PC (%)", "PT (%)", "Status"};
            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            List<Store> stores = storeRepository.findByStatusTrue();
            int rowIdx = 1;

            for (Store store : stores) {
                Row row = sheet.createRow(rowIdx++);

                BigDecimal pc = monitoringService.calculatePerformanceCompliance(store.getIdStore());
                BigDecimal pt = monitoringService.calculateTimeElapsedPercentage(store.getIdStore());
                String status = (pc.compareTo(pt) >= 0) ? "ON TRACK" : "AT RISK";

                row.createCell(0).setCellValue(store.getName());
                row.createCell(1).setCellValue(pc.doubleValue());
                row.createCell(2).setCellValue(pt.doubleValue());
                row.createCell(3).setCellValue(status);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
