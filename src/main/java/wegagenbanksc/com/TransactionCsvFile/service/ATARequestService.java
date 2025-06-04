package wegagenbanksc.com.TransactionCsvFile.service;

import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wegagenbanksc.com.TransactionCsvFile.DTO.FilteredDataForATARequestDTO;
import wegagenbanksc.com.TransactionCsvFile.repository.primary.AllInOneRepo;
import wegagenbanksc.com.TransactionCsvFile.repository.primary.CsvTransactionRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ATARequestService {

    private final AllInOneRepo allInOneRepo;
    private final CsvTransactionRepository csvTransactionRepository;

    @Value("${wegaServiceUrl}")
    private String serviceURLFromEnv;

    @Value("${userId}")
    private String userId;

    @Autowired
    public ATARequestService(AllInOneRepo allInOneRepo, CsvTransactionRepository csvTransactionRepository) {
        this.allInOneRepo = allInOneRepo;
        this.csvTransactionRepository = csvTransactionRepository;
    }

    public String requestXMLCreator(FilteredDataForATARequestDTO dto) {
        final String SOURCE = "USSD";
        final String UBSCOMP = "FCUBS";
        final String MODULE_ID = "RT";
        final String SERVICE = "FCUBSRTService";
        final String OPERATION = "CreateTransaction";
        final String PRD = "EBWW";
        final String CCY = "ETB";
        final String BRANCH_CODE = "002";

        LocalDate currentDate = LocalDate.now();
        String formattedDate = formatDate(currentDate);

        String debitBranch = allInOneRepo.getBranch(dto.getDebitAccount());
        String creditBranch = allInOneRepo.getBranch(dto.getCreditAccount());

        return new StringBuilder()
                .append("<soapenv:Envelope ")
                .append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
                .append("xmlns:fcub=\"http://fcubs.ofss.com/service/FCUBSRTService\">\n")
                .append("  <soapenv:Header />\n")
                .append("  <soapenv:Body>\n")
                .append("    <fcub:CREATETRANSACTION_FSFS_REQ>\n")
                .append("      <fcub:FCUBS_HEADER>\n")
                .append("        <fcub:SOURCE>").append(SOURCE).append("</fcub:SOURCE>\n")
                .append("        <fcub:UBSCOMP>").append(UBSCOMP).append("</fcub:UBSCOMP>\n")
                .append("        <fcub:USERID>").append(userId).append("</fcub:USERID>\n")
                .append("        <fcub:BRANCH>").append(BRANCH_CODE).append("</fcub:BRANCH>\n")
                .append("        <fcub:MODULEID>").append(MODULE_ID).append("</fcub:MODULEID>\n")
                .append("        <fcub:SERVICE>").append(SERVICE).append("</fcub:SERVICE>\n")
                .append("        <fcub:OPERATION>").append(OPERATION).append("</fcub:OPERATION>\n")
                .append("      </fcub:FCUBS_HEADER>\n")
                .append("      <fcub:FCUBS_BODY>\n")
                .append("        <fcub:Transaction-Details>\n")
                .append("          <fcub:XREF>").append(dto.getTransactionId()).append("</fcub:XREF>\n")
                .append("          <fcub:PRD>").append(PRD).append("</fcub:PRD>\n")
                .append("          <fcub:BRN>").append(debitBranch).append("</fcub:BRN>\n")
                .append("          <fcub:TXNACC>").append(dto.getDebitAccount()).append("</fcub:TXNACC>\n")
                .append("          <fcub:TXNCCY>").append(CCY).append("</fcub:TXNCCY>\n")
                .append("          <fcub:TXNAMT>").append(dto.getAmount()).append("</fcub:TXNAMT>\n")
                .append("          <fcub:OFFSETBRN>").append(creditBranch).append("</fcub:OFFSETBRN>\n")
                .append("          <fcub:OFFSETACC>").append(dto.getCreditAccount()).append("</fcub:OFFSETACC>\n")
                .append("          <fcub:OFFSETCCY>").append(CCY).append("</fcub:OFFSETCCY>\n")
                .append("          <fcub:OFFSETAMT>").append(dto.getAmount()).append("</fcub:OFFSETAMT>\n")
                .append("          <fcub:TXNDATE>").append(formattedDate).append("</fcub:TXNDATE>\n")
                .append("          <fcub:VALDATE>").append(formattedDate).append("</fcub:VALDATE>\n")
                .append("          <fcub:NARRATIVE>").append(dto.getDescription()).append("</fcub:NARRATIVE>\n")
                .append("        </fcub:Transaction-Details>\n")
                .append("      </fcub:FCUBS_BODY>\n")
                .append("    </fcub:CREATETRANSACTION_FSFS_REQ>\n")
                .append("  </soapenv:Body>\n")
                .append("</soapenv:Envelope>")
                .toString();
    }

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public String sendSoapRequest(String soapXML) throws IOException {
        URL url = new URL(serviceURLFromEnv);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        connection.setRequestProperty("SOAPAction", "http://fcubs.ofss.com/service/FCUBSRTService/CreateTransaction");
        connection.setDoOutput(true);

        connection.getOutputStream().write(soapXML.getBytes());

        int status = connection.getResponseCode();
        InputStream inputStream;

        if (status >= 200 && status < 300) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
            if (inputStream == null) throw new IOException("Received HTTP error: " + status);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();

        String response = outputStream.toString("UTF-8");

        System.out.println("📥 SOAP Response:");
        System.out.println(response);

        return response;
    }

    public String convertSoapToJSON(String soapXml) {
        JSONObject jsonObj = XML.toJSONObject(soapXml);
        return jsonObj.toString(2); // Pretty print with indent
    }

    public void processPendingRecords() {
        List<FilteredDataForATARequestDTO> pendingRecords = csvTransactionRepository.getPendingATARequests();

        for (FilteredDataForATARequestDTO dto : pendingRecords) {
            try {
                String soapRequest = requestXMLCreator(dto);
                System.out.println("✅ soapRequest for  " + soapRequest + " processed.");
                String soapResponse = sendSoapRequest(soapRequest);
                System.out.println("✅ Response for soaprequest  " + soapResponse + " processed.");
                String json = convertSoapToJSON(soapResponse);

                System.out.println("✅ Transaction " + dto.getTransactionId() + " processed.");
                System.out.println("🔁 JSON Response: " + json);

                csvTransactionRepository.markAsProcessed(dto.getTransactionId());

            } catch (Exception e) {
                System.err.println("❌ Failed to process transaction ID: " + dto.getTransactionId());
                e.printStackTrace();
            }
        }
    }
}
