import {Component, EventEmitter, HostBinding, Input, Output} from '@angular/core';

import {Layer} from './layer.model';

@Component({
  selector: 's4e-layer',
  templateUrl: './layer.component.html',
  styleUrls: ['./layer.component.scss'],
})
export class LayerComponent {
  @HostBinding() class = 'clearfix';
  @Input() layer: Layer;
  @Output() moveDown = new EventEmitter<void>();
  @Output() moveUp = new EventEmitter<void>();
  @Output() remove = new EventEmitter<void>();
  @Output() opacity = new EventEmitter<number>();
  detailsVisible = false;
}
