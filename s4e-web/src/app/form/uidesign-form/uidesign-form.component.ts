import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import environment from 'src/environments/environment';

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

  constructor() {
  }

  ngOnInit() {
    setTimeout(() => {
      this.customForm.controls.input.setErrors({
        required: true,
        email: true,
        mustMatch: true,
        server: ['Error #1', 'Error #2'],
        [environment.generalErrorKey]: 'General Type Error'
      });
      this.customForm.controls.input.markAsTouched();
      this.customForm.controls.inputDisabled.disable();
    }, 50);
  }
}
