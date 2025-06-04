package wegagenbanksc.com.TransactionCsvFile.repository.primary;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import wegagenbanksc.com.TransactionCsvFile.DTO.FilteredDataForATARequestDTO;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class AllInOneRepo {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AllInOneRepo(@Qualifier("dataSource") DataSource firstDataSource) {
        this.jdbcTemplate = new JdbcTemplate(firstDataSource);
    }


    public String getBranch(String accNumber) {
        String sql = "SELECT BRANCH_CODE FROM p1fchwgbm.sttms_cust_account WHERE cust_ac_no = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, accNumber);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }





}
