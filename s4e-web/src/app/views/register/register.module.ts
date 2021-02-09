/*
 * Copyright 2021 ACC Cyfronet AGH
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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RegisterComponent} from './register.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {UtilsModule} from '../../utils/utils.module';
import {FormErrorModule} from '../../components/form-error/form-error.module';
import {RecaptchaFormsModule, RecaptchaModule} from 'ng-recaptcha';
import {RegisterConfirmationComponent} from './register-confirmation/register-confirmation.component';

@NgModule({
  declarations: [RegisterComponent, RegisterConfirmationComponent],
  imports: [
    CommonModule,
    ShareModule,
    ReactiveFormsModule,
    FormErrorModule,
    UtilsModule,
    RecaptchaFormsModule,
    RecaptchaModule
  ],
  exports: [RegisterComponent, RegisterConfirmationComponent]
})
export class RegisterModule {}
