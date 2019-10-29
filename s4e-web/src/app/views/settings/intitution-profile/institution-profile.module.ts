import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {InstitutionProfileComponent} from './institution-profile.component';

@NgModule({
  declarations: [
    InstitutionProfileComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    InstitutionProfileComponent
  ]
})
export class InstitutionProfileModule { }
