import {
  Component, ElementRef, forwardRef, Input, Optional, ViewChild,
  ViewEncapsulation
} from '@angular/core';
import {ControlContainer, NG_VALUE_ACCESSOR} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {untilDestroyed} from 'ngx-take-until-destroy';

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

    if (type === 'date') {
      this.controlType = 'text';
    }
    else if (type === 'percent') {
      this.controlType = 'number';
    }
    else {
      this.controlType = type;
    }
  }

  @Input('placeholder') public _placeholder: string;

  protected onSetLabel(value: string) {
    if (!this._placeholder) {
      this._placeholder = value;
    }
  }

  onChange(e: Event, value: any) {
    if(this.controlType === 'number') {
      return super.onChange(e, value == null ? undefined : Number(value));
    }
    return super.onChange(e, value);
  }

  writeValue(value: any): void {
    if (value !== undefined) {
      this.currentValue = value;
      this.fc.setValue(value)
    }
  }

  constructor(cc: ControlContainer, @Optional() extForm: ExtFormDirective, private ref: ElementRef) {
    super(cc, extForm);
  }


  ngOnInit(): void {
    super.ngOnInit();
    this.fc.valueChanges.pipe(untilDestroyed(this)).subscribe(val => this.onChange(null, val));
  }
}
