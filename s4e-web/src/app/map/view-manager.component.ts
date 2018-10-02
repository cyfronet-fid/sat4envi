import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Layer, Image} from 'ol/layer';
import {ImageWMS} from 'ol/source';

import {Granule} from '../products/granule.model';
import {GranuleView} from './granule-view.model';
import {Overlay} from './overlay.model';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss'],
})
export class ViewManagerComponent {
  @Input() overlays: Overlay[];
  @Input() granuleViews: GranuleView[];
  @Input() activeGranuleView: Granule;
  @Output() activeGranuleViewChangeRequest: EventEmitter<GranuleView>;
  @Output() granuleViewRemoveRequest: EventEmitter<GranuleView>;

  constructor() {
    this.activeGranuleViewChangeRequest = new EventEmitter();
    this.granuleViewRemoveRequest = new EventEmitter();
  }
}
