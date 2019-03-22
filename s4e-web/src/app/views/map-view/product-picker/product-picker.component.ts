import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ProductType} from '../state/product-type/product-type.model';

@Component({
  selector: 's4e-product-picker',
  templateUrl: './product-picker.component.html',
  styleUrls: ['./product-picker.component.scss']
})
export class ProductPickerComponent implements OnInit {
  @Input() products: ProductType[] = [];
  @Input() loading: boolean = true;
  @Output() selectProduct = new EventEmitter<ProductType>();

  constructor() { }

  ngOnInit() {
  }
}
