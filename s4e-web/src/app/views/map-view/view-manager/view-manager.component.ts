import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Layer, Image} from 'ol/layer';
import {ImageWMS} from 'ol/source';
import {Overlay} from '../state/overlay/overlay.model';
import {ICompleteRecentView, RecentView} from '../state/recent-view/recent-view.model';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss'],
})
export class ViewManagerComponent {
  @Input() loading = true;
  @Input() overlays: Overlay[] = [];
  @Input() activeRecentView: RecentView|null = null;
  @Input() recentViews: ICompleteRecentView[] = [];

  @Output() activeViewChange = new EventEmitter<RecentView>();
  @Output() removeView = new EventEmitter<RecentView>();

  constructor() {}
}
