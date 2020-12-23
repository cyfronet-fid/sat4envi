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

package pl.cyfronet.s4e.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Locale;

@Getter
public class OnShareLinkEvent extends ApplicationEvent {
    @Value
    @Builder
    public static class Request {
        String caption;
        String description;
        byte[] thumbnail;
        String path;
        List<String> emails;
    }

    private final String requesterEmail;
    private final Request request;
    private final Locale locale;

    public OnShareLinkEvent(String requesterEmail, Request request, Locale locale) {
        super(requesterEmail);

        this.requesterEmail = requesterEmail;
        this.request = request;
        this.locale = locale;
    }
}
