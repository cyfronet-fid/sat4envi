package pl.cyfronet.s4e;

public final class Constants {
    public static final String API_PREFIX_V1 = "/api/v1";
    public static final String API_PREFIX_S3 = "/api/s3";
    public static final String GEOSERVER_PRG_DATA_STORE = "prg";
    public static final String GEOSERVER_PRG_PATH = "/opt/geoserver/prg/";
    /**
     * Date format used in the system.
     * <p>
     * <strong>Warning:</strong> it isn't set on the formatter itself. See
     * <a href="https://blog.codecentric.de/en/2017/08/parsing-of-localdate-query-parameters-in-spring-boot/">
     *     this blog post
     * </a> for a possible solution, however I couldn't get it to work in Spring Boot 2.1.4.
     * Setting <code>spring.(mvc|jackson).date-format</code> properties also don't have effect.
     */
    public static final String JACKSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
}
