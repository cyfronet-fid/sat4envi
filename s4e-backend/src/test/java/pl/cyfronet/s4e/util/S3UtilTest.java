package pl.cyfronet.s4e.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@BasicTest
@Slf4j
public class S3UtilTest {

    private static String WEBHOOK_KEY = "s4e-test-1/test/201810042345_Merkator_WV-IR.tif";

    @Autowired
    private S3Util s3Util;

    @Test
    public void shouldReturnTimeStampFromWebhookKey() {
        LocalDateTime dateFromUtil = s3Util.getTimeStamp(WEBHOOK_KEY);
        LocalDateTime date = LocalDateTime.of(2018,10 , 4, 23,45);

        assertThat(dateFromUtil, is(equalTo(date)));
    }

    @Test
    public void shouldReturnProductFromWebhookKey() {
        String productFromWebhook = s3Util.getProduct(WEBHOOK_KEY);
        String product = "WV-IR";

        assertThat(productFromWebhook, is(equalTo(product)));
    }

    @Test
    public void shouldReturnLayerNameFromWebhookKey() {
        String layerNameFromWebhook = s3Util.getLayerName(WEBHOOK_KEY);
        String layerName = "test/201810042345_Merkator_WV-IR";

        assertThat(layerNameFromWebhook, is(equalTo(layerName)));
    }

    @Test
    public void shouldReturnS3PathFromWebhookKey() {
        String pathFromWebhook = s3Util.getS3Path(WEBHOOK_KEY);
        String path = "test/201810042345_Merkator_WV-IR.tif";

        assertThat(pathFromWebhook, is(equalTo(path)));
    }
}
