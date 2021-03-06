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

package pl.cyfronet.s4e.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.cyfronet.s4e.properties.JwtProperties;
import pl.cyfronet.s4e.security.JwtTokenService;
import pl.cyfronet.s4e.security.LoadKeyPair;
import pl.cyfronet.s4e.util.LicenseHelper;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
@Slf4j
public class JwtConfig {
    private static final String LOCAL_KEYSTORE = "dev_key.p12";

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private LicenseHelper licenseHelper;

    @Bean
    public KeyPair jwtKeyPair() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
        log.info("Loading jwtKeyPair from '" + jwtProperties.getKeyStore() + "'");
        if (LOCAL_KEYSTORE.equals(jwtProperties.getKeyStore())) {
            log.warn("Using a publicly available KeyPair!!! Don't expect actual security in this case.");
        }
        return LoadKeyPair.loadKeyPair(jwtProperties);
    }

    @Bean
    public JwtTokenService jwtTokenService(ObjectMapper objectMapper, KeyPair jwtKeyPair) {
        JwtProperties.Token token = jwtProperties.getToken();

        long expirationTime = token.getExpirationTime().toMillis();

        return new JwtTokenService(expirationTime, objectMapper, jwtKeyPair, licenseHelper);
    }
}
