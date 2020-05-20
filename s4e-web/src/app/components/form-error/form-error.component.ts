import {Component, Input, OnInit} from '@angular/core';
import {ValidatorsModel} from '@ng-stack/forms';
import {AbstractControl} from '@angular/forms';

@Component({
  selector: 'ul[s4e-form-error]',
  templateUrl: './form-error.component.html',
  styleUrls: ['./form-error.component.scss'],
  host: {'[class]': '"invalid-feedback special__error"'}
})
export class FormErrorComponent implements OnInit {
  @Input() errors: ValidatorsModel | {server?: string[]};
  @Input() control: AbstractControl;

  constructor() { }

  ngOnInit() {
  }

  getServerErrorMessages() {
    const errors = !!this.control && this.control.errors || this.errors;
    const serverErrors = !!errors && !!errors.server && (
      errors.server instanceof Array && errors.server
      || typeof errors.server === 'string' && [errors.server]
    )
  || [];

    return serverErrors;
  }

}
