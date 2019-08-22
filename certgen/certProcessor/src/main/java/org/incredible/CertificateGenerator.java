package org.incredible;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.apache.commons.lang.StringUtils;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.views.HTMLGenerator;
import org.incredible.certProcessor.views.HTMLTemplateProvider;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.CertificateResponse;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


public class CertificateGenerator {


    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    private Map<String, String> properties;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CertificateGenerator(Map<String, String> properties) {
        this.properties = properties;
    }

    private CertificateFactory certificateFactory = new CertificateFactory();


    public CertificateResponse createCertificate(CertModel certModel, HTMLTemplateProvider htmlTemplateProvider, String directory)
            throws Exception {
        String uuid = null;
        CertificateExtension certificateExtension = certificateFactory.createCertificate(certModel, properties);
        String jsonData = generateCertificateJson(certificateExtension, directory);
        String accessCode = generateQRCodeForCertificate(certificateExtension, directory);
        String htmlContent = htmlTemplateProvider.getTemplateContent(directory);
        if (htmlTemplateProvider.checkHtmlTemplateIsValid(htmlContent)) {
            HTMLGenerator htmlGenerator = new HTMLGenerator(htmlContent);
            htmlGenerator.generate(certificateExtension, directory);
            uuid = getUUID(certificateExtension.getId());
        } else return new CertificateResponse();
        return new CertificateResponse(uuid, accessCode, jsonData);
    }

    private String getUUID(String id) {
        try {
            URI uri = new URI(id);
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            return StringUtils.substringBefore(idStr, ".");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateCertificateJson(CertificateExtension certificateExtension, String directory) {
        checkDirectoryExists(directory);
        File file = new File(directory + getUUID(certificateExtension.getId()) + ".json");
        String jsonData = null;
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.writeValue(file, certificateExtension);
            jsonData = objectMapper.writeValueAsString(certificateExtension);
            logger.info("Json file has been generated for the certificate");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    private void checkDirectoryExists(String directory) {
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private String generateQRCodeForCertificate(CertificateExtension certificateExtension, String directory) throws WriterException,
            FontFormatException, NotFoundException, IOException {
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(Double.valueOf(properties.get(JsonKey.ACCESS_CODE_LENGTH)));
        String accessCode = accessCodeGenerator.generate();
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        qrCodeGenerationModel.setText(accessCode);
        qrCodeGenerationModel.setFileName(directory + getUUID(certificateExtension.getId()));
        qrCodeGenerationModel.setData(properties.get(JsonKey.DOMAIN_URL).concat("/") +
                properties.get(JsonKey.SLUG).concat("/") + getUUID(certificateExtension.getId()));
        QRCodeImageGenerator qrCodeImageGenerator = new QRCodeImageGenerator();
        File Qrcode = qrCodeImageGenerator.createQRImages(qrCodeGenerationModel);
        logger.info("Qrcode is created for the certificate");
        return accessCode;
    }
}