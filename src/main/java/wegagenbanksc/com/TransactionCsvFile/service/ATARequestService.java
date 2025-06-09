package wegagenbanksc.com.TransactionCsvFile.service;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import wegagenbanksc.com.TransactionCsvFile.DTO.FilteredDataForATARequestDTO;
import wegagenbanksc.com.TransactionCsvFile.repository.primary.AllInOneRepo;
import wegagenbanksc.com.TransactionCsvFile.repository.primary.CsvTransactionRepository;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        final String PRD = "FTUS";
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
                .append("          <fcub:OFFSETBRN>").append(BRANCH_CODE).append("</fcub:OFFSETBRN>\n")
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


public Map<String, String> sendSoapRequest(String soapXML) throws IOException {
    String serviceUrl = serviceURLFromEnv;
    URL url = new URL(serviceUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    // Setup HTTP request
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
    connection.setRequestProperty("SOAPAction", serviceURLFromEnv);
    connection.setDoOutput(true);

    // Send SOAP request
    connection.getOutputStream().write(soapXML.getBytes());

    // Get SOAP response
    InputStream inputStream = connection.getInputStream();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
    }
    inputStream.close();

    String rawXml = outputStream.toString("UTF-8");
    System.out.println("Raw SOAP Response:\n" + rawXml);

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("rawXml", rawXml);

    try {
        // Use org.json.XML to parse XML
        JSONObject json = XML.toJSONObject(rawXml);

        JSONObject body = json.getJSONObject("S:Envelope").getJSONObject("S:Body");

        // Since the namespace is present, we must loop through keys to find the actual node
        String responseKey = null;
        for (String key : body.keySet()) {
            if (key.contains("CREATETRANSACTION_FSFS_RES")) {
                responseKey = key;
                break;
            }
        }

        if (responseKey == null) {
            resultMap.put("status", "FAILED");
            resultMap.put("message", "CREATETRANSACTION_FSFS_RES node not found in the response.");
            return resultMap;
        }

        JSONObject responseNode = body.getJSONObject(responseKey);
        JSONObject header = responseNode.getJSONObject("FCUBS_HEADER");
        String msgStat = header.getString("MSGSTAT");
        resultMap.put("status", msgStat);

        // Try to read error message if present
        if ("FAILURE".equalsIgnoreCase(msgStat)) {
            JSONObject bodyContent = responseNode.getJSONObject("FCUBS_BODY");

            if (bodyContent.has("FCUBS_ERROR_RESP")) {
                JSONObject errorResp = bodyContent.getJSONObject("FCUBS_ERROR_RESP");
                if (errorResp.has("ERROR")) {
                    JSONObject error = errorResp.getJSONObject("ERROR");
                    String eCode = error.optString("ECODE", "UNKNOWN");
                    String eDesc = error.optString("EDESC", "No description");
                    resultMap.put("message", "Error [" + eCode + "]: " + eDesc);
                } else {
                    resultMap.put("message", "Unknown failure: No ERROR block.");
                }
            } else {
                resultMap.put("message", "FAILURE with no error details.");
            }
        } else {
            resultMap.put("message", "Transaction processed successfully.");
        }

    } catch (Exception e) {
        resultMap.put("status", "FAILED");
        resultMap.put("message", "Exception while parsing response: " + e.getMessage());
    }

    return resultMap;
}

    public String convertSoapToJSON(String soapXml) {
        JSONObject jsonObj = XML.toJSONObject(soapXml);
        return jsonObj.toString(2); // Pretty print with indent
    }

    public List<Map<String, String>> processPendingRecords() {
        List<Map<String, String>> responseList = new ArrayList<>();
        List<FilteredDataForATARequestDTO> pendingRecords = csvTransactionRepository.getPendingATARequests();

        for (FilteredDataForATARequestDTO dto : pendingRecords) {
            Map<String, String> result = new HashMap<>();
            result.put("transactionId", dto.getTransactionId());

            try {
                String soapRequest = requestXMLCreator(dto);

                Map<String, String> soapResponse = sendSoapRequest(soapRequest);
                String rawXml = soapResponse.get("rawXml");

                System.out.println("Raw SOAP Response: \n" + rawXml);

                // Use XML parsing instead of converting to JSON
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(rawXml)));

                NodeList msgStatNodes = doc.getElementsByTagNameNS("*", "MSGSTAT");
                String msgStat = msgStatNodes.getLength() > 0 ? msgStatNodes.item(0).getTextContent() : "";

                if ("SUCCESS".equalsIgnoreCase(msgStat)) {
                    csvTransactionRepository.markAsProcessed(dto.getTransactionId());
                    result.put("status", "success");
                    result.put("message", "Transaction processed successfully.");
                } else {
                    result.put("status", "failure");

                    NodeList errorCodeNodes = doc.getElementsByTagNameNS("*", "ECODE");
                    NodeList errorDescNodes = doc.getElementsByTagNameNS("*", "EDESC");

                    if (errorCodeNodes.getLength() > 0 && errorDescNodes.getLength() > 0) {
                        String ecode = errorCodeNodes.item(0).getTextContent();
                        String edesc = errorDescNodes.item(0).getTextContent();
                        result.put("message", String.format("Error [%s]: %s", ecode, edesc));
                    } else {
                        result.put("message", "Transaction failed. No error detail found.");
                    }
                }

            } catch (Exception e) {
                result.put("status", "failure");
                result.put("message", "Exception: " + e.getMessage());
            }

            responseList.add(result);
        }

        return responseList;
    }


}
