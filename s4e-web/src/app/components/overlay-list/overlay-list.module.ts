import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {OverlayListComponent} from './overlay-list.component';
import {ModalModule} from '../../modal/modal.module';
import {NgStackFormsModule} from '@ng-stack/forms';
import {S4EFormsModule} from '../../form/form.module';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {UtilsModule} from '../../utils/utils.module';

@NgModule({
  declarations: [OverlayListComponent],
  imports: [
    CommonModule,
    ModalModule,
    NgStackFormsModule,
    S4EFormsModule,
    FontAwesomeModule,
    UtilsModule
  ],
  exports: [OverlayListComponent]
})
export class OverlayListModule { }
