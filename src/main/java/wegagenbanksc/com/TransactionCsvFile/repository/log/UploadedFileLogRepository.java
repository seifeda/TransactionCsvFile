package wegagenbanksc.com.TransactionCsvFile.repository.log;
import org.springframework.data.jpa.repository.JpaRepository;
import wegagenbanksc.com.TransactionCsvFile.model.log.UploadedFileLog;

public interface UploadedFileLogRepository extends JpaRepository<UploadedFileLog, Long> {
}
