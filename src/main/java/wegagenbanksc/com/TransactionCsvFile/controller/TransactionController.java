package wegagenbanksc.com.TransactionCsvFile.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegagenbanksc.com.TransactionCsvFile.model.log.UploadedFileLog;
import wegagenbanksc.com.TransactionCsvFile.repository.log.UploadedFileLogRepository;
import wegagenbanksc.com.TransactionCsvFile.service.CsvService;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/csv")
public class TransactionController {

    @Autowired
    private CsvService csvService;
    private final UploadedFileLogRepository fileLogRepository;

    public TransactionController(CsvService csvService, UploadedFileLogRepository fileLogRepository) {
        this.csvService = csvService;
        this.fileLogRepository = fileLogRepository;
    }
    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("CSV file is required");
        }

        try {
            // Save file log to DB
            UploadedFileLog fileLog = new UploadedFileLog();
            fileLog.setFileName(file.getOriginalFilename());
            fileLog.setContentType(file.getContentType());
            fileLog.setSize(file.getSize());
            fileLog.setData(file.getBytes());
            fileLog.setUploadTime(LocalDateTime.now());
            fileLogRepository.save(fileLog);

            csvService.processCsvFile(file.getInputStream());
            return ResponseEntity.ok("CSV transaction processed successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to read file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Error processing CSV: " + e.getMessage());
        }
    }
}
