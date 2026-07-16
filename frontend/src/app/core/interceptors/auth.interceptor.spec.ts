import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environments';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should attach bearer token to API requests', () => {
    authService.getToken.and.returnValue('jwt-token');

    http.get(`${environment.apiUrl}/pedidos`).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush([]);
  });

  it('should not attach token to non API requests', () => {
    authService.getToken.and.returnValue('jwt-token');

    http.get(`${environment.actuatorUrl}/health`).subscribe();

    const request = httpMock.expectOne(`${environment.actuatorUrl}/health`);
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({ status: 'UP' });
  });

  it('should not attach authorization when token is missing', () => {
    authService.getToken.and.returnValue(null);

    http.get(`${environment.apiUrl}/pedidos`).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos`);
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush([]);
  });
});