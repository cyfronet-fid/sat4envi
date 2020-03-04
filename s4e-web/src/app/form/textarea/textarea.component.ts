import {
  Component, ElementRef, forwardRef, Input, Optional, ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {ControlContainer, NG_VALUE_ACCESSOR} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ExtFormDirective} from '../form-directive/ext-form.directive';

@Component({
  selector: 'ext-textarea',
  templateUrl: './textarea.component.html',
  styleUrls: ['./textarea.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextareaComponent),
      multi: true
    }
  ]
})
export class TextareaComponent extends GeneralInput {
  @Input('placeholder') _placeholder: string;
  @ViewChild('input') inputRef: ElementRef;

  constructor(cc: ControlContainer, @Optional() extForm: ExtFormDirective) {
    super(cc, extForm);
  }

  focus(): void {
    this.inputRef.nativeElement.focus();
  }

  protected onSetLabel(value: string) {
    if (!this._placeholder) {
      this._placeholder = value;
    }
  }

  writeValue(value: any): void {
    if (value !== undefined) {
      this.currentValue = value;
      this.fc.setValue(value)
    }
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.fc.valueChanges.pipe(untilDestroyed(this)).subscribe(val => this.onChange(null, val));
  }
}
