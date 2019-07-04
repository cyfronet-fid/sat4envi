package pl.cyfronet.s4e;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.val;

import java.util.function.Supplier;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;

public class GreenMailSupplier implements Supplier<GreenMail> {
    @Override
    public GreenMail get() {
        val greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.withConfiguration(aConfig().withDisabledAuthentication());
        return greenMail;
    }
}
