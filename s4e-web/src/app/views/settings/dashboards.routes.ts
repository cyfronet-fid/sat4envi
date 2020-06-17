import { UrlSegment } from '@angular/router';
import { ProfileQuery } from 'src/app/state/profile/profile.query';
import { InjectorModule } from 'src/app/common/injector.module';
import { BreadcrumbService } from './breadcrumb/breadcrumb.service';
import { settingsRoutes } from './settings.routes';

export function adminDashboardMatcher(url: UrlSegment[]) {
  const profileQuery: ProfileQuery = InjectorModule.Injector.get(ProfileQuery);
  const isAdminDashboard = profileQuery.isAdmin() && (isDashboardUrl(url) || isEmptyUrl(url));

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
  const profileQuery: ProfileQuery = InjectorModule.Injector.get(ProfileQuery);
  const isManagerDashboard = profileQuery.isManager() && !profileQuery.getValue().admin && (isDashboardUrl(url) || isEmptyUrl(url));
  if (isManagerDashboard && !profileQuery.hasOnlyGroupMemberRole()) {
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
