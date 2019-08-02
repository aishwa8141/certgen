package org.incredible.csvProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


import org.incredible.CertificateGenerator;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.incredible.certProcessor.signature.KeyGenerator;
import org.incredible.certProcessor.store.StorageParams;
import org.incredible.certProcessor.views.HTMLGenerator;
import org.incredible.certProcessor.views.HTMLTemplateFile;
import org.incredible.certProcessor.views.HTMLZipProcessor;
import org.incredible.certProcessor.views.PdfConverter;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Main {


    /**
     * List to CertModel
     **/

    private static ArrayList<CertModel> certModelsList = new ArrayList();

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * to get the file name
     **/

    private static String csvFileName = "input.csv";


    /**
     * csv file name
     **/

    private static String modelFileName = "CertModelMapper.json";

    private static final String templateName = "template.html";


    private static CSVReader csvReader = new CSVReader();

    private static Properties properties = readPropertiesFile();


    /**
     * mapper to read cert json mapper file
     **/

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * to store catv column name
     **/

    private static HashMap<String, String> csvProperties;


    private static final String domain = properties.getProperty("CONTEXT");
    private static final String contextFileName = "context.json";

    private static String context;

    private static final String WORK_DIR = "./";

    /**
     * The algorithm to use
     */
    private static final String RSA_ALGO = "RSA";

    /**
     * The public key file name
     */
    private static final String PUBLIC_KEY_FILENAME = WORK_DIR + "public.pub";

    /**
     * The private key file name
     */
    private static final String PRIVATE_KEY_FILENAME = WORK_DIR + "private.key";

    final static String resourceName = "application.properties";

    /**
     * The public key pair
     */
    private static KeyPair keyPair;

    /**
     * to get all the application properties
     */
    private static HashMap<String, String> property = new HashMap<>();

    private static ArrayList<CertificateExtension> listOfCertificate = new ArrayList<>();


    private static String getPath(String file) {
        String result = null;
        try {
            result = Main.class.getClassLoader().getResource(file).getFile();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            return result;
        }
    }

    /**
     * read csv file
     **/
    private static void readFile(String fileName) {
        try {
            String csvContent = new String(Files.readAllBytes(new File(getPath(fileName)).toPath()));
            csvProperties = mapper.readValue(csvContent, HashMap.class);
        } catch (IOException io) {
            logger.error("Input model  mapper file does not exits {}, {}", modelFileName, io.getMessage());

        }
    }

    private static void readCSV(String filename) {
        boolean isFileExits = csvReader.isExists(filename);
        if (isFileExits) {
            try {
                CSVParser csvParser = csvReader.readCsvFileRows(filename);
                setCertModelsList(csvParser);
            } catch (IOException io) {
                logger.error("CSV Parsing exception {}, {}", io.getMessage(), io.getStackTrace());
            }
        } else {
            logger.error("Input CSV file not found {}", csvFileName);
        }

        logger.info("Finished reading the csv file");
    }

    public static void main(String[] args) throws MalformedURLException {
        readFile(modelFileName);
        initializeKeys();
        readCSV(getPath(csvFileName));
        initContext();

        /**
         * iterate each inputmodel to generate certificate
         */

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            property.put(key, value);
        }

        CertificateFactory certificateFactory = new CertificateFactory();
//        URL url = new URL("http://127.0.0.1:8080/certificate_pdf.zip");
//        HTMLZipProcessor htmlZipProcessor = new HTMLZipProcessor(url);
//        System.out.println("hii "+htmlZipProcessor.getTemplateContent());


//        try {
//            URL url = new URL("http://127.0.0.1:8080/certificate_pdf.zip");
//            htmlZipProcessor.getZipFileFromURl(url, new File("certificate"));
//        } catch (IOException e) {
//
//        }

//        for (int row = 0; row < certModelsList.size(); row++) {
//            try {
//
//
//                CertificateExtension certificate = certificateFactory.createCertificate(certModelsList.get(row), context, property.get("VERIFICATION_TYPE"));
//                listOfCertificate.add(certificate);
////                File file = new File(certificate.getId().split("Certificate/")[1] + ".json");
////                mapper.writeValue(file, certificate);
////                String url = uploadFileToCloud(file);
////                System.out.println(url);
//                generateQRCode(certificate, certificate.getId() + ".json");
//                generateHtml(certificate);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.error("exception while creating certificates {}", e.getMessage());
//            }
//        }
        try {
            CertificateGenerator certificateGenerator = new CertificateGenerator(property);
            URL url = new URL("http://127.0.0.1:8080/certificate_pdf.zip");
            HTMLZipProcessor htmlZipProcessor = new HTMLZipProcessor(url);
            for (int row = 0; row < certModelsList.size(); row++) {
                String response = certificateGenerator.createCertificate(certModelsList.get(row), htmlZipProcessor);
                if (response == null) {
                    logger.error("certificate is not generated due to html template");
                } else
                    logger.info("certificate has been generated for the id {}", response);
            }
        } catch (InvalidDateFormatException e) {
            e.printStackTrace();
            logger.info("{}", e.getMessage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    /**
     * set each field to inputModel object
     **/

    public static CertModel getInputModel(CSVRecord csvRecord) {
        CertModelFactory certModelFactory = new CertModelFactory(csvProperties);
        CertModel model = certModelFactory.create(csvRecord);
        logger.info("csv row => {}", csvRecord);
        logger.info("Model created is => {}", model.toString());
        return model;
    }


    private static void setCertModelsList(CSVParser csvParser) {
        for (CSVRecord csvRecord : csvParser) {
            CertModel model = getInputModel(csvRecord);
            if (null == model) {
                logger.error("Cannot generate certificate for row {}. Invalid input", csvRecord.getRecordNumber());
            } else {
                certModelsList.add(model);
            }
        }
    }


    /**
     * to generateQRCode for certificate
     **/
    private static void generateQRCode(CertificateExtension certificateExtension, String url) {
        File Qrcode;
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(Double.valueOf(property.get("ACCESS_CODE_LENGTH")));
        qrCodeGenerationModel.setText(accessCodeGenerator.generate());
        qrCodeGenerationModel.setFileName(certificateExtension.getId().split("Certificate/")[1]);
        qrCodeGenerationModel.setData(url);
        QRCodeImageGenerator qrCodeImageGenerator = new QRCodeImageGenerator();
        try {
            Qrcode = qrCodeImageGenerator.createQRImages(qrCodeGenerationModel);

        } catch (IOException | WriterException | FontFormatException | NotFoundException e) {
            logger.info("Exception while generating QRcode {}", e.getMessage());
        }

    }

    /**
     * generate Html Template for certificate
     **/
    private static void generateHtml(CertificateExtension certificateExtension) throws Exception {

        String id = certificateExtension.getId().split("Certificate/")[1];
        HTMLTemplateFile htmlTemplateFile = new HTMLTemplateFile(templateName);
        HTMLGenerator htmlGenerator = new HTMLGenerator(htmlTemplateFile.getTemplateContent());
        if (htmlTemplateFile.checkHtmlTemplateIsValid(htmlTemplateFile.getTemplateContent())) {
            htmlGenerator.generate(certificateExtension);
            File file = new File(id + ".html");
//            uploadFileToCloud(file);
            convertHtmlToPdf(file, id);
        } else {
            throw new Exception("HTML template is not valid");
        }

    }


    private static void convertHtmlToPdf(File file, String id) {
        PdfConverter pdfConverter = new PdfConverter();
        pdfConverter.convertor(file, id);

    }

    private static void initContext() {
        try {
            ClassLoader classLoader = Main.class.getClassLoader();

            File file = new File(classLoader.getResource(contextFileName).getFile());
            if (file == null) {
                throw new IOException("Context file not found ");
            }
            context = domain + "/" + contextFileName;
            logger.info("Context file Found : {} ", file.exists());
        } catch (IOException e) {
            logger.info("Exception while initializing context {}", e.getMessage());
        }

    }


    /**
     * to upload file to cloud
     *
     * @param file file to be uploaded
     * @return url
     */

    private static String uploadFileToCloud(File file) {
        StorageParams storageParams = new StorageParams(property);
        storageParams.init();
        String url = storageParams.upload(property.get("CONTAINER_NAME"), "", file, false);
        return url;

    }


    private static KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = new KeyGenerator(RSA_ALGO, 2048);
        keyPair = keyGenerator.get();
        System.out.println(keyPair.getPublic().toString());
        return keyPair;
    }

    private String getPublicKey() throws IOException {
        return new KeyWriter().getPublicKey(keyPair.getPublic());
    }

    private static void initializeKeys() {

        try {
            keyPair = KeyLoader.load(RSA_ALGO, PUBLIC_KEY_FILENAME, PRIVATE_KEY_FILENAME);
            if (keyPair.getPrivate() == null ||
                    keyPair.getPublic() == null) {
                // Generate new key pairs
                keyPair = generateKeys();
                // Write it in current directory for next time
                new KeyWriter().write(keyPair, WORK_DIR);
            }
        } catch (NoSuchAlgorithmException algoException) {
            logger.info("No such algorithm. Refer https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html, {}", algoException.getMessage());
        } catch (IOException ioException) {
            logger.info(ioException.getCause() + "message : " + ioException.getMessage());
        }
    }

    public static Properties readPropertiesFile() {
        ClassLoader loader = CertificateFactory.class.getClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("Exception while reading application.properties {}", e.getMessage());
        }
        return properties;
    }

}
