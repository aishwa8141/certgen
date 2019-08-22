package org.incredible.certProcessor.store;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class StorageParams {


    private static BaseStorageService storageService = null;
    private Logger logger = Logger.getLogger(StorageParams.class);

    private static Map<String, String> properties;

    public StorageParams(Map<String, String> properties) {
        this.properties = properties;
    }

    public void init() {
        String cloudStoreType = properties.get(JsonKey.CLOUD_STORAGE_TYPE);
        if (StringUtils.equalsIgnoreCase(cloudStoreType, JsonKey.AZURE)) {
            String storageKey = properties.get(JsonKey.AZURE_STORAGE_KEY);
            String storageSecret = properties.get(JsonKey.AZURE_STORAGE_SECRET);
            StorageConfig storageConfig = new StorageConfig(cloudStoreType, storageKey, storageSecret);
            logger.info("StorageParams:init:all storage params initialized for azure block");
            storageService = StorageServiceFactory.getStorageService(storageConfig);
            } else if (StringUtils.equalsIgnoreCase(cloudStoreType,JsonKey.AWS)) {
                String storageKey = properties.get(JsonKey.AWS_STORAGE_KEY);
                String storageSecret = properties.get(JsonKey.AWS_STORAGE_SECRET);
                storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
                logger.info("StorageParams:init:all storage params initialized for aws block");

        } else {
            logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(cloudStoreType));
        }
    }

    public String upload(String path, File file, boolean isDirectory) {
        CloudStorage cloudStorage = new CloudStorage(storageService);
        int retryCount= Integer.parseInt(properties.get(JsonKey.CLOUD_UPLOAD_RETRY_COUNT));
        String containerName=properties.get(JsonKey.CONTAINER_NAME);
        logger.info("StorageParams:upload:container name got:"+containerName);
        return cloudStorage.uploadFile(containerName, path, file, isDirectory,retryCount);

    }
}
