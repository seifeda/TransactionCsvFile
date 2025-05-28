package wegagenbanksc.com.TransactionCsvFile.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wegagenbanksc.com.TransactionCsvFile.model.TransactionRecord;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, String> {
}


