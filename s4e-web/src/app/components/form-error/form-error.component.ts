import {Component, Input, OnInit} from '@angular/core';
import {HashMap} from '@datorama/akita';
import {ValidatorsModel} from '@ng-stack/forms';

@Component({
  selector: 'ul[s4e-form-error]',
  templateUrl: './form-error.component.html',
  styleUrls: ['./form-error.component.scss'],
  host: {'[class]': '"invalid-feedback"'}
})
export class FormErrorComponent implements OnInit {
  @Input() errors: ValidatorsModel | {server?: string[]};

  constructor() { }

  ngOnInit() {
  }

}
