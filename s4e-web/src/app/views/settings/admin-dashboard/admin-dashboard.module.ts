import { SearchModule } from './../../../components/search/search.module';
import { TilesDashboardModule } from './../../../components/tiles-dashboard/tiles-dashboard.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {AdminDashboardComponent} from './admin-dashboard.component';
import {RouterModule} from '@angular/router';

@NgModule({
  declarations: [
    AdminDashboardComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    TilesDashboardModule,
    SearchModule
  ],
  exports: [
    AdminDashboardComponent
  ]
})
export class AdminDashboardModule { }
