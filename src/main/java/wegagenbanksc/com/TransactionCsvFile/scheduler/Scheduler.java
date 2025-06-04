package wegagenbanksc.com.TransactionCsvFile.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import wegagenbanksc.com.TransactionCsvFile.service.ATARequestService;

@Configuration
@EnableScheduling
public class Scheduler {
    @Autowired
    private ATARequestService ataRequestService;
    @Scheduled(fixedRate = 60000)
    public void run() {
        ataRequestService.processPendingRecords();
    }
}
