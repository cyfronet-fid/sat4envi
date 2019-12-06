import { Component, OnInit } from '@angular/core';
import {Observable, ReplaySubject} from 'rxjs';

@Component({
  selector: 's4e-sentinel-search',
  templateUrl: './sentinel-search.component.html',
  styleUrls: ['./sentinel-search.component.scss']
})
export class SentinelSearchComponent implements OnInit {
  // loading$: Observable<boolean> = new ReplaySubject();
  loading = false;

  constructor() { }

  ngOnInit() {
  }

  search() {
    this.loading = !this.loading
  }
}
