/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
