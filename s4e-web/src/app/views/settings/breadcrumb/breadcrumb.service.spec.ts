import { RouterTestingModule } from '@angular/router/testing';
import { TestBed } from '@angular/core/testing';

import { BreadcrumbService } from './breadcrumb.service';
import { ActivatedRoute, ParamMap, convertToParamMap, Route } from '@angular/router';
import { Subject, ReplaySubject } from 'rxjs';
import { IBreadcrumb } from './breadcrumb.model';

class ActivatedRouteStub {
  data: Subject<ParamMap> = new ReplaySubject(1);
  routeConfig: Route = null;
  parent: ActivatedRoute = null;

  constructor() {
    this.data.next(convertToParamMap({}));
  }
}

const NO_PATH_ROUTE: Route = {
  data: {
    breadcrumbs: [
      {
        label: 'No path',
        url: 'noPath'
      }
    ] as IBreadcrumb[]
  }
};
const MULTI_BREADCRUMB_ROUTE: Route = {
  path: 'multiBreadcrumbRoute',
  data: {
    breadcrumbs: [
      {
        label: 'Multi breadcrumb route Parent',
        url: 'otherRoutePath'
      },
      {
        label: 'Multi breadcrumb route'
      }
    ] as IBreadcrumb[]
  }
};
const EMPTY_DATA_ROUTE: Route = {
  path: 'emptyDataRoute',
  data: {}
};
const DEFAULT_ROUTE: Route = {
  path: '**',
  redirectTo: MULTI_BREADCRUMB_ROUTE.path
};
const PARENT_ROUTE: Route = {
  path: 'parentPath',
  children: [
    NO_PATH_ROUTE,
    MULTI_BREADCRUMB_ROUTE,
    EMPTY_DATA_ROUTE,
    DEFAULT_ROUTE
  ],
  data: {}
};

describe('BreadcrumbService', () => {
  let activatedRoute: ActivatedRouteStub;
  let breadcrumbService: BreadcrumbService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      providers: [
        {provide: ActivatedRoute, useClass: ActivatedRouteStub}
      ]
    })
      .compileComponents();

    activatedRoute = <ActivatedRouteStub>TestBed.get(ActivatedRoute);
    breadcrumbService = TestBed.get(BreadcrumbService);

    breadcrumbService.rootPath = PARENT_ROUTE.path;
    breadcrumbService.defaultRoute = MULTI_BREADCRUMB_ROUTE;
  }
  );

  it('should be created', () => {
    expect(breadcrumbService).toBeTruthy();
  });

  it('should update with url', () => {
    const breadcrumbWithUrl = NO_PATH_ROUTE.data.breadcrumbs[0];
    let updatedBreadcrumb = (breadcrumbService as any)
      ._updateWithUrl(breadcrumbWithUrl, NO_PATH_ROUTE.path);
    expect(updatedBreadcrumb).toEqual(breadcrumbWithUrl);

    const breadcrumbWithoutUrl = MULTI_BREADCRUMB_ROUTE.data.breadcrumbs
      .find(breadcrumb => !breadcrumb.url);
    updatedBreadcrumb = (breadcrumbService as any)
      ._updateWithUrl(breadcrumbWithoutUrl, MULTI_BREADCRUMB_ROUTE.path);
    expect(updatedBreadcrumb).toEqual({
      ...breadcrumbWithoutUrl,
      url: '/' + PARENT_ROUTE.path + '/' + MULTI_BREADCRUMB_ROUTE.path
    });
  });
  it('should check if route is start page', () => {
    const breadcrumbsWithDefaults = PARENT_ROUTE.children
      .filter(route => !!route.data && !!route.data.breadcrumbs)
      .map(route => route.data.breadcrumbs)
      .reduce((acc, breadcrumbs) => acc = [...acc, ...breadcrumbs], []);

    expect((breadcrumbService as any)._isStartPageIn(breadcrumbsWithDefaults)).toBeTruthy();

    const breadcrumbsWithoutDefaults = [NO_PATH_ROUTE, EMPTY_DATA_ROUTE]
      .filter(route => !!route.data && !!route.data.breadcrumbs)
      .map(route => route.data.breadcrumbs)
      .filter(breadcrumbs => !!breadcrumbs)
      .reduce((acc, breadcrumbs) => acc = [...acc, ...breadcrumbs], []);
    expect((breadcrumbService as any)._isStartPageIn(breadcrumbsWithoutDefaults)).toBeFalsy();
  });
  it('should check if tree routes has ended', () => {
    activatedRoute.routeConfig = PARENT_ROUTE;
    expect((breadcrumbService as any)._isTreeEnd(activatedRoute as undefined)).toBeTruthy();

    activatedRoute.parent = null;
    expect((breadcrumbService as any)._isTreeEnd(activatedRoute as undefined)).toBeTruthy();

    activatedRoute.routeConfig = MULTI_BREADCRUMB_ROUTE;
    activatedRoute.parent = {} as undefined as ActivatedRoute;
    const exceededMaxDepth = BreadcrumbService._MAX_DEPTH + 1;
    expect((breadcrumbService as any)._isTreeEnd(activatedRoute as undefined, exceededMaxDepth)).toBeTruthy();
    expect((breadcrumbService as any)._isTreeEnd(activatedRoute as undefined)).toBeFalsy();
  });
  it('should add new route and increase depth and update urls', () => {
    const breadcrumbs = [];
    const actualDepth = 5;
    const newDepth = (breadcrumbService as any)._addNew(MULTI_BREADCRUMB_ROUTE, breadcrumbs, actualDepth);

    expect(newDepth).toEqual(actualDepth + 1);
    expect(breadcrumbs.length).toEqual(MULTI_BREADCRUMB_ROUTE.data.breadcrumbs.length);
    expect(breadcrumbs.every(breadcrumb => !!breadcrumb.url)).toBeTruthy();
  });
  it('should return default route', () => {
    const defaultRouteFromRedirect = PARENT_ROUTE.children
      .find(route => route.path === DEFAULT_ROUTE.redirectTo);

    expect((breadcrumbService as any)._getDefaultRouteFrom(PARENT_ROUTE.children)).toEqual(defaultRouteFromRedirect);
  });
});
