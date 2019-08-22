package org.incredible.certProcessor.store;


import org.incredible.UrlManager;
import org.sunbird.cloud.storage.BaseStorageService;
import scala.Option;

import java.io.File;

public class CloudStorage {

    private static BaseStorageService storageService = null;


    public CloudStorage(BaseStorageService storageService) {
        this.storageService = storageService;
    }


    public static String uploadFile(String container, String path, File file, boolean isDirectory,int retryCount) {
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount), Option.apply(1));
        return UrlManager.getSharableUrl(url,container);
         }
}
