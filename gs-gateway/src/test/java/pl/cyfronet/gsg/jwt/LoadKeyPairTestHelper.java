package pl.cyfronet.gsg.jwt;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class LoadKeyPairTestHelper {
    private static final String keyStore = "dev_key.p12";
    private static final char[] keyStorePassword = "dev_password".toCharArray();
    private static final String keyAlias = "1";
    private static final char[] keyPassword = "dev_password".toCharArray();

    public static KeyPair loadKeyPair() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
        File keystoreFile = new File(keyStore);

        final KeyStore store = KeyStore.getInstance(keystoreFile, keyStorePassword);
        Key key = store.getKey(keyAlias, keyPassword);
        if (!(key instanceof PrivateKey)) {
            throw new IllegalStateException("Provided keystore doesn't contain a private key, keyStore=" + keyStore);
        }
        Certificate cert = store.getCertificate(keyAlias);
        return new KeyPair(cert.getPublicKey(), (PrivateKey) key);
    }
}
