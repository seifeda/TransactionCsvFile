package wegagenbanksc.com.TransactionCsvFile.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import wegagenbanksc.com.TransactionCsvFile.DTO.ProcessingResultDto;
import wegagenbanksc.com.TransactionCsvFile.DTO.UploadResponseDto;
import wegagenbanksc.com.TransactionCsvFile.DTO.UploadResultDto;
import wegagenbanksc.com.TransactionCsvFile.model.log.UploadedFileLog;
import wegagenbanksc.com.TransactionCsvFile.repository.log.UploadedFileLogRepository;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsvUploadService {

    private final UploadedFileLogRepository fileLogRepository;
    private final CsvService csvService;
    private final ATARequestService ataRequestService;


public UploadResponseDto processAndMergeCsv(MultipartFile file) throws IOException {
    logUploadedFile(file);

    List<Map<String, String>> validationResults = csvService.processCsvFile(file.getInputStream());
    List<Map<String, String>> soapResults = ataRequestService.processPendingRecords();

    int totalRecords = validationResults.size();
    int validRecords = (int) validationResults.stream().filter(r -> "successful".equalsIgnoreCase(r.get("status"))).count();
    int invalidRecords = totalRecords - validRecords;

    List<UploadResultDto> uploadSummary = validationResults.stream()
            .map(r -> new UploadResultDto(
                    r.getOrDefault("transactionId", "UNKNOWN"),
                    "successful".equalsIgnoreCase(r.get("status")) ? "VALID" : "INVALID",
                    r.getOrDefault("reason", "No error")
            )).collect(Collectors.toList());

    List<ProcessingResultDto> processingSummary = soapResults.stream()
            .map(r -> new ProcessingResultDto(
                    r.getOrDefault("transactionId", "UNKNOWN"),
                    "success".equalsIgnoreCase(r.get("status")) ? "SUCCESS" : "FAILED",
                    r.getOrDefault("message", "No message"),
                    r.getOrDefault("soapResponse", "")
            )).collect(Collectors.toList());

    UploadResponseDto responseDto = new UploadResponseDto();
    responseDto.setUploadStatus("success");
    responseDto.setTotalRecords(totalRecords);
    responseDto.setValidRecords(validRecords);
    responseDto.setInvalidRecords(invalidRecords);
    responseDto.setUploadSummary(uploadSummary);
    responseDto.setProcessingSummary(processingSummary);

    return responseDto;
}



    private void logUploadedFile(MultipartFile file) throws IOException {
        UploadedFileLog fileLog = new UploadedFileLog();
        fileLog.setFileName(file.getOriginalFilename());
        fileLog.setContentType(file.getContentType());
        fileLog.setSize(file.getSize());
        fileLog.setData(file.getBytes());
        fileLog.setUploadTime(LocalDateTime.now());
        fileLogRepository.save(fileLog);
    }

    private byte[] generateMergedCsv(List<Map<String, String>> validationResults,
                                     List<Map<String, String>> soapResults) throws IOException {

        Map<String, Map<String, String>> merged = new LinkedHashMap<>();

        // Merge validation results
        for (Map<String, String> row : validationResults) {
            String txnId = row.get("transactionId");
            Map<String, String> data = new HashMap<>();
            data.put("transactionId", txnId);
            data.put("validationStatus", row.getOrDefault("status", ""));
            data.put("validationMessage", row.getOrDefault("message", row.getOrDefault("reason", "")));
            merged.put(txnId, data);
        }

        // Merge SOAP results
        for (Map<String, String> row : soapResults) {
            String txnId = row.get("transactionId");
            Map<String, String> data = merged.getOrDefault(txnId, new HashMap<>());
            data.put("transactionId", txnId);
            data.put("TransactionStatus", row.getOrDefault("status", ""));
            data.put("TransactionMessage", row.getOrDefault("message", ""));
            merged.put(txnId, data);
        }

        // Create CSV
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("transactionId", "validationStatus", "validationMessage", "TransactionStatus", "TransactionMessage"));

        for (Map<String, String> row : merged.values()) {
            printer.printRecord(
                    row.getOrDefault("transactionId", ""),
                    row.getOrDefault("validationStatus", ""),
                    row.getOrDefault("validationMessage", ""),
                    row.getOrDefault("TransactionStatus", ""),
                    row.getOrDefault("TransactionMessage", "")
            );
        }

        printer.flush();
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
