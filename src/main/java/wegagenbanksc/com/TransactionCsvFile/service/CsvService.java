package wegagenbanksc.com.TransactionCsvFile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wegagenbanksc.com.TransactionCsvFile.model.TransactionRecord;
import wegagenbanksc.com.TransactionCsvFile.repository.primary.TransactionRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvService {

    @Autowired
    private TransactionRepository transactionRepository;

//    public void processCsvFile(InputStream inputStream) throws IOException {
//        int insertedCount = 0;
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            String line;
//            boolean isFirstLine = true;
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//
//            while ((line = reader.readLine()) != null) {
//                if (isFirstLine) {
//                    isFirstLine = false;
//                    continue; // Skip header
//                }
//
//                // Use the correct delimiter: "\t" for tab-separated, "," for comma-separated
//                String[] tokens = line.split(",");
//
//                if (tokens.length < 10) {
//                    System.err.println("Skipping malformed line: " + line);
//                    continue;
//                }
//
//                try {
//                    TransactionRecord record = new TransactionRecord();
//                    record.setTransactionId(tokens[0].trim());
//                    record.setTimeInitiated(LocalDateTime.parse(tokens[1].trim(), formatter));
//                    record.setReason(tokens[2].trim());
//                    record.setCurrency(tokens[3].trim());
//                    record.setAmount(Double.parseDouble(tokens[4].trim()));
//                    record.setAccountNumber(tokens[5].trim());
//                    record.setAccountHolderName(tokens[6].trim());
//                    record.setAccountBankName(tokens[7].trim());
//                    record.setAccountBankSwiftCode(tokens[8].trim());
//                    record.setCiyyMitAccountNumber(tokens[9].trim());
//                    record.setStatus("Pending");
//                    transactionRepository.save(record);
//                    insertedCount++;
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to parse or save line: " + line, e);
//                }
//            }
//
//            if (insertedCount == 0) {
//                throw new RuntimeException("No valid records found in CSV file.");
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to read CSV file", e);
//        }
//    }
public List<Map<String, String>> processCsvFile(InputStream inputStream) throws IOException {
    List<Map<String, String>> results = new ArrayList<>();
    Set<String> transactionIdsInFile = new HashSet<>();
    Set<String> duplicateTransactionIdsInFile = new HashSet<>();
    List<TransactionRecord> validRecords = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        boolean isFirstLine = true;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip header
            }

            String[] tokens = line.split(",");

            if (tokens.length < 10) {
                results.add(Map.of("transactionId", "UNKNOWN", "status", "failure", "reason", "Malformed line"));
                continue;
            }

            String transactionId = tokens[0].trim();
            String resultStatus = "successful";
            String reason = "";

            // Check duplicate in file
            if (!transactionIdsInFile.add(transactionId)) {
                duplicateTransactionIdsInFile.add(transactionId);
                resultStatus = "failure";
                reason = "Duplicate transactionId in file";
            }

            // Check existing in DB
            if (transactionRepository.existsByTransactionId(transactionId)) {
                resultStatus = "failure";
                reason = "Transaction ID already exists in database";
            }

            String accountNumber = tokens[5].trim();
            if (!accountNumber.matches("\\d{13}")) {
                resultStatus = "failure";
                reason = "Invalid account number (must be 13 digits)";
            }

            String currency = tokens[3].trim();
            if (!currency.equalsIgnoreCase("ETB")) {
                resultStatus = "failure";
                reason = "Invalid currency (must be ETB)";
            }

            double amount;
            try {
                amount = Double.parseDouble(tokens[4].trim());
                if (amount <= 0) {
                    resultStatus = "failure";
                    reason = "Amount must be greater than 0";
                }
            } catch (NumberFormatException e) {
                resultStatus = "failure";
                reason = "Invalid amount";
                amount = 0;
            }

            Map<String, String> result = new HashMap<>();
            result.put("transactionId", transactionId);
            result.put("status", resultStatus);
            result.put("reason", reason);

            if (resultStatus.equals("successful")) {
                try {
                    TransactionRecord record = new TransactionRecord();
                    record.setTransactionId(transactionId);
                    record.setTimeInitiated(LocalDateTime.parse(tokens[1].trim(), formatter));
                    record.setReason(tokens[2].trim());
                    record.setCurrency(currency);
                    record.setAmount(amount);
                    record.setAccountNumber(accountNumber);
                    record.setAccountHolderName(tokens[6].trim());
                    record.setAccountBankName(tokens[7].trim());
                    record.setAccountBankSwiftCode(tokens[8].trim());
                    record.setCiyyMitAccountNumber(tokens[9].trim());
                    record.setStatus("Pending");

                    validRecords.add(record);
                } catch (Exception e) {
                    result.put("status", "failure");
                    result.put("reason", "Error parsing record: " + e.getMessage());
                }
            }

            results.add(result);
        }

        // Save all valid records at once
        if (!validRecords.isEmpty()) {
            transactionRepository.saveAll(validRecords);
        }

    } catch (IOException e) {
        throw new RuntimeException("Failed to read CSV file", e);
    }

    return results;
}


}
