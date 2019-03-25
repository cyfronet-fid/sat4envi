import { Injectable } from '@angular/core';
import { ID } from '@datorama/akita';
import { HttpClient } from '@angular/common/http';
import { OverlayStore } from './overlay.store';
import {Overlay, OverlayType} from './overlay.model';
import {of} from 'rxjs';
import {finalize} from 'rxjs/operators';

/**
 * This is stub service which will be responsible for getting overlay data
 * when it's backend implementation is finished
 */
@Injectable({ providedIn: 'root' })
export class OverlayService {
  constructor(private overlayStore: OverlayStore, private http: HttpClient) {}

  get() {
    this.overlayStore.setLoading(true);
    // :TODO replace mock with HTTP request
    of([{
      id: 'test:wojew%C3%B3dztwa',
      caption: 'regions',
      type: 'wms' as OverlayType
    }]).pipe(finalize(() => this.overlayStore.setLoading(false))).subscribe(overlays => this.overlayStore.set(overlays));
  }
}
