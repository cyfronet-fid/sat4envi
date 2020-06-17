import { PERSON_FORM_MODAL_ID } from './person-form/person-form-modal.model';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {PersonListComponent} from './person-list/person-list.component';
import {PersonFormComponent} from './person-form/person-form.component';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../form/form.module';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';
import { makeModalProvider } from 'src/app/modal/modal.providers';
import { ModalModule } from 'src/app/modal/modal.module';

@NgModule({
  declarations: [
    PersonListComponent,
    PersonFormComponent
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
    PersonFormComponent
  ],
  providers: [
    makeModalProvider(PERSON_FORM_MODAL_ID, PersonFormComponent)
  ]
})
export class PeopleModule {
}
