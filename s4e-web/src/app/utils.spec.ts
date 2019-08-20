import {activateMatcher} from './utils';
import {UrlSegment} from '@angular/router';

describe('utils', () => {
  it('activate matches correct url', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9dfc-02012fa8e2f4', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toEqual(
      {
        'consumed': urls,
        'posParams':
          {
            token: urls[1]
          }
      }
    );
  });

  it('activate doesn\'t match incorrect token', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9df', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });

  it('activate doesn\'t match incorrect path', () => {
    const urls = [
      new UrlSegment('confirm-email', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9df', {})
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });

  it('activate doesn\'t match too long urls', () => {
    const urls = [
      new UrlSegment('activate', {}),
      new UrlSegment('4aaeb75b-73a9-4e8c-9dfc-02012fa8e2f4', {}),
      new UrlSegment('foo', {}),
    ];
    const result = activateMatcher(urls);
    expect(result).toBe(null);
  });
});
