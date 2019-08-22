package org.incredible.certProcessor.signature;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;


public class SignatureHelper {

    private Map<String, String> properties;


    public SignatureHelper(Map<String, String> properties) {
        this.properties = properties;
    }

    private ObjectMapper mapper = new ObjectMapper();


    private static Logger logger = LoggerFactory.getLogger(SignatureHelper.class);


    /**
     * This method calls signature service for signing the object
     *
     * @param rootNode - contains input need to be signed
     * @return - signed data with key
     * @throws SignatureException.UnreachableException
     * @throws SignatureException.CreationException
     */
    public Map<String, Object> generateSignature(JsonNode rootNode, String keyId)
            throws SignatureException.UnreachableException, SignatureException.CreationException {
        Map signReq = new HashMap<String, Object>();
        signReq.put(JsonKey.ENTITY, rootNode);
        CloseableHttpClient client = HttpClients.createDefault();
        logger.info("SignatureHelper:generateSignature:keyID:".concat(keyId));
        String encServiceUrl=properties.get(JsonKey.SIGN_URL).concat("/").concat(keyId);
        logger.info("SignatureHelper:generateSignature:enc service url formed:".concat(encServiceUrl));
        HttpPost httpPost = new HttpPost(encServiceUrl);
        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(signReq));
            logger.info("SignatureHelper:generateSignature:SignRequest for enc-service call:".concat(mapper.writeValueAsString(signReq)));
            httpPost.setEntity(entity);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            CloseableHttpResponse response = client.execute(httpPost);
            return mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException when signing: {}", e.getMessage());
            throw new SignatureException().new UnreachableException(e.getMessage());
        } catch (IOException e) {
            logger.error("RestClientException when signing: {}", e.getMessage());
            throw new SignatureException().new CreationException(e.getMessage());

        }

    }


    public boolean verifySignature(JsonNode rootNode)
            throws SignatureException.UnreachableException, SignatureException.VerificationException {
        logger.debug("verify method starts with value {}", rootNode);
        Map signReq = new HashMap<String, Object>();
        signReq.put(JsonKey.ENTITY, rootNode);
        boolean result = false;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(properties.get(JsonKey.SIGN_VERIFY_URL));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(signReq));
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            result = mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<Boolean>() {
                    });

        } catch (ClientProtocolException ex) {
            logger.error("ClientProtocolException when verifying: ", ex);
            throw new SignatureException().new UnreachableException(ex.getMessage());
        } catch (Exception e) {
            logger.error("Exception occurred  while verifying signature: ", e);
            throw new SignatureException().new VerificationException(e.getMessage());
        }
        logger.debug("verify method ends with value {}", result);
        return result;
    }

}