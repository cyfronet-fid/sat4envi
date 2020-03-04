import {Component, Input, OnInit} from '@angular/core';
import {HashMap} from '@datorama/akita';

@Component({
  selector: 'ext-form-input-errors',
  templateUrl: './form-input-errors.component.html',
  styleUrls: ['./form-input-errors.component.scss']
})
export class FormInputErrorsComponent implements OnInit {

  @Input() set errorList(value: HashMap<any>) {
    this.errors = Object.entries(value).map(([key, value]) => ({key, value}));
  }

  public errors: {key: string, value: any}[];

  constructor() { }

  ngOnInit() {
  }
}
