import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'ext-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss']
})
export class SpinnerComponent implements OnInit {
  @Input() isLoading: boolean = false;

  constructor() { }

  ngOnInit() {
  }

}
