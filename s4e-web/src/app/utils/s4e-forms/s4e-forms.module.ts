import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {CheckBoxListComponent} from './check-box-list/check-box-list.component';

@NgModule({
  declarations: [
    CheckBoxListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  exports: [
    CheckBoxListComponent
  ]
})
export class S4EFormsModule { }
