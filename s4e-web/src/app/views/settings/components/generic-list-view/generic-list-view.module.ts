import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {GenericListViewComponent} from './generic-list-view.component';
import {S4EFormsModule} from '../../../../form/form.module';

@NgModule({
  declarations: [
    GenericListViewComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    S4EFormsModule,
  ],
  exports: [
    GenericListViewComponent
  ]
})
export class GenericListViewModule {
}
