package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.entities.Store;
import com.upc.oss.monitoreo.repository.StoreRepository;
import com.upc.oss.monitoreo.service.MonitoringService;
import com.upc.oss.monitoreo.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final MonitoringService monitoringService;
    private final StoreRepository storeRepository;

    // ── Colores corporativos ─────────────────────────────────────
    private static final String COLOR_DARK_BG   = "0D1117";
    private static final String COLOR_HEADER_BG = "161D2C";
    private static final String COLOR_CYAN      = "00D4FF";
    private static final String COLOR_GREEN      = "10B981";
    private static final String COLOR_RED        = "EF4444";
    private static final String COLOR_YELLOW     = "F59E0B";
    private static final String COLOR_WHITE      = "E2E8F0";
    private static final String COLOR_GRAY       = "7A8BA0";
    private static final String COLOR_ROW_ALT    = "1C2333";
    private static final String COLOR_BORDER     = "2A3448";

    @Override
    public byte[] generateComplianceCsv(Long companyId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Compliance Report");
            sheet.setDisplayGridlines(false);
            sheet.setPrintGridlines(false);

            // Anchos de columna
            sheet.setColumnWidth(0, 36 * 256); // Store Name
            sheet.setColumnWidth(1, 16 * 256); // PC
            sheet.setColumnWidth(2, 16 * 256); // PT
            sheet.setColumnWidth(3, 16 * 256); // Status
            sheet.setColumnWidth(4, 4  * 256); // padding

            // ── Fila 0: título principal ─────────────────────────
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(36);
            XSSFCell titleCell = (XSSFCell) titleRow.createCell(0);
            titleCell.setCellValue("OSS Monitor · Reporte de Cumplimiento");
            titleCell.setCellStyle(buildTitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // ── Fila 1: subtítulo con fecha ──────────────────────
            Row subtitleRow = sheet.createRow(1);
            subtitleRow.setHeightInPoints(20);
            XSSFCell subtitleCell = (XSSFCell) subtitleRow.createCell(0);
            subtitleCell.setCellValue("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                    "  ·  Empresa ID: " + companyId);
            subtitleCell.setCellStyle(buildSubtitleStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            // ── Fila 2: espacio ──────────────────────────────────
            sheet.createRow(2).setHeightInPoints(8);

            // ── Fila 3: encabezados ──────────────────────────────
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(28);
            String[] columns = { "Local", "PC (%)", "PT (%)", "Estado" };
            CellStyle[] headerStyles = {
                    buildHeaderStyle(workbook, false),
                    buildHeaderStyle(workbook, true),
                    buildHeaderStyle(workbook, true),
                    buildHeaderStyle(workbook, true),
            };
            for (int i = 0; i < columns.length; i++) {
                XSSFCell cell = (XSSFCell) headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyles[i]);
            }

            // ── Filas de datos ───────────────────────────────────
            List<Store> stores = storeRepository.findByCompanyIdCompanyAndStatusTrue(companyId);
            int rowIdx = 4;

            for (int s = 0; s < stores.size(); s++) {
                Store store = stores.get(s);
                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(24);

                boolean isAlt = (s % 2 != 0);

                BigDecimal pc     = monitoringService.calculatePerformanceCompliance(store.getIdStore());
                BigDecimal pt     = monitoringService.calculateTimeElapsedPercentage(store.getIdStore());
                boolean onTrack   = pc.compareTo(pt) >= 0;
                String  status    = onTrack ? "ON TRACK" : "AT RISK";

                // Store name
                XSSFCell nameCell = (XSSFCell) row.createCell(0);
                nameCell.setCellValue(store.getName());
                nameCell.setCellStyle(buildDataStyle(workbook, isAlt, false, null));

                // PC
                XSSFCell pcCell = (XSSFCell) row.createCell(1);
                pcCell.setCellValue(pc.doubleValue());
                String pcColor = pc.doubleValue() >= 80 ? COLOR_GREEN
                        : pc.doubleValue() >= 50 ? COLOR_YELLOW
                        : COLOR_RED;
                pcCell.setCellStyle(buildDataStyle(workbook, isAlt, true, pcColor));

                // PT
                XSSFCell ptCell = (XSSFCell) row.createCell(2);
                ptCell.setCellValue(pt.doubleValue());
                ptCell.setCellStyle(buildDataStyle(workbook, isAlt, true, "A78BFA"));

                // Status
                XSSFCell statusCell = (XSSFCell) row.createCell(3);
                statusCell.setCellValue(status);
                statusCell.setCellStyle(buildStatusStyle(workbook, isAlt, onTrack));
            }

            // ── Fila final: total de locales ─────────────────────
            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.setHeightInPoints(22);
            XSSFCell totalLabel = (XSSFCell) totalRow.createCell(0);
            totalLabel.setCellValue("Total locales analizados: " + stores.size());
            totalLabel.setCellStyle(buildTotalStyle(workbook));
            sheet.addMergedRegion(new CellRangeAddress(rowIdx + 1, rowIdx + 1, 0, 3));

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generando reporte Excel", e);
        }
    }

    // ── Estilos ──────────────────────────────────────────────────

    private CellStyle buildTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(COLOR_DARK_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBottomBorderColor(new XSSFColor(hexToBytes(COLOR_CYAN), null));
        style.setBorderBottom(BorderStyle.MEDIUM);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(new XSSFColor(hexToBytes(COLOR_CYAN), null));
        font.setFontName("Calibri");
        style.setFont(font);
        return style;
    }

    private CellStyle buildSubtitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(COLOR_DARK_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(hexToBytes(COLOR_GRAY), null));
        font.setFontName("Calibri");
        style.setFont(font);
        return style;
    }

    private CellStyle buildHeaderStyle(XSSFWorkbook wb, boolean center) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(COLOR_HEADER_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Bordes
        XSSFColor borderColor = new XSSFColor(hexToBytes(COLOR_CYAN), null);
        style.setBottomBorderColor(borderColor);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setTopBorderColor(new XSSFColor(hexToBytes(COLOR_BORDER), null));
        style.setBorderTop(BorderStyle.THIN);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(new XSSFColor(hexToBytes(COLOR_CYAN), null));
        font.setFontName("Calibri");
        style.setFont(font);
        return style;
    }

    private CellStyle buildDataStyle(XSSFWorkbook wb, boolean alt, boolean center, String fontColor) {
        XSSFCellStyle style = wb.createCellStyle();
        String bg = alt ? COLOR_ROW_ALT : COLOR_HEADER_BG;
        style.setFillForegroundColor(new XSSFColor(hexToBytes(bg), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFColor borderColor = new XSSFColor(hexToBytes(COLOR_BORDER), null);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);

        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        if (fontColor != null) {
            font.setBold(true);
            font.setColor(new XSSFColor(hexToBytes(fontColor), null));
        } else {
            font.setColor(new XSSFColor(hexToBytes(COLOR_WHITE), null));
        }
        style.setFont(font);
        return style;
    }

    private CellStyle buildStatusStyle(XSSFWorkbook wb, boolean alt, boolean onTrack) {
        XSSFCellStyle style = wb.createCellStyle();
        String bg = alt ? COLOR_ROW_ALT : COLOR_HEADER_BG;
        style.setFillForegroundColor(new XSSFColor(hexToBytes(bg), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFColor borderColor = new XSSFColor(hexToBytes(COLOR_BORDER), null);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(borderColor);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Calibri");
        font.setColor(new XSSFColor(hexToBytes(onTrack ? COLOR_GREEN : COLOR_RED), null));
        style.setFont(font);
        return style;
    }

    private CellStyle buildTotalStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(hexToBytes(COLOR_HEADER_BG), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFColor borderColor = new XSSFColor(hexToBytes(COLOR_CYAN), null);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setTopBorderColor(borderColor);

        XSSFFont font = wb.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(new XSSFColor(hexToBytes(COLOR_GRAY), null));
        font.setFontName("Calibri");
        style.setFont(font);
        return style;
    }

    private byte[] hexToBytes(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new byte[]{ (byte) r, (byte) g, (byte) b };
    }
}
