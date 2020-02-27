package pl.cyfronet.s4e.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class S3Util {
    /**
     *
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_product.extension
     * @return LocalDateTime made from timestamp
     */
    public LocalDateTime getTimeStamp(String key) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return  LocalDateTime.parse((key.replaceAll(".*/","").split("_"))[0], formatter);
    }

    /**
     *
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_product.extension
     * @return part of filename that represents product
     */
    public String getProduct(String key){
        return key.replaceAll(".*_","").replaceAll("\\..*","");
    }

    /**
     *
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_product.extension
     * @return part of key: path/to/file/filename.ext
     */
    public String getS3Path(String key){
        return key.substring(key.indexOf("/")+1);
    }

    /**
     *
     * @param endpoint
     * @param bucket
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_product.extension
     * @return part of key with prepended endpoint and bucket: mailto://bucket/path/to/file/filename.ext
     */
    public String getGranulePath(String endpoint, String bucket, String key) {
        return endpoint + "://" + bucket + "/" + key.substring(key.indexOf("/")+1);
    }
}
