import {async, TestBed} from '@angular/core/testing';
import {AuthInterceptor} from './auth.interceptor';
import {SessionQuery} from '../../state/session/session.query';
import {HttpEvent, HttpHandler, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';

class MockHttpHandler extends HttpHandler {
  handle(req: HttpRequest<any>): Observable<HttpEvent<any>> {
    return of(null);
  }
}

describe('AuthInterceptor', function () {
  let interceptor;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      providers: [SessionQuery, AuthInterceptor],
    });
    interceptor = TestBed.get(AuthInterceptor);
  }));

  it('should send token header if user is logged in', () => {
    const query = TestBed.get(SessionQuery);
    const mockToken = 'mysecrettocken';
    jest.spyOn(query, 'isLoggedIn').mockReturnValueOnce(true);
    jest.spyOn(query, 'getToken').mockReturnValueOnce(mockToken);

    const request = new HttpRequest('GET', '/api/resource');
    const httpHandler = new MockHttpHandler();

    const httpHandleSpy = jest.spyOn(httpHandler, 'handle');

    interceptor.intercept(request, httpHandler);

    expect(httpHandleSpy).toHaveBeenCalledWith(request.clone({
      setHeaders: {'Authorization': `Bearer ${mockToken}`}
    }));
  });

  it('should not change request if user is not logged in', () => {
    const query = TestBed.get(SessionQuery);
    jest.spyOn(query, 'isLoggedIn').mockReturnValueOnce(false);
    jest.spyOn(query, 'getToken').mockReturnValueOnce(null);

    const request = new HttpRequest('GET', '/api/resource');
    const httpHandler = new MockHttpHandler();

    const httpHandleSpy = jest.spyOn(httpHandler, 'handle');

    interceptor.intercept(request, httpHandler);

    expect(httpHandleSpy).toHaveBeenCalledWith(request);
  });
});
