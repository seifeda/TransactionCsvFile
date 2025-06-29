package wegagenbanksc.com.TransactionCsvFile.repository.primary;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wegagenbanksc.com.TransactionCsvFile.model.TransactionRecord;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, String> {
    boolean existsByTransactionId(String transactionId);
}
