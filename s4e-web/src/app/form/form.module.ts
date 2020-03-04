import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule} from '@angular/router';
import {environment} from '../../environments/environment';
import {UIDesignFormComponent} from './uidesign-form/uidesign-form.component';
import {TextareaComponent} from './textarea/textarea.component';
import {DatepickerComponent} from './datepicker/datepicker.component';
import {ExtFormDirective} from './form-directive/ext-form.directive';
import {ExtOptionDirective} from './select/option.directive';
import {SortMessagesPipe} from './messages/sort-messages.pipe';
import {FormControlComponent} from './form-control/form-control.component';
import {InputComponent} from './input/input.component';
import {SpinnerComponent} from './spinner/spinner.component';
import {ReactiveFormsModule} from '@angular/forms';
import {FormInputErrorsComponent} from './form-input-errors/form-input-errors.component';
import {SelectComponent} from './select/select.component';
import {DateUtilsService} from '../utils/s4e-date/date-utils.service';
import {CheckboxComponent} from './checkbox/checkbox.component';
import {OwlDialogModule} from 'ng-pick-datetime/dialog';
import {OwlDateTimeModule, OwlNativeDateTimeModule} from 'ng-pick-datetime';
import {ButtonComponent} from './button/button.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {MessagesComponent} from './messages/messages.component';
import {CheckBoxListComponent} from './check-box-list/check-box-list.component';

@NgModule({
  declarations: [
    UIDesignFormComponent,
    TextareaComponent,
    DatepickerComponent,
    ExtFormDirective,
    SelectComponent,
    ExtOptionDirective,
    SortMessagesPipe,
    FormControlComponent,
    InputComponent,
    SpinnerComponent,
    FormInputErrorsComponent,
    CheckboxComponent,
    ButtonComponent,
    MessagesComponent,
    CheckBoxListComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    OwlDialogModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule,
    FontAwesomeModule,
    RouterModule.forChild(environment.uiDevelopment ? [
      {path: 'ui-design', component: UIDesignFormComponent}
    ] : [])
  ],
  providers: [
    DateUtilsService
  ],
  exports: [
    TextareaComponent,
    DatepickerComponent,
    ExtFormDirective,
    ExtOptionDirective,
    SortMessagesPipe,
    FormControlComponent,
    InputComponent,
    CheckboxComponent,
    SelectComponent,
    FormInputErrorsComponent,
    MessagesComponent,
    CheckBoxListComponent
  ],
  entryComponents: [
    SpinnerComponent
  ]
})
export class S4EFormsModule { }
