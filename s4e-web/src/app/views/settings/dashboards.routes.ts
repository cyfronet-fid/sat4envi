import {UrlSegment} from '@angular/router';
import {InjectorModule} from 'src/app/common/injector.module';
import {BreadcrumbService} from './breadcrumb/breadcrumb.service';
import {SessionQuery} from '../../state/session/session.query';
import {InstitutionQuery} from './state/institution/institution.query';

export function multipleInstitutionAdminDashboardMatcher(url: UrlSegment[]) {
  const institutionQuery = InjectorModule.Injector.get(InstitutionQuery);
  const settingsRoutes = InjectorModule.Injector.get(BreadcrumbService).getMainRoutes();

  const redirectToManyInstitutionsDashboard = (isDashboardUrl(url) || isEmptyUrl(url))
    && institutionQuery.getAdministrationInstitutions().length > 1;
  if (!redirectToManyInstitutionsDashboard) {
    return null;
  }

  // set default breadcrumb
  const breadcrumbService: BreadcrumbService = InjectorModule.Injector.get(BreadcrumbService);
  const adminDashboardRoute = settingsRoutes
    .reduce((routes, route) => routes = [...routes, route, ...route.children], [])
    .find(route => !!route.data && route.data.isAdminDashboard);
  breadcrumbService.defaultRoute = !!adminDashboardRoute
    ? adminDashboardRoute
    : breadcrumbService.defaultRoute;

  // redirect
  const lastUrlSegment = url[0];
  const nextUrl = isEmptyUrl(url)
    && new UrlSegment('dashboard', {})
    || lastUrlSegment;
  return ({consumed: [nextUrl]});
}

export function singleInstitutionAdminDashboardMatcher(url: UrlSegment[]) {
  const institutionQuery = InjectorModule.Injector.get(InstitutionQuery);
  const settingsRoutes = InjectorModule.Injector.get(BreadcrumbService).getMainRoutes();

  const administrativeInstitutions = institutionQuery.getAdministrationInstitutions();
  const redirectToSingleInstitutionDashboard = (isDashboardUrl(url) || isEmptyUrl(url))
    && administrativeInstitutions.length === 1;
  if (!redirectToSingleInstitutionDashboard) {
    return null;
  }

  // Set default dashboard
  const breadcrumbService: BreadcrumbService = InjectorModule.Injector.get(BreadcrumbService);
  const managerDashboardRoute = settingsRoutes
    .reduce((routes, route) => routes = [...routes, route, ...route.children], [])
    .find(route => !!route.data && 'isAdminDashboard' in route.data && !route.data.isAdminDashboard);
  breadcrumbService.defaultRoute = !!managerDashboardRoute
    ? managerDashboardRoute
    : breadcrumbService.defaultRoute;

  // redirect
  const lastUrlSegment = url[0];
  const nextUrl = isEmptyUrl(url)
    && new UrlSegment('dashboard', {})
    || lastUrlSegment;
  return ({consumed: [nextUrl]});
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
