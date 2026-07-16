import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environments';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should store the token after login', () => {
    service.login({ email: 'admin@claro.com', password: '123456' }).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    request.flush({ token: 'jwt-token', tokenType: 'Bearer', expiresIn: 3600000 });

    expect(service.getToken()).toBe('jwt-token');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('should clear token and last route on logout', () => {
    localStorage.setItem('access_token', 'jwt-token');
    localStorage.setItem('last_authenticated_route', '/pedidos');

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getLastAuthenticatedRoute()).toBe('/dashboard');
  });

  it('should not save login as last authenticated route', () => {
    service.saveLastAuthenticatedRoute('/login');

    expect(service.getLastAuthenticatedRoute()).toBe('/dashboard');
  });

  it('should save the last authenticated route', () => {
    service.saveLastAuthenticatedRoute('/pedidos/novo');

    expect(service.getLastAuthenticatedRoute()).toBe('/pedidos/novo');
  });
});