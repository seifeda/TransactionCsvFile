package wegagenbanksc.com.TransactionCsvFile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegagenbanksc.com.TransactionCsvFile.DTO.UploadResponseDto;
import wegagenbanksc.com.TransactionCsvFile.service.CsvService;
import wegagenbanksc.com.TransactionCsvFile.service.CsvUploadService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/csv")
public class TransactionController {

    @Autowired
    private CsvService csvService;
    private final CsvUploadService csvUploadService;

    public TransactionController(CsvService csvService,CsvUploadService csvUploadService) {
        this.csvService = csvService;
        this.csvUploadService = csvUploadService;
    }
    @Operation(summary = "Upload CSV file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "CSV file is required"));
        }

        try {
            UploadResponseDto responseDto = csvUploadService.processAndMergeCsv(file);
            return ResponseEntity.ok(responseDto);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", "File read error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("status", "error", "message", "CSV processing error: " + e.getMessage()));
        }
    }




}
