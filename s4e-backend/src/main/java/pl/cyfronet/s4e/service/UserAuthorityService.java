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

package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.data.repository.AppUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthorityService {
    private final AppUserRepository appUserRepository;

    public <T> List<T> findAllUsersByAuthority(String authority, Class<T> projection) {
        return appUserRepository.findAllByAuthority(authority, projection);
    }

    @Transactional
    public Optional<Boolean> addAuthority(String email, String authority) {
        return appUserRepository.findByEmail(email)
                .map(appUser -> appUser.addAuthority(authority));
    }

    @Transactional
    public Optional<Boolean> removeAuthority(String email, String authority) {
        return appUserRepository.findByEmail(email)
                .map(appUser -> appUser.removeAuthority(authority));
    }
}
