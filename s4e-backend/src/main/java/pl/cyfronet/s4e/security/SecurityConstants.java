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

package pl.cyfronet.s4e.security;

public class SecurityConstants {
    public static final String HEADER_NAME = "Authorization";
    public static final String COOKIE_NAME = "token";

    public static final String JWT_AUTHORITIES_CLAIM = "authorities";
    public static final String JWT_LAYERS_CLAIM = "layers";
    public static final String JWT_PRIORITY_ACCESS_CLAIM = "priority_access";

    public static final String LICENSE_READ_AUTHORITY_PREFIX = "LICENSE_READ_";
    public static final String LICENSE_WRITE_AUTHORITY_PREFIX = "LICENSE_WRITE_";
}
