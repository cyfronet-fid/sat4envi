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

}
