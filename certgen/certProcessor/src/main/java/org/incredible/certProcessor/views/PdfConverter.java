package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.util.FileUtil;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public static void convertor(File htmlSource, String id, String directory) {
        File file = new File(directory, id + ".pdf");
        try {
//            ConverterProperties converterProperties = new ConverterProperties();
//            PdfWriter pdfWriter = new PdfWriter(file);
//            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
//            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
//            HtmlConverter.convertToPdf(new FileInputStream(htmlSource),
//                    pdfDocument, converterProperties);
            HtmlConverter.convertToPdf(htmlSource, file);
            logger.info("Pdf file is created ");
        } catch (FileNotFoundException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
            e.printStackTrace();
        }
    }

}
