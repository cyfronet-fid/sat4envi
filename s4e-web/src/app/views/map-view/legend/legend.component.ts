import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Legend} from '../state/legend/legend.model';

@Component({
  selector: 's4e-legend',
  templateUrl: './legend.component.html',
  styleUrls: ['./legend.component.scss']
})
export class LegendComponent implements OnInit {
  @Input() activeLegend: Legend;
  @Input() isOpen: boolean = false;
  @Output() opened = new EventEmitter<boolean>();

  constructor() { }

  ngOnInit() {
  }
}
