import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ID} from '@datorama/akita';
import {IUILayer} from '../../state/common.model';
import {ProductType} from '../../state/product-type/product-type.model';

@Component({
  selector: 's4e-product-picker',
  templateUrl: './layer-picker.component.html',
  styleUrls: ['./layer-picker.component.scss']
})
export class LayerPicker implements OnInit {
  @Input() items: IUILayer[] = [];
  @Input() loading: boolean = true;
  @Input() selectedIds: number[] = [];
  @Input() help: string;
  @Input() caption: string;

  @Output() itemSelected = new EventEmitter<ID>();

  constructor() { }

  ngOnInit() {
  }
}
