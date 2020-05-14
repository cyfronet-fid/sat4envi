import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {PersonListComponent} from './person-list/person-list.component';
import {PersonFormComponent} from './person-form/person-form.component';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../form/form.module';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';

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
  ],
  exports: [
    PersonListComponent,
    PersonFormComponent
  ]
})
export class PeopleModule {
}
