package org.incredible.pojos;

public class CertificateResponse {
    private String uuid;
    private String accessCode;
    private String jsonData;

    public CertificateResponse() {
    }

    public CertificateResponse(String uuid, String accessCode, String jsonData) {
        this.uuid = uuid;
        this.accessCode = accessCode;
        this.jsonData = jsonData;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
