import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {InstitutionProfileComponent} from './institution-profile.component';

@NgModule({
  declarations: [
    InstitutionProfileComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    InstitutionProfileComponent
  ]
})
export class InstitutionProfileModule { }
