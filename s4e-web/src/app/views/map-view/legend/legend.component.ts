import {Component, EventEmitter, HostBinding, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Legend} from '../state/legend/legend.model';
import {DomSanitizer, SafeStyle} from '@angular/platform-browser';

@Component({
  selector: 's4e-legend',
  templateUrl: './legend.component.html',
  styleUrls: ['./legend.component.scss']
})
export class LegendComponent implements OnInit, OnChanges {
  readonly OPEN_WIDTH = 221;
  readonly CLOSED_WIDTH = 20;

  @Input() activeLegend: Legend;
  isOpen: boolean = false;
  @Input('isOpen') set _isOpen(open: boolean) {
    this.isOpen = open;
    this.width = this.isOpen ? this.OPEN_WIDTH : this.CLOSED_WIDTH;
  }
  @Output() opened = new EventEmitter<boolean>();
  url: SafeStyle|null = null;

  constructor(private domSanitizer: DomSanitizer) { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.activeLegend && changes.activeLegend.currentValue != null) {
      this.url = this.domSanitizer.bypassSecurityTrustStyle(`url(${(changes.activeLegend.currentValue as Legend).url})`);
    }
  }

  @HostBinding('style.width.px') width: number = this.CLOSED_WIDTH;
}
