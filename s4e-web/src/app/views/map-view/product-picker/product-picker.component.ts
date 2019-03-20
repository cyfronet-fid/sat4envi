import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Product} from '../state/product/product.model';

@Component({
  selector: 's4e-product-picker',
  templateUrl: './product-picker.component.html',
  styleUrls: ['./product-picker.component.scss']
})
export class ProductPickerComponent implements OnInit {
  @Input() products: Product[] = [];
  @Input() loading: boolean = true;
  @Output() selectProduct = new EventEmitter<Product>();

  constructor() { }

  ngOnInit() {
  }
}
