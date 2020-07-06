import { map, switchMap } from 'rxjs/operators';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { IBreadcrumb } from './breadcrumb.model';
import { Routes, Route, ActivatedRoute } from '@angular/router';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  static _MAX_DEPTH = 8;

  public defaultRoute: Route;
  public rootPath = '';

  public breadcrumbs$: Observable<IBreadcrumb[]>;
  protected _breadcrumbSubject$: BehaviorSubject<IBreadcrumb[]> = new BehaviorSubject<IBreadcrumb[]>([]);

  constructor() {
    this.breadcrumbs$ = this._breadcrumbSubject$.asObservable();
  }

  registerRoutes(childrenRoutes: Routes, rootPath = '') {
    this.defaultRoute = this._getDefaultRouteFrom(childrenRoutes);
    this.rootPath = rootPath;
  }
  loadFrom(activatedRoute: ActivatedRoute) {
    return this._getBreadcrumbsFrom(this._getActualRouteFrom(activatedRoute))
      .subscribe((breadcrumbs: IBreadcrumb[]) => this._breadcrumbSubject$.next(breadcrumbs));
  }
  replaceWith(breadcrumbs: IBreadcrumb[]) {
    if (this._breadcrumbSubject$.value !== breadcrumbs) {
      this._breadcrumbSubject$.next(breadcrumbs);
    }
  }

  protected _getBreadcrumbsFrom(childRoute: ActivatedRoute, breadcrumbs: IBreadcrumb[] = [], depth = 0): Observable<IBreadcrumb[]> {
    return childRoute.data
      .pipe(switchMap(() => this._toBreadcrumbs(childRoute, breadcrumbs, depth)));
  }
  protected _getActualRouteFrom(activatedRoute: ActivatedRoute) {
    while (activatedRoute.firstChild) {
      activatedRoute = activatedRoute.firstChild;
    }
    return activatedRoute;
  }
  protected _toBreadcrumbs(childRoute: ActivatedRoute, breadcrumbs: IBreadcrumb[], depth: number) {
    const routeConfig = childRoute.routeConfig;
    if (this._isTreeEnd(childRoute, depth)) {
      return this._withDefault$(breadcrumbs);
    }

    depth = this._addNew(routeConfig, breadcrumbs, depth);
    return this._isRoot(childRoute)
      ? this._withDefault$(breadcrumbs)
      : this._getBreadcrumbsFrom(childRoute.parent, breadcrumbs, depth);
  }
  protected _getDefaultRouteFrom(routes: Routes): Route {
    const redirectRoute = routes.find((route: Route) => route.path === '**');
    return !!redirectRoute
      ? routes.find((route: Route) => route.path === redirectRoute.redirectTo)
      : null;
  }
  protected _withDefault$(breadcrumbs: IBreadcrumb[]): Observable<IBreadcrumb[]> {
    if (!this._isStartPageIn(breadcrumbs)) {
      this._addNew(this.defaultRoute, breadcrumbs);
    }

    return of(breadcrumbs);
  }
  protected _addNew(routeConfig: Route, breadcrumbs: IBreadcrumb[], depth = 0) {
    breadcrumbs.unshift(...this._updateAllWithUrl(routeConfig.data.breadcrumbs, routeConfig.path));
    return ++depth;
  }
  protected _isTreeEnd(route: ActivatedRoute, depth = 0) {
    const routeConfig = route.routeConfig;
    const hasBreadcrumbs = !!routeConfig.data && !!routeConfig.data.breadcrumbs;
    return !hasBreadcrumbs || !route.parent || this._isMaxDepth(depth);
  }
  protected _isStartPageIn(routeBreadcrumbs: IBreadcrumb[]) {
    const hasDefaultRouteBreadcrumbs = !!this.defaultRoute
      && !!this.defaultRoute.data
      && !!this.defaultRoute.data.breadcrumbs;
    if (!hasDefaultRouteBreadcrumbs) {
      console.warn('WARNING!!! Breadcrumbs default route has not been set!');
      return routeBreadcrumbs;
    }

    const defaultBreadcrumbsLabels = this.defaultRoute.data.breadcrumbs
      .map(breadcrumb => breadcrumb.label);
    return routeBreadcrumbs
      .some(breadcrumb => defaultBreadcrumbsLabels.indexOf(breadcrumb.label) > -1);
  }
  protected _isRoot(route: ActivatedRoute) {
    return route.routeConfig.path === this.rootPath;
  }
  protected _updateAllWithUrl(breadcrumbs: IBreadcrumb[], actualSegmentPath: string) {
    return breadcrumbs.map(breadcrumb => this._updateWithUrl(breadcrumb, actualSegmentPath));
  }
  protected _updateWithUrl(breadcrumb: IBreadcrumb, actualSegmentPath: string): IBreadcrumb {
    const url = '/' + this.rootPath + '/' + actualSegmentPath;
    return !!breadcrumb.url && breadcrumb.url !== ''
      ? breadcrumb
      : {...breadcrumb, url};
  }
  protected _isMaxDepth(depth) {
    return depth >= BreadcrumbService._MAX_DEPTH;
  }
}
