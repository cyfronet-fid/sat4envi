/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {
  AfterContentInit,
  ElementRef,
  Host,
  HostBinding,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  SkipSelf,
  ViewChild,
  Directive
} from '@angular/core';
import {
  ControlContainer,
  ControlValueAccessor,
  FormControl,
  FormControlName
} from '@angular/forms';
import {
  ControlSize,
  IFormControlOptions,
  VisualType
} from '../form-control/form-control.component';
import {ExtFormDirective} from '../form-directive/ext-form.directive';

@Directive()
export class GeneralInput
  implements OnInit, OnDestroy, ControlValueAccessor, AfterContentInit {
  public fc: FormControl = new FormControl();
  public currentValue: any;
  public control: FormControlName;
  public formControlOptions: Partial<IFormControlOptions>;

  public _disabled: boolean = false;
  public elementId: string = '';

  @HostBinding('class.input-sm') inputSm: boolean = false;
  @ViewChild('input', {static: true}) inputRef: ElementRef;

  get size(): ControlSize {
    return this.formControlOptions.size;
  }
  get idPrefix(): string {
    if (this.extForm && this.extForm.controlIdPrefix !== '') {
      return this.extForm.controlIdPrefix + '_';
    }
    return '';
  }

  get label() {
    return this._label;
  }
  @Input() set size(value: ControlSize) {
    this.formControlOptions = {...this.formControlOptions, size: value};
  }
  @Input() controlId: string;
  @Input()
  set required(value: boolean) {
    this.formControlOptions = {
      ...this.formControlOptions,
      required: value
    } as IFormControlOptions;
  }
  @Input()
  set tooltipText(value: string) {
    this.formControlOptions = {
      ...this.formControlOptions,
      tooltip: value
    } as IFormControlOptions;
  }
  @Input() formGroupClass: string = 'form-group-sm';
  @Input() inputClass: string = '';
  @Input() set disabled(value) {
    value ? this.fc.disable() : this.fc.enable();
    this._disabled = value;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }
  @Input() set visualType(newValue: VisualType) {
    this.formControlOptions = {
      ...this.formControlOptions,
      visualType: newValue
    } as IFormControlOptions;
  }
  @Input() set showErrorMessages(newValue: boolean) {
    this.formControlOptions = {
      ...this.formControlOptions,
      showErrorMessages: newValue
    } as IFormControlOptions;
  }
  @Input() set labelSize(value: number) {
    this.formControlOptions = {
      ...this.formControlOptions,
      labelSize: value
    } as IFormControlOptions;
  }
  @Input() set label(newValue: string) {
    this._label = newValue;
    this.formControlOptions = {
      ...this.formControlOptions,
      label: newValue
    } as IFormControlOptions;
    this.onSetLabel(newValue);
  }
  @Input('help') set _help(val: string) {
    this.formControlOptions = {
      ...this.formControlOptions,
      help: val
    } as IFormControlOptions;
  }

  // propagate changes into the custom form control
  protected propagateChange: Function = (_: any) => {};
  protected _label = '';
  protected onSetLabel(newValue: string) {}

  constructor(
    @Optional() @Host() @SkipSelf() protected controlContainer: ControlContainer,
    @Optional() protected extForm: ExtFormDirective
  ) {}

  public focus() {
    this.inputRef.nativeElement.focus();
  }

  public getErrors(): Array<string> {
    if (!this.controlContainer) {
      return [];
    }

    return (
      (!!this.control &&
        Array.isArray(this.control.errors) &&
        (this.control.errors as string[])) ||
      this.controlContainer.getError(this.controlId) ||
      []
    );
  }

  public hasErrors(): boolean {
    if (!this.controlContainer || (!!this.control && !this.control.touched)) {
      return false;
    }
    return (
      (!!this.control &&
        Array.isArray(this.control.errors) &&
        this.control.errors.length > 0) ||
      this.controlContainer.hasError(this.controlId) ||
      false
    );
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}

  ngAfterContentInit(): void {
    /**
     * This is ultra hack - as of now there is no way to get FormControl injected directly
     * to angular control, only ControlContainer. So in order to get FormControl a dirty one liner is required
     * @type {FormControl}
     */
    const formControlNames =
      (!!this.controlContainer &&
        ((this.controlContainer as any).directives as FormControlName[])) ||
      null;
    if (!!formControlNames) {
      this.control = formControlNames.find(fc => fc.valueAccessor === this);
    }

    if (!this.controlId && !this.control) {
      throw Error(
        `controlId not specified on input (label: ${this.label}) (and formControlName is not set)`
      );
    }

    this.calculateElementId();
  }

  writeValue(value: any): void {
    if (value !== undefined) {
      this.currentValue = value;
      this.inputRef.nativeElement.value = value;
    }
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  onTouched: any = () => {};

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onChange(e: Event, value: any) {
    // set changed value
    this.currentValue = value;
    // propagate value into form control using control value accessor interface
    this.propagateChange(this.currentValue);
  }

  calculateElementId() {
    const id =
      (!!this.controlId && this.controlId) || (!!this.control && this.control.name);
    this.elementId = this.idPrefix + id;

    this.formControlOptions = {
      ...this.formControlOptions,
      controlId: this.elementId,
      control: !!this.control ? this.control.control : undefined
    } as IFormControlOptions;
  }
}
