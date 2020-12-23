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

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.cyfronet.s4e.controller.request.HelpType;

import java.util.Locale;

@Getter
public class OnSendHelpRequestEvent extends ApplicationEvent {
    private final String requestingUserEmail;
    private final String expertEmail;
    private final HelpType helpType;
    private final String issueDescription;
    private final Locale locale;

    public OnSendHelpRequestEvent(String requestingUserEmail, String expertEmail, HelpType helpType, String issueDescription, Locale locale) {
        super(requestingUserEmail);

        this.requestingUserEmail = requestingUserEmail;
        this.expertEmail = expertEmail;
        this.helpType = helpType;
        this.issueDescription = issueDescription;
        this.locale = locale;
    }
}
