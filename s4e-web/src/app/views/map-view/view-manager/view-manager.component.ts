import {Component, EventEmitter, Input, Output} from '@angular/core';
import {IUILayer} from '../state/common.model';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss'],
})
export class ViewManagerComponent {
  @Input() loading = true;
  @Input() userLoggedIn: boolean = false;
  @Input() products: IUILayer[] = [];
  @Input() productsTypes: IUILayer[] = [];
  @Input() productTypeLoading: boolean = true;
  @Output() selectProductType = new EventEmitter<number>();
  @Input() overlays: IUILayer[] = [];
  @Input() overlaysLoading: boolean = true;

  @Output() selectOverlay = new EventEmitter<string>();
  @Output() logout = new EventEmitter<void>();

  constructor() {}
}
