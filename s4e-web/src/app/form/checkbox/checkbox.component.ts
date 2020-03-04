import {Component, forwardRef, ViewEncapsulation} from '@angular/core';
import {GeneralInput} from '../general-input/general-input';
import {NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
  selector: 'ext-checkbox',
  templateUrl: './checkbox.component.html',
  styleUrls: ['./checkbox.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxComponent),
      multi: true
    }
  ]
})
export class CheckboxComponent extends GeneralInput {

  writeValue(value: any): void {
    if (value !== undefined) {
      this.currentValue = value;
      this.inputRef.nativeElement.checked = value;
    }
  }
}
