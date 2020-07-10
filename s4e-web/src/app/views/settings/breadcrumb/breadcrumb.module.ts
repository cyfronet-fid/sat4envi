import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbComponent } from './breadcrumb.component';
import { RouterModule } from '@angular/router';
import {BreadcrumbService} from './breadcrumb.service';

@NgModule({
  providers: [
    BreadcrumbService
  ],
  declarations: [
    BreadcrumbComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [BreadcrumbComponent]
})
export class BreadcrumbModule {}
