package wegagenbanksc.com.TransactionCsvFile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegagenbanksc.com.TransactionCsvFile.service.CsvService;

import java.io.IOException;

@RestController
@RequestMapping("/api/csv")
public class TransactionController {

    @Autowired
    private CsvService csvService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("CSV file is required");
        }

        try {
            csvService.processCsvFile(file.getInputStream());
            return ResponseEntity.ok("CSV transaction processed successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to read file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error processing CSV: " + e.getMessage());
        }
    }
}
