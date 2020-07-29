import { INVITATION_FORM_MODAL_ID } from './invitation-form/invitation-form-modal.model';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
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
