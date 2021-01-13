/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {UrlSegment} from '@angular/router';
import {InjectorModule} from 'src/app/common/injector.module';
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
