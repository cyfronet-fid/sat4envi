import { InjectorModule } from './common/injector.module';
import { ProfileQuery } from './state/profile/profile.query';
import {UrlSegment} from '@angular/router';

export function activateMatcher(url: UrlSegment[]) {
  const uuidv4 = new RegExp(/^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$/i);
  if (url.length === 2 && url[0].path === 'activate' && url[1].path.match(uuidv4)) {
    return {
      consumed: url,
      posParams: {
        token: url[1]
      }
    };
  }
  return null;
}

function isDashboardUrl(url: UrlSegment[]) {
  const lastUrlSegment = url[0];
  const isOneSegmentUrl = url.length === 1;
  return !!lastUrlSegment
    && isOneSegmentUrl
    && lastUrlSegment.path === 'dashboard';
}
export function adminDashboardMatcher(url: UrlSegment[]) {
  const profileQuery: ProfileQuery = InjectorModule.Injector.get(ProfileQuery);
  const isAdminDashboard = profileQuery.isAdmin() && isDashboardUrl(url);
  const lastUrlSegment = url[0];
  return isAdminDashboard ? ({consumed: [lastUrlSegment]}) : null;
}
export function managerDashboardMatcher(url: UrlSegment[]) {
  const profileQuery: ProfileQuery = InjectorModule.Injector.get(ProfileQuery);
  const isManagerDashboard = profileQuery.isManager() && !profileQuery.getValue().admin && isDashboardUrl(url);
  const lastUrlSegment = url[0];
  return isManagerDashboard ? ({consumed: [lastUrlSegment]}) : null;
}
