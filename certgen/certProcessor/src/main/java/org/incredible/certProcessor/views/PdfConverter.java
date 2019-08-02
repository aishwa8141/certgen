package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import java.io.*;

public class PdfConverter {


    public static void convertor(File htmlSource, String id) {

        File file = new File(id + ".pdf");
        try {
            ConverterProperties converterProperties = new ConverterProperties();
            PdfWriter pdfWriter = new PdfWriter(file);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            HtmlConverter.convertToPdf(new FileInputStream(htmlSource),
                    pdfDocument, converterProperties);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
