import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApihowtoComponent} from "./apihowto.component";
import {ShareModule} from '../../common/share.module';


@NgModule({
  declarations: [
    ApihowtoComponent,
  ],
  imports: [
    CommonModule,
    ShareModule
  ],
  exports: [
    ApihowtoComponent
  ]
})

export class ApihowtoModule {
}
