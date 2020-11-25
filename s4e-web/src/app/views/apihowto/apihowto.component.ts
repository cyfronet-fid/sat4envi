import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {LOCATION} from '../../app.providers';
import environment from '../../../environments/environment';

@Component({
  templateUrl: './apihowto.component.html',
  styleUrls: ['./apihowto.component.scss']
})

export class ApihowtoComponent implements OnInit, OnDestroy {
  public readonly API_BASE: string;
  public readonly API_WMS: string;

  constructor(@Inject(LOCATION) location: Location) {
    this.API_BASE = location.origin + '/' + environment.apiPrefixV1;
    this.API_WMS = location.origin + '/wms';
  }

  ngOnInit() {
  }

  ngOnDestroy() {
  }
}
