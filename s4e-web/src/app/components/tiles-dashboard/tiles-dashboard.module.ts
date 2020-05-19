import { TilesDashboardComponent } from './tiles-dashboard.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TileComponent } from './tile/tile.component';

@NgModule({
  declarations: [
    TilesDashboardComponent,
    TileComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    TilesDashboardComponent,
    TileComponent
  ]
})
export class TilesDashboardModule { }
