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

package pl.cyfronet.s4e.admin.sync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.IntegrationTest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Note that in these tests we pass it a path prefixed with slash.
 * This is ok, most likely because of the minio implementation of the AWS S3, which is also different
 * than CEPH.
 * See e.g. https://github.com/minio/minio/issues/7717.
 */
@IntegrationTest
class PrefixScannerIntegrationTest {
    @Autowired
    private PrefixScanner prefixScanner;

    @Test
    public void shouldWork() {
        Stream<S3Object> scan = prefixScanner.scan("/");

        assertThat(scan.count(), is(equalTo(10296L)));
    }

    @Test
    public void shouldLimitResultsToPrefix() {
        Stream<S3Object> scan = prefixScanner.scan("/MSG_Products_WM/108m/20200201/202002010000_kan_10800m");

        assertThat(scan.map(S3Object::key).collect(Collectors.toList()), containsInAnyOrder(
                "/MSG_Products_WM/108m/20200201/202002010000_kan_10800m.cog.tif",
                "/MSG_Products_WM/108m/20200201/202002010000_kan_10800m.metadata",
                "/MSG_Products_WM/108m/20200201/202002010000_kan_10800m.scene"
        ));
    }
}
