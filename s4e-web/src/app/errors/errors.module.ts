import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ErrorsComponent} from './errors.component';
import {RouterModule} from '@angular/router';

@NgModule({
  declarations: [ErrorsComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {
        path: `errors/:errorCode`,
        component: ErrorsComponent
      }
    ])
  ],
  exports: [
    ErrorsComponent
  ]
})
export class ErrorsModule {
}
