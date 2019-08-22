package org.incredible.certProcessor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.incredible.builders.*;
import org.incredible.certProcessor.signature.SignatureHelper;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.ob.Criteria;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.incredible.pojos.ob.SignedVerification;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CertificateFactory {

    private static String uuid;

    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);


    private ObjectMapper mapper = new ObjectMapper();

    public CertificateExtension createCertificate(CertModel certModel, Map<String, String> properties)
            throws InvalidDateFormatException, SignatureException.UnreachableException, IOException, SignatureException.CreationException {

        uuid = properties.get(JsonKey.DOMAIN_URL).concat("/") + properties.get(JsonKey.SLUG).concat("/")
                + properties.get(JsonKey.ROOT_ORG_ID).concat("/") + properties.get(JsonKey.TAG).concat("/") + UUID.randomUUID().toString() + ".json";

        CertificateExtensionBuilder certificateExtensionBuilder = new CertificateExtensionBuilder(properties.get(JsonKey.CONTEXT));
        CompositeIdentityObjectBuilder compositeIdentityObjectBuilder = new CompositeIdentityObjectBuilder(properties.get(JsonKey.CONTEXT));
        BadgeClassBuilder badgeClassBuilder = new BadgeClassBuilder(properties.get(JsonKey.CONTEXT));
        IssuerBuilder issuerBuilder = new IssuerBuilder(properties.get(JsonKey.CONTEXT));
        SignedVerification signedVerification = new SignedVerification();
        SignatureBuilder signatureBuilder = new SignatureBuilder();

        Criteria criteria = new Criteria();
        criteria.setId(properties.get(JsonKey.DOMAIN_URL).concat("/") + properties.get(JsonKey.SLUG).concat("/") + properties.get(JsonKey.ROOT_ORG_ID).concat("/")
                + properties.get(JsonKey.TAG));
        criteria.setNarrative(certModel.getCertificateDescription());

        /**
         *  recipient object
         *  **/
        compositeIdentityObjectBuilder.setName(certModel.getRecipientName()).setId(certModel.getIdentifier())
                .setHashed(false).
                setType(new String[]{JsonKey.ID});


        issuerBuilder.setId(properties.get(JsonKey.ISSUER_URL)).setName(certModel.getIssuer().getName())
                .setUrl(certModel.getIssuer().getUrl()).setPublicKey(certModel.getIssuer().getPublicKey());

        /**
         * badge class object
         * **/

        badgeClassBuilder.setName(certModel.getCourseName()).setDescription(certModel.getCertificateDescription())
                .setId(properties.get(JsonKey.BADGE_URL)).setCriteria(criteria)
                .setImage(certModel.getCertificateLogo()).
                setIssuer(issuerBuilder.build());


        /**
         *
         * Certificate extension object
         */
        certificateExtensionBuilder.setId(uuid).setRecipient(compositeIdentityObjectBuilder.build())
                .setBadge(badgeClassBuilder.build())
                .setIssuedOn(certModel.getIssuedDate()).setExpires(certModel.getExpiry())
                .setValidFrom(certModel.getValidFrom()).setVerification(signedVerification).setSignatory(certModel.getSignatoryList());

        if (StringUtils.isEmpty(properties.get(JsonKey.KEY_ID))) {
            signedVerification.setType(new String[]{JsonKey.HOSTED});
            logger.info("CertificateExtension:createCertificate: if keyID is empty then verification type is HOSTED");
        } else {
            signedVerification.setCreator(properties.get(JsonKey.PUBLIC_KEY_URL));
            logger.info("CertificateExtension:createCertificate: if keyID is not empty then verification type is SignedBadge");

            /** certificate  signature value **/
            String signatureValue = getSignatureValue(certificateExtensionBuilder.build(), properties, properties.get(JsonKey.KEY_ID));

            /**
             * to assign signature value
             */
            signatureBuilder.setCreated(Instant.now().toString()).setCreator(properties.get(JsonKey.SIGN_CREATOR))
                    .setSignatureValue(signatureValue);

            certificateExtensionBuilder.setSignature(signatureBuilder.build());
        }

        logger.info("CertificateFactory:createCertificate:certificate extension => {}", certificateExtensionBuilder.build());
        return certificateExtensionBuilder.build();
    }


    /**
     * to verifySignature signature value
     *
     * @param certificate
     * @param signatureValue
     * @param properties
     * @return
     */
    public boolean verifySignature(CertificateExtension certificate, String signatureValue, Map<String, String> properties) {
        boolean isValid = false;
        SignatureHelper signatureHelper = new SignatureHelper(properties);
        try {
            Map signReq = new HashMap<String, Object>();
            signReq.put(JsonKey.CLAIM, certificate);
            signReq.put(JsonKey.SIGNATURE_VALUE, signatureValue);
            signReq.put(JsonKey.KEY_ID, getKeyId(properties.get(JsonKey.SIGN_CREATOR)));
            JsonNode jsonNode = mapper.valueToTree(signReq);
            isValid = signatureHelper.verifySignature(jsonNode);
        } catch (SignatureException.UnreachableException | SignatureException.VerificationException e) {
            logger.error("exception while verifying Signature : {}", e.getMessage());
        }
        return isValid;
    }

    /**
     * to get signature value of certificate
     *
     * @param certificateExtension
     * @param properties
     * @return
     */
    private String getSignatureValue(CertificateExtension certificateExtension, Map<String, String> properties, String keyID) throws IOException, SignatureException.UnreachableException, SignatureException.CreationException {
        SignatureHelper signatureHelper = new SignatureHelper(properties);
        Map<String, Object> signMap;

            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String request = mapper.writeValueAsString(certificateExtension);
            JsonNode jsonNode = mapper.readTree(request);
            logger.info("CertificateFactory:getSignatureValue:Json node of certificate".concat(jsonNode.toString()));
            signMap = signatureHelper.generateSignature(jsonNode, keyID);
            return (String) signMap.get(JsonKey.SIGNATURE_VALUE);

    }

    /**
     * to get the KeyId from the sign_creator url , key id used for verifying signature
     *
     * @param creator
     * @return
     */
    private int getKeyId(String creator) {
        String idStr = null;
        try {
            URI uri = new URI(creator);
            String path = uri.getPath();
            idStr = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.debug("Exception while getting key id from the sign-creator url : {}", e.getMessage());
        }
        return Integer.parseInt(idStr);
    }

}


