import { untilDestroyed } from 'ngx-take-until-destroy';
import {
  Component, EventEmitter, forwardRef, Host, Input, Optional, Output, SkipSelf,
  ViewEncapsulation,
  OnDestroy,
  OnInit
} from '@angular/core';
import {ControlContainer, FormControl, NG_VALUE_ACCESSOR} from '@angular/forms';
import {GeneralInput} from '../general-input/general-input';
import {DateUtilsService} from '../../utils/s4e-date/date-utils.service';
import {ExtFormDirective} from '../form-directive/ext-form.directive';
import {S4eConfig} from '../../utils/initializer/config.service';
import {disableEnableForm} from '../../utils/miscellaneous/miscellaneous';

@Component({
  selector: 'ext-datepicker',
  templateUrl: './datepicker.component.html',
  styleUrls: ['./datepicker.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatepickerComponent),
      multi: true
    }
  ]
})
export class DatepickerComponent extends GeneralInput {
  _placeholder: string = 'RRRR-MM-DD';
  inputFormControl: FormControl = new FormControl();

  constructor(private dateUtils: DateUtilsService,
              public constants: S4eConfig,
              @Optional() extForm: ExtFormDirective,
              @Optional() @Host() @SkipSelf() cc: ControlContainer) {
    super(cc, extForm);
  }

  @Input() placement: 'top'|'bottom'|'left'|'right' = 'bottom';
  @Input() showNowButton: boolean = false;
  @Input() showExtraButton: boolean = true;
  @Input() extraButtonIcon: string = 'question-sign';

  @Output() extraButtonClick = new EventEmitter();

  ngOnInit() {
    super.ngOnInit();
    this.inputFormControl.valueChanges
      .pipe(untilDestroyed(this))
      .subscribe(value => {
        this.onChange(null, this.dateUtils.dateToApiString(value));
      });
  }

  setDateToNow() {
    this.writeValue(new Date());
  }

  writeValue(value: any): void {
    this.currentValue = value;
    this.inputFormControl.setValue(value);
  }


  setDisabledState(isDisabled: boolean) {
    super.setDisabledState(isDisabled);
    disableEnableForm(isDisabled, this.inputFormControl);
  }
}
