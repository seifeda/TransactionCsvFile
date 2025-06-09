package wegagenbanksc.com.TransactionCsvFile.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class UploadResponseDto {
    private String uploadStatus;
    private int totalRecords;
    private int validRecords;
    private int invalidRecords;
    private List<UploadResultDto> uploadSummary;
    private List<ProcessingResultDto> processingSummary;

    // Getters & Setters
}
