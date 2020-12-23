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

package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.security.KeyPair;
import java.util.Base64;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(path = API_PREFIX_V1, produces = APPLICATION_JSON_VALUE)
@Tag(name = "jwt", description = "The JWT API")
public class JwtController {
    private final String x509PublicKey;

    public JwtController(KeyPair jwtKeyPair) {
        x509PublicKey = encodePublicKey(jwtKeyPair.getPublic());
    }

    private String encodePublicKey(Key publicKey) {
        // See https://tools.ietf.org/html/rfc1421#section-4.3.2.4
        Base64.Encoder mimeEncoder = Base64.getMimeEncoder(64, "\n".getBytes());

        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        sb.append(mimeEncoder.encodeToString(publicKey.getEncoded()));
        sb.append("\n-----END PUBLIC KEY-----\n");
        return sb.toString();
    }

    @Operation(summary = "Return the JWT key, which can be used to verify JWT tokens issued by this system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Returns the public key", content = @Content(schema = @Schema(
                    type = "string",
                    format = "pem"
            )))
    })
    @GetMapping("/jwt/pubkey")
    public String jwtPublicKey() {
        return x509PublicKey;
    }
}
