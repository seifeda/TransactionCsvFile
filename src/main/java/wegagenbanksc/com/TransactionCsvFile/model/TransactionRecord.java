package wegagenbanksc.com.TransactionCsvFile.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "csv_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {
    @Id
    private String transactionId;
    private LocalDateTime timeInitiated;
    private String reason;
    private String currency;
    private Double amount;
    private String accountNumber;
    private String accountHolderName;
    private String accountBankName;
    private String accountBankSwiftCode;
    private String ciyyMitAccountNumber;
    private String status = "Pending";
}