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
     *  filename convention: timestamp_*_productType.extension
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
     *  filename convention: timestamp_*_productType.extension
     * @return part of filename that represents productType
     */
    public String getProductType(String key){
        return key.replaceAll(".*_","").replaceAll("\\..*","");
    }

    /**
     *
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_productType.extension
     * @return part of key: path/to/file/filename
     */
    public String getLayerName(String key){
        return key.substring(key.indexOf("/")+1).replaceAll("\\..*","");
    }

    /**
     *
     * @param key
     *  key should contain bucket/path/to/file/filename.ext
     *  filename convention: timestamp_*_productType.extension
     * @return part of key: path/to/file/filename.ext
     */
    public String getS3Path(String key){
        return key.substring(key.indexOf("/")+1);
    }
}
