import { RouterModule } from '@angular/router';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {InstitutionProfileComponent} from './institution-profile.component';
import { GenericListViewModule } from '../components/generic-list-view/generic-list-view.module';

@NgModule({
  declarations: [
    InstitutionProfileComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    GenericListViewModule
  ],
  exports: [
    InstitutionProfileComponent
  ]
})
export class InstitutionProfileModule { }
