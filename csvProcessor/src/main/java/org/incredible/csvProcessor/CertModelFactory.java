package org.incredible.csvProcessor;

import org.apache.commons.csv.CSVRecord;
import org.incredible.pojos.ob.Issuer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.incredible.certProcessor.CertModel;


public class CertModelFactory {
    private HashMap<String, String> csvProperties;
    private static Logger logger = LoggerFactory.getLogger(CertModelFactory.class);


    public CertModelFactory(HashMap<String, String> csvprop) {
        this.csvProperties = csvprop;
    }

    public CertModel create(CSVRecord csvRecord) {
        try {
            CertModel certModel = new CertModel();
            for (HashMap.Entry<String, String> member : csvProperties.entrySet()) {
                String methodName = "set" + member.getKey();
                Class params = getParamType(methodName);
                Method method = certModel.getClass().getMethod(methodName, params);
                method.setAccessible(true);
                if (!methodName.equals("setIssuer")) {
                    method.invoke(certModel, csvRecord.get(member.getValue()));

                } else {
                    Issuer issuer = new Issuer("context");
                    issuer.setName(csvRecord.get(member.getValue()));
                    method.invoke(certModel, issuer);
                }
            }
            return certModel;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            logger.error("Exception in creating certmodel" + e);
            return null;
        }
    }

    private Class getParamType(String methodName) {
        Method[] declaredMethods = CertModel.class.getDeclaredMethods();
        Class[] parameterTypes = new Class[0];
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals(methodName)) {
                parameterTypes = declaredMethod.getParameterTypes();
                break;
            }
        }
        if (parameterTypes.length == 0) {
            return null;

        } else
            return parameterTypes[0];
    }


}



