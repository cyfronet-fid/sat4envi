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

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { SearchComponent } from './search.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { S4EFormsModule } from 'src/app/form/form.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EventsModule } from 'src/app/utils/search/events.module';

@NgModule({
  declarations: [
    SearchComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    S4EFormsModule,
    ReactiveFormsModule,
    FontAwesomeModule,
    EventsModule
  ],
  exports: [
    SearchComponent
  ]
})
export class SearchModule { }
