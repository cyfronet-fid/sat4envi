import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';

@Component({
  selector: 'ext-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss']
})
export class ButtonComponent {
  @Input('disabled') _disabled: boolean = false;
  @Input() cssClass: string = 'button--primary button--small';
  @Input() type: string = 'button';
  @Input() onClick: () => void|Promise<boolean>|Observable<boolean> = () => {};

  @Input() isLoading: boolean = false;

  constructor() { }
}
