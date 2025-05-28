package wegagenbanksc.com.TransactionCsvFile.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wegagenbanksc.com.TransactionCsvFile.model.TransactionRecord;
import wegagenbanksc.com.TransactionCsvFile.repository.TransactionRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CsvService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void processCsvFile(InputStream inputStream) throws IOException {
        int insertedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isFirstLine = true;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                // Use the correct delimiter: "\t" for tab-separated, "," for comma-separated
                String[] tokens = line.split(",");

                if (tokens.length < 10) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                try {
                    TransactionRecord record = new TransactionRecord();
                    record.setTransactionId(tokens[0].trim());
                    record.setTimeInitiated(LocalDateTime.parse(tokens[1].trim(), formatter));
                    record.setReason(tokens[2].trim());
                    record.setCurrency(tokens[3].trim());
                    record.setAmount(Double.parseDouble(tokens[4].trim()));
                    String accountNumber = tokens[5].trim();
                    if (accountNumber.length() < 13) {
                        try {
                            accountNumber = String.format("%013d", Long.parseLong(accountNumber));
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid account number format: " + accountNumber, e);
                        }
                    }
                    record.setAccountNumber(accountNumber);
                    record.setAccountHolderName(tokens[6].trim());
                    record.setAccountBankName(tokens[7].trim());
                    record.setAccountBankSwiftCode(tokens[8].trim());
                    record.setCiyyMitAccountNumber(tokens[9].trim());
                    record.setStatus("Pending");
                    transactionRepository.save(record);
                    insertedCount++;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse or save line: " + line, e);
                }
            }

            if (insertedCount == 0) {
                throw new RuntimeException("No valid records found in CSV file.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
    }

}
