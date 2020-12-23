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

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApihowtoComponent} from "./apihowto.component";
import {ShareModule} from '../../common/share.module';


@NgModule({
  declarations: [
    ApihowtoComponent,
  ],
  imports: [
    CommonModule,
    ShareModule
  ],
  exports: [
    ApihowtoComponent
  ]
})

export class ApihowtoModule {
}
