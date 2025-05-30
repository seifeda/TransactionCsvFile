package wegagenbanksc.com.TransactionCsvFile.model.log;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String contentType;
    private long size;

    @Lob
    private byte[] data;

    private LocalDateTime uploadTime;

    // Getters and Setters
}
