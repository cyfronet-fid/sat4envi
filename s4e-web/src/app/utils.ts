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
