import {
  Component, ElementRef, forwardRef, Input, Optional, ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {ControlContainer, NG_VALUE_ACCESSOR, AbstractControl} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {untilDestroyed} from 'ngx-take-until-destroy';

const SPECIAL_TO_REGULAR_TYPES = {
  date: 'text',
  percent: 'number'
};

@Component({
  selector: 'ext-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true
    }
  ]
})
export class InputComponent extends GeneralInput {
  @Input() public inputType: string = 'text';
  public controlType: string = 'text';

  @Input() public css: string = '';
  @ViewChild('input') inputRef: ElementRef;

  focus(): void {
    this.inputRef.nativeElement.focus();
  }

  @Input('type')
  set type(type: string) {
    this.inputType = type;
    this.controlType = !!SPECIAL_TO_REGULAR_TYPES[type] && SPECIAL_TO_REGULAR_TYPES[type] || type;
  }

  @Input('placeholder') public _placeholder: string;

  constructor(cc: ControlContainer, @Optional() extForm: ExtFormDirective, private ref: ElementRef) {
    super(cc, extForm);
  }

  ngOnInit(): void {
    super.ngOnInit();
    this.fc.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(val => this.onChange(null, val));
  }

  onChange(e: Event, value: any) {
    if(this.controlType === 'number') {
      return super.onChange(e, !value ? undefined : Number(value));
    }
    return super.onChange(e, value);
  }

  writeValue(value: any): void {
    if (!!value) {
      this.currentValue = value;
      this.fc.setValue(value);
    }
  }

  protected onSetLabel(value: string) {
    if (!this._placeholder) {
      this._placeholder = value;
    }
  }
}
