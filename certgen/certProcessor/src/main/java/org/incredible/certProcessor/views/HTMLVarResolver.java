package org.incredible.certProcessor.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.incredible.pojos.CertificateExtension;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HTMLVarResolver {


    private CertificateExtension certificateExtension;

    public HTMLVarResolver(CertificateExtension certificateExtension) {
        this.certificateExtension = certificateExtension;
    }

    private ObjectMapper mapper = new ObjectMapper();

    public String getRecipient() {
        return certificateExtension.getRecipient().getName();
    }


    public String getCourse() {
        return certificateExtension.getBadge().getName();
    }


    public String getQrCodeImage() {
        return certificateExtension.getId().split("Certificate/")[1] + ".png";
    }


    public String getTitle() {
        return "certificate";
    }


    public String getDated() {
        return certificateExtension.getIssuedOn();
    }


    public String getDateInFormatOfWords() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateInFormat;
        try {
            Date parsedIssuedDate = simpleDateFormat.parse(certificateExtension.getIssuedOn());
            DateFormat format = new SimpleDateFormat("dd MMMM yyy");
            format.format(parsedIssuedDate);
            dateInFormat = format.format(parsedIssuedDate);
            return dateInFormat;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public String getSignatoryName() {
//        return certificateExtension.getSignatory()[1].getName();
//    }
//
//    public String getSignatory() {
//        return certificateExtension.getSignatory()[1].getId();
//    }

}
