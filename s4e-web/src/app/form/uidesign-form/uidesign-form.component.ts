import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {S4eConfig} from '../../utils/initializer/config.service';

@Component({
  selector: 's4e-uidesign-form',
  templateUrl: './uidesign-form.component.html',
  styleUrls: ['./uidesign-form.component.scss']
})
export class UIDesignFormComponent implements OnInit {
  customForm = new FormGroup({
    input: new FormControl(''),
    inputDisabled: new FormControl(''),
    select: new FormControl(1),
    textarea: new FormControl(''),
    datepicker: new FormControl(null),
    checkbox: new FormControl(true),
  });

  constructor(private config: S4eConfig) {
  }

  ngOnInit() {
    setTimeout(() => {
      this.customForm.controls.input.setErrors({
        required: true,
        email: true,
        mustMatch: true,
        server: ['Error #1', 'Error #2'],
        [this.config.generalErrorKey]: 'General Type Error'
      });
      this.customForm.controls.input.markAsTouched();
      this.customForm.controls.inputDisabled.disable();
    }, 50);
  }
}
