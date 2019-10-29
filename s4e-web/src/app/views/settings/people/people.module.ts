import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {PersonListComponent} from './person-list/person-list.component';
import {PersonFormComponent} from './person-form/person-form.component';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../utils/s4e-forms/s4e-forms.module';

@NgModule({
  declarations: [
    PersonListComponent,
    PersonFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    S4EFormsModule,
  ],
  exports: [
    PersonListComponent,
    PersonFormComponent
  ]
})
export class PeopleModule {
}
