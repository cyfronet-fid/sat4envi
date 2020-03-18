import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MapStore} from './map.store';
import {MapQuery} from './map.query';
import {S4eConfig} from '../../../../utils/initializer/config.service';
import {ViewPosition} from './map.model';
import {forkJoin, of} from 'rxjs';
import {catchError, filter, map, take, tap} from 'rxjs/operators';
import {OverlayQuery} from '../overlay/overlay.query';
import {SceneService} from '../scene/scene.service';
import {ProductQuery} from '../product/product.query';
import {SceneQuery} from '../scene/scene.query.service';
import {ActivatedRoute, Router} from '@angular/router';
import {OverlayService} from '../overlay/overlay.service';
import {ProductService} from '../product/product.service';
import {ViewRouterConfig} from '../view-configuration/view-configuration.model';

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

  toggleLoginOptions(open: boolean = true) {
    this.store.update({loginOptionsOpened: open});
  }

  setWorking($event: boolean) {
    this.store.setLoading($event);
  }

  setView(view: ViewPosition): void {
    this.store.update({view});
  }

  public connectRouterToStore(route: ActivatedRoute) {


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
        this.updateStoreByView(viewConfig);
        return true;
      }),
      catchError((err) => {
        return of(false);
      })
    );
  }

  public updateStoreByView(viewConfig: ViewRouterConfig) {
    this.setView(viewConfig.viewPosition);
    this.overlayService.setAllActive(viewConfig.overlays);
    this.productService.setActive(viewConfig.productId);
    if (viewConfig.date) {
      this.productService.setSelectedDate(viewConfig.date);
    }
    this.sceneService.setActive(viewConfig.sceneId);
  }

  public connectStoreToRouter() {
    return this.mapQuery.selectQueryParamsFromStore().pipe(
      tap((queryParams) => {
        this.router.navigate([], {
          queryParamsHandling: 'merge',
          queryParams: queryParams
        });
      })
    );
  }
}

