import {UrlSegment} from '@angular/router';
import {InjectorModule} from 'src/app/common/injector.module';
import {BreadcrumbService} from './breadcrumb/breadcrumb.service';
import {SessionQuery} from '../../state/session/session.query';
import {InstitutionQuery} from './state/institution/institution.query';

export function multipleInstitutionAdminDashboardMatcher(url: UrlSegment[]) {
  const institutionQuery = InjectorModule.Injector.get(InstitutionQuery);
  const isMultipleInstitutionDashboard = institutionQuery.getAdministrationInstitutions().length > 1
    && (isDashboardUrl(url) || isEmptyUrl(url));
  if (!isMultipleInstitutionDashboard) {
    return null;
  }

  const lastUrlSegment = url[0];
  const nextUrl = isEmptyUrl(url)
    && new UrlSegment('dashboard', {})
    || lastUrlSegment;
  return ({consumed: [nextUrl]});
}

export function singleInstitutionAdminDashboardMatcher(url: UrlSegment[]) {
  const institutionQuery = InjectorModule.Injector.get(InstitutionQuery);
  const administrativeInstitutions = institutionQuery.getAdministrationInstitutions();
  const redirectToSingleInstitutionDashboard = (isDashboardUrl(url) || isEmptyUrl(url))
    && administrativeInstitutions.length === 1;
  if (!redirectToSingleInstitutionDashboard) {
    return null;
  }

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
