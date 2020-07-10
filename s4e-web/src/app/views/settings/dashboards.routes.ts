import {UrlSegment} from '@angular/router';
import {InjectorModule} from 'src/app/common/injector.module';
import {BreadcrumbService} from './breadcrumb/breadcrumb.service';
import {SessionQuery} from '../../state/session/session.query';

export function adminDashboardMatcher(url: UrlSegment[]) {
  const sessionQuery: SessionQuery = InjectorModule.Injector.get(SessionQuery);
  const isAdminDashboard = sessionQuery.isAdmin() && (isDashboardUrl(url) || isEmptyUrl(url));
  const settingsRoutes = InjectorModule.Injector.get(BreadcrumbService).getMainRoutes();

  if (isAdminDashboard) {
    const breadcrumbService: BreadcrumbService = InjectorModule.Injector.get(BreadcrumbService);
    const adminDashboardRoute = settingsRoutes
      .reduce((routes, route) => routes = [...routes, route, ...route.children], [])
      .find(route => !!route.data && route.data.isAdminDashboard);
    breadcrumbService.defaultRoute = !!adminDashboardRoute
      ? adminDashboardRoute
      : breadcrumbService.defaultRoute;
  }

  const lastUrlSegment = url[0];
  const nextUrl = isEmptyUrl(url) && new UrlSegment('dashboard', {}) || lastUrlSegment;
  return isAdminDashboard ? ({consumed: [nextUrl]}) : null;
}

export function managerDashboardMatcher(url: UrlSegment[]) {
  const sessionQuery: SessionQuery = InjectorModule.Injector.get(SessionQuery);
  const isManagerDashboard = sessionQuery.isManager() && !sessionQuery.getValue().admin && (isDashboardUrl(url) || isEmptyUrl(url));
  const settingsRoutes = InjectorModule.Injector.get(BreadcrumbService).getMainRoutes();

  if (isManagerDashboard && !sessionQuery.hasOnlyGroupMemberRole()) {
    const breadcrumbService: BreadcrumbService = InjectorModule.Injector.get(BreadcrumbService);
    const managerDashboardRoute = settingsRoutes
      .reduce((routes, route) => routes = [...routes, route, ...route.children], [])
      .find(route => !!route.data && 'isAdminDashboard' in route.data && !route.data.isAdminDashboard);
    breadcrumbService.defaultRoute = !!managerDashboardRoute
      ? managerDashboardRoute
      : breadcrumbService.defaultRoute;
  }

  const lastUrlSegment = url[0];
  const nextUrl = isEmptyUrl(url) && new UrlSegment('dashboard', {}) || lastUrlSegment;
  return isManagerDashboard ? ({consumed: [nextUrl]}) : null;
}

function isDashboardUrl(url: UrlSegment[]) {
  const lastUrlSegment = url[0];
  const isOneSegmentUrl = url.length === 1;
  return !!lastUrlSegment
    && isOneSegmentUrl
    && lastUrlSegment.path === 'dashboard';
}

function isEmptyUrl(url: UrlSegment[]) {
  return url.length === 0;
}
