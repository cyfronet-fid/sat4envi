import {
  AfterContentInit, ElementRef, Host, HostBinding, Input, OnDestroy, OnInit, Optional, SkipSelf,
  ViewChild
} from '@angular/core';
import {
  ControlContainer, ControlValueAccessor, FormControl, FormControlName} from '@angular/forms';
import {ControlSize, IFormControlOptions, VisualType} from '../form-control/form-control.component';
import {ExtFormDirective} from '../form-directive/ext-form.directive';


export class GeneralInput implements OnInit, OnDestroy, ControlValueAccessor, AfterContentInit {
  public fc: FormControl = new FormControl();

  get size(): ControlSize {
    return this.formControlOptions.size;
  }

  public focus() {
    this.inputRef.nativeElement.focus();
  }

  @Input() set size(value: ControlSize) {
    this.formControlOptions = {...this.formControlOptions, size: value};
  }
  public currentValue: any;

  @HostBinding('class.input-sm') inputSm: boolean = false;


  @Input()
  public set required(value: boolean) {
    this.formControlOptions = {...this.formControlOptions, required: value} as IFormControlOptions;
  }

  @Input()
  public set tooltipText(value: string) {
    this.formControlOptions = {...this.formControlOptions, tooltip: value} as IFormControlOptions;
  }

  @Input() public formGroupClass: string = 'form-group-sm';
  @Input() public inputClass: string = '';

  control: FormControlName;

  _disabled: boolean = false;

  @Input() set disabled(value) {
    if(value) {
      this.fc.disable();
    } else {
      this.fc.enable();
    }
    this._disabled = value;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }

  @Input() public controlId: string;

  @Input() set visualType(newValue: VisualType) {
    this.formControlOptions = {...this.formControlOptions, visualType: newValue} as IFormControlOptions;
  }

  @Input() set showErrorMessages(newValue: boolean) {
    this.formControlOptions = {...this.formControlOptions, showErrorMessages: newValue} as IFormControlOptions;
  }

  @Input() set labelSize(value: number) {
    this.formControlOptions = {...this.formControlOptions, labelSize: value} as IFormControlOptions;
  }

  protected _label: string = '';

  protected onSetLabel(newValue: string) {
  }


  @Input() public set label(newValue: string) {
    this._label = newValue;
    this.formControlOptions = {...this.formControlOptions, label: newValue} as IFormControlOptions;
    this.onSetLabel(newValue);
  }

  public get label() {
    return this._label;
  }

  @Input('help') public set _help(val: string) {
    this.formControlOptions = {...this.formControlOptions, help: val} as IFormControlOptions;
  }
  formControlOptions: Partial<IFormControlOptions>;

  // propagate changes into the custom form control
  protected propagateChange: Function = (_: any) => {
  };

  // get reference to the input element
  @ViewChild('input') inputRef: ElementRef;

  get idPrefix(): string {
    if (this.extForm && this.extForm.controlIdPrefix !== '') {
      return this.extForm.controlIdPrefix + '_';
    }
    return '';
  }

  constructor(@Optional() @Host() @SkipSelf() protected cc: ControlContainer,
              @Optional() protected extForm: ExtFormDirective) {
  }

  public getErrors(): Array<string> {
    if (typeof(this.cc) === 'undefined')
      return [];
    if(this.control != null && Array.isArray(this.control.errors))
      return this.control.errors as string[];
    return this.cc.getError(this.controlId) || [];
  }

  public hasErrors(): boolean {
    if (this.cc === null)
      return false;
    if(this.control != null && Array.isArray(this.control.errors))
      return this.control.errors.length > 0;
    if(this.control != null && !this.control.touched)
      return false;
    return this.cc.hasError(this.controlId) || false;
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
  }

  ngAfterContentInit(): void {
    /**
     * This is ultra hack - as of now there is no way to get FormControl injected directly
     * to angular control, only ControlContainer. So in order to get FormControl a dirty one liner is required
     * @type {FormControl}
     */
    if (this.cc != null && (this.cc as any).directives)
      this.control = ((this.cc as any).directives as FormControlName[]).find(fc => {
        return fc.valueAccessor === this;
      });

    if (this.controlId == null && this.control == null)
      throw Error(`controlId not specified on input (label: ${this.label}) (and formControlName is not set)`);

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

  elementId: string = '';

  calculateElementId() {
    let id = '';
    if (this.controlId)
      id = this.controlId;
    if (this.control)
      id = this.control.name;

    this.elementId = this.idPrefix + id;

    this.formControlOptions = {
      ...this.formControlOptions,
      controlId: this.elementId,
      control: this.control != null ? this.control.control : undefined
    } as IFormControlOptions;
  }
}
