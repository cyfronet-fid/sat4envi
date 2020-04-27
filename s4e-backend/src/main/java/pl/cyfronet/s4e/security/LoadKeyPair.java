package pl.cyfronet.s4e.security;

import pl.cyfronet.s4e.properties.JwtProperties;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class LoadKeyPair {
    public static KeyPair loadKeyPair(JwtProperties jwtProperties) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
        File keystoreFile = new File(jwtProperties.getKeyStore());
        char[] keyStorePassword = jwtProperties.getKeyStorePassword().toCharArray();
        String keyAlias = jwtProperties.getKeyAlias();
        char[] keyPassword = jwtProperties.getKeyPassword().toCharArray();

        final KeyStore store = KeyStore.getInstance(keystoreFile, keyStorePassword);
        Key key = store.getKey(keyAlias, keyPassword);
        if (!(key instanceof PrivateKey)) {
            throw new IllegalStateException("Provided keystore doesn't contain a private key, keyStore=" + jwtProperties.getKeyStore());
        }
        Certificate cert = store.getCertificate(keyAlias);
        return new KeyPair(cert.getPublicKey(), (PrivateKey) key);
    }
}
