package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.exception.GlobalExceptionHandler;
import com.upc.oss.monitoreo.jwt.JwtUtil;
import com.upc.oss.monitoreo.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController Unit Tests")
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;

    private static final String VALID_TOKEN = "mocked.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final byte[] EXCEL_CONTENT = "fake-excel-content".getBytes();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/reports/export")
    class ExportReportExcel {

        @Test
        @DisplayName("Should return 200 OK with Excel bytes when token is valid")
        void shouldReturn200_withExcelBytes_whenTokenIsValid() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(1L);
            when(reportService.generateComplianceCsv(1L)).thenReturn(EXCEL_CONTENT);

            // When / Then
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(EXCEL_CONTENT));
        }

        @Test
        @DisplayName("Should return Content-Disposition header with correct filename")
        void shouldReturnContentDispositionHeader_withCorrectFilename() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(1L);
            when(reportService.generateComplianceCsv(1L)).thenReturn(EXCEL_CONTENT);

            // When / Then
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=compliance_report.xlsx"));
        }

        @Test
        @DisplayName("Should return Excel content type in response")
        void shouldReturnExcelContentType_inResponse() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(1L);
            when(reportService.generateComplianceCsv(1L)).thenReturn(EXCEL_CONTENT);

            // When / Then
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        }

        @Test
        @DisplayName("Should extract token from Authorization header removing Bearer prefix")
        void shouldExtractToken_fromAuthorizationHeaderRemovingBearerPrefix() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(1L);
            when(reportService.generateComplianceCsv(1L)).thenReturn(EXCEL_CONTENT);

            // When
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk());

            // Then — verifica que se extrae el token SIN el prefijo "Bearer "
            verify(jwtUtil, times(1)).extractCompanyId(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should call reportService with companyId extracted from token")
        void shouldCallReportService_withCompanyIdExtractedFromToken() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(5L);
            when(reportService.generateComplianceCsv(5L)).thenReturn(EXCEL_CONTENT);

            // When
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk());

            // Then
            verify(jwtUtil, times(1)).extractCompanyId(VALID_TOKEN);
            verify(reportService, times(1)).generateComplianceCsv(5L);
        }

        @Test
        @DisplayName("Should return empty byte array when report has no data")
        void shouldReturnEmptyByteArray_whenReportHasNoData() throws Exception {
            // Given
            when(jwtUtil.extractCompanyId(VALID_TOKEN)).thenReturn(1L);
            when(reportService.generateComplianceCsv(1L)).thenReturn(new byte[0]);

            // When / Then
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", BEARER_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(new byte[0]));
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when Authorization header is missing")
        void shouldReturn401_whenAuthorizationHeaderIsMissing() throws Exception {
            // When / Then
            mockMvc.perform(get("/api/reports/export"))
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).extractCompanyId(any());
            verify(reportService, never()).generateComplianceCsv(any());
        }

        @Test
        @DisplayName("Should return 401 UNAUTHORIZED when Authorization header has no Bearer prefix")
        void shouldReturn401_whenAuthorizationHeaderHasNoBearerPrefix() throws Exception {
            // When / Then
            mockMvc.perform(get("/api/reports/export")
                            .header("Authorization", VALID_TOKEN)) // sin "Bearer "
                    .andExpect(status().isUnauthorized());

            verify(jwtUtil, never()).extractCompanyId(any());
            verify(reportService, never()).generateComplianceCsv(any());
        }
    }
}