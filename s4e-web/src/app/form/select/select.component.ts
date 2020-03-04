import {Component, forwardRef, Host, Optional, SkipSelf, ViewChild, ViewEncapsulation} from '@angular/core';
import {ControlContainer, FormControl, NG_VALUE_ACCESSOR, SelectControlValueAccessor} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {DateUtilsService} from '../../utils/s4e-date/date-utils.service';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {S4eConfig} from '../../utils/initializer/config.service';

@Component({
  selector: 'ext-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true
    }
  ]
})
export class SelectComponent extends GeneralInput {
  inputFormControl: FormControl = new FormControl();
  @ViewChild(SelectControlValueAccessor) select: SelectControlValueAccessor;

  constructor(private dateUtils: DateUtilsService,
              public constants: S4eConfig,
              @Optional() extForm: ExtFormDirective,
              @Optional() @Host() @SkipSelf() cc: ControlContainer) {
    super(cc, extForm);
    this.inputFormControl.valueChanges.subscribe(value => {
      this.onChange(null, value);
    });
  }

  writeValue(value: any): void {
    this.currentValue = value;
    this.inputFormControl.setValue(value);
  }

  setDisabledState(isDisabled: boolean): void {
    super.setDisabledState(isDisabled);
    if (isDisabled) {
      this.inputFormControl.disable();
    } else {
      this.inputFormControl.enable();
    }
  }
}
