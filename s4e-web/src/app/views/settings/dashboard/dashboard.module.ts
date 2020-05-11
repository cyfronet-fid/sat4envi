import { TilesDashboardModule } from './../../../components/tiles-dashboard/tiles-dashboard.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {DashboardComponent} from './dashboard.component';
import {RouterModule} from '@angular/router';

@NgModule({
  declarations: [
    DashboardComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    TilesDashboardModule
  ],
  exports: [
    DashboardComponent
  ]
})
export class DashboardModule { }
