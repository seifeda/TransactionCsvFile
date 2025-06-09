package wegagenbanksc.com.TransactionCsvFile.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessingResultDto {
    private String transactionId;
    private String status; // SUCCESS / FAILED
    private String message;
    private String webServiceResponse;

    public ProcessingResultDto(String transactionId, String status, String message, String webServiceResponse) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.webServiceResponse = webServiceResponse;
    }

    // Getters & Setters
}