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

package pl.cyfronet.s4e.ex.product;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import pl.cyfronet.s4e.ex.BindingResultException;

public class ProductValidationException extends ProductException implements BindingResultException {
    @Getter
    private final BindingResult bindingResult;

    public ProductValidationException(BindingResult bindingResult) {
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, Throwable cause, BindingResult bindingResult) {
        super(message, cause);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(Throwable cause, BindingResult bindingResult) {
        super(cause);
        this.bindingResult = bindingResult;
    }

    public ProductValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, BindingResult bindingResult) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bindingResult = bindingResult;
    }
}
