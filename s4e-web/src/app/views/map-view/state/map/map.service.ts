import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MapStore} from './map.store';
import {MapQuery} from './map.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ViewPosition} from './map.model';
import {combineLatest, forkJoin, Observable, of, throwError} from 'rxjs';
import {catchError, distinctUntilChanged, filter, map, take, tap} from 'rxjs/operators';
import {OverlayQuery} from '../overlay/overlay.query';
import {SceneService} from '../scene/scene.service';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OverlayService} from '../overlay/overlay.service';
import {ProductService} from '../product/product.service';

@Injectable({providedIn: 'root'})
export class MapService {

  constructor(private store: MapStore,
              private mapQuery: MapQuery,
              private http: HttpClient,
              private overlayQuery: OverlayQuery,
              private overlayService: OverlayService,
              private productQuery: ProductQuery,
              private productService: ProductService,
              private sceneQuery: SceneQuery,
              private sceneService: SceneService,
              private router: Router,
              private CONFIG: S4eConfig
  ) {
  }

  toggleZKOptions(open: boolean = true) {
    this.store.update({zkOptionsOpened: open});
  }

  setWorking($event: boolean) {
    this.store.setLoading($event);
  }

  setView(view: ViewPosition): void {
    this.store.update({view});
  }

  public connectRouterToStore(route: ActivatedRoute) {
    interface ViewRouterConfig {
      overlays: string[];
      viewPosition: ViewPosition;
      productId: number;
      date: string;
      sceneId: number;
    }

    return forkJoin([
      route.queryParamMap.pipe(
        map(params => {
          const overlays = params.getAll('overlays');
          const productId = params.get('product');
          const date = params.get('date');
          const sceneId = params.get('scene');
          const centerX = params.get('centerx');
          const centerY = params.get('centery');
          const zoom = params.get('zoom');

          // validate data, if there is something missing throw error, which will prevent setting store from those query params
          if (zoom == null || centerX == null || centerY == null || (date != null && !date.match(/^[0-9]+-[0-1][0-9]-[0-9][0-9]$/g))) {
            throw new Error('queryParams do not have all required params');
        }

          const viewPosition = {
            centerCoordinates: [Number(centerX), Number(centerY)],
            zoomLevel: Number(zoom)
          };

          return {
            overlays,
            viewPosition,
            productId: productId == null ? null : Number(productId),
            sceneId: sceneId == null ? null : Number(sceneId),
            date,
          } as ViewRouterConfig;
        }), take(1)
      ),
      this.overlayQuery.selectLoading().pipe(filter(p => p === false), take(1)),
      this.productQuery.selectLoading().pipe(filter(p => p === false), take(1))
    ]).pipe(
      map(([viewConfig, foo, bar]) => {
        this.setView(viewConfig.viewPosition);
        this.overlayService.setAllActive(viewConfig.overlays);
        this.productService.setActive(viewConfig.productId);
        if (viewConfig.date) this.productService.setSelectedDate(viewConfig.date);
        this.sceneService.setActive(viewConfig.sceneId);
        return true;
      }),
      catchError((err) => {
        return of(false)
      })
    );
  }

  public connectStoreToRouter() {
    return combineLatest([
      this.overlayQuery.selectActiveUIOverlays().pipe(map(overlays => overlays.map(o => o.id)), distinctUntilChanged()),
      this.productQuery.select().pipe(distinctUntilChanged()),
      this.sceneQuery.selectActive().pipe(distinctUntilChanged()),
      this.mapQuery.select().pipe(map(mv => mv.view), distinctUntilChanged())
    ]).pipe(
      tap(([overlays, product, scene, zoom]) => {
        this.router.navigate([], {
          queryParamsHandling: 'merge',
          queryParams: {
            overlays: overlays.length === 0 ? null : overlays,
            product: product == null ? null : product.active,
            scene: scene == null ? null : scene.id,
            date: product.ui.selectedDate,
            zoom: zoom.zoomLevel,
            centerx: zoom.centerCoordinates[0],
            centery: zoom.centerCoordinates[1]
          }
        });
      })
    );
  }
}

