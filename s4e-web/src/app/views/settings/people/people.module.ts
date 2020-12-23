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

import { INVITATION_FORM_MODAL_ID } from './invitation-form/invitation-form-modal.model';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';
import {PersonListComponent} from './person-list/person-list.component';
import {InvitationFormComponent} from './invitation-form/invitation-form.component';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../form/form.module';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';
import { makeModalProvider } from 'src/app/modal/modal.providers';
import { ModalModule } from 'src/app/modal/modal.module';

@NgModule({
  declarations: [
    PersonListComponent,
    InvitationFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    GenericListViewModule,
    S4EFormsModule,
    ModalModule
  ],
  exports: [
    PersonListComponent,
    InvitationFormComponent
  ],
  providers: [
    makeModalProvider(INVITATION_FORM_MODAL_ID, InvitationFormComponent)
  ],
  entryComponents: [
    InvitationFormComponent
  ]
})
export class PeopleModule {
}
