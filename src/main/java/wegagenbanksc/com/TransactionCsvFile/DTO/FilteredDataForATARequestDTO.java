package wegagenbanksc.com.TransactionCsvFile.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FilteredDataForATARequestDTO {

    private String transactionId;
    private String debitAccount;
    private String creditAccount;
    private Double  amount;
    private String description;

    public FilteredDataForATARequestDTO(String transactionId, String debitAccount,
                                        String creditAccount, Double  amount, String description) {
        this.transactionId = transactionId;
        this.debitAccount = debitAccount;
        this.creditAccount = creditAccount;
        this.amount = amount;
        this.description = description;
    }
}
