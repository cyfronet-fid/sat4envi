import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {OwnerType} from '../../map-view/state/overlay/overlay.model';
import {OverlayService} from '../../map-view/state/overlay/overlay.service';
import {GLOBAL_OVERLAYS_PATH, INSTITUTION_OVERLAYS_PATH} from '../settings.routes';

@Component({
  templateUrl: './wms-overlays.component.html',
  styleUrls: ['./wms-overlays.component.scss']
})
export class WmsOverlaysComponent implements OnInit {
  public ownerType: OwnerType;

  constructor(
    private _router: Router,
    private _overlayService: OverlayService
  ) {}

  ngOnInit() {
    this._overlayService.get();

    const urlWithoutParams = this._router.url.split('?').shift();
    const lastUrlSegment = urlWithoutParams.split('/').pop();
    switch (lastUrlSegment) {
      case INSTITUTION_OVERLAYS_PATH:
        this.ownerType = 'INSTITUTIONAL';
        break;
      case GLOBAL_OVERLAYS_PATH:
        this.ownerType = 'GLOBAL';
        break;
    }
  }
}
