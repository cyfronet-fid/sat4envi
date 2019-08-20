import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivateComponent} from './activate.component';
import {ReactiveFormsModule} from '@angular/forms';
import {ShareModule} from '../../common/share.module';
import {UtilsModule} from '../../utils/utils.module';

@NgModule({
  declarations: [
    ActivateComponent,
  ],
  imports: [
    CommonModule,
    ShareModule,
    UtilsModule
  ],
  exports: [
    ActivateComponent
  ]
})
export class ActivateModule {
}
