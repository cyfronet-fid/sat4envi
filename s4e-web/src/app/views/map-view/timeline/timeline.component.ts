import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnInit, Output} from '@angular/core';
import {Product} from '../state/product/product.model';
import {formatDate} from '@angular/common';

export interface Day {
  label: string;
  products: Product[];

}

@Component({
  selector: 's4e-timeline',
  templateUrl: './timeline.component.html',
  styleUrls: ['./timeline.component.scss']
})
export class TimelineComponent {

  public days: Day[] = [];

  @Input() public loading: boolean = true;

  @Input() set products(products: Product[] | null) {
    this.days = [];
    let currDay: Day;
    for (const product of (products || [])) {
      const day = formatDate(product.timestamp, 'shortDate', this.LOCALE_ID);
      if (currDay === undefined || currDay.label !== day) {
        currDay = {label: day, products: []};
        this.days.push(currDay);
      }
      currDay.products.push(product);
    }
  }

  @Output() public selectProduct = new EventEmitter<Product>();

  // tslint:disable-next-line:no-shadowed-variable
  constructor(@Inject(LOCALE_ID) private LOCALE_ID: string) { }
}
