import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environments';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'access_token';
  private readonly lastAuthenticatedRouteKey = 'last_authenticated_route';

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => this.storeToken(response.token))
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.lastAuthenticatedRouteKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  saveLastAuthenticatedRoute(url: string): void {
    if (url && url !== '/login') {
      localStorage.setItem(this.lastAuthenticatedRouteKey, url);
    }
  }

  getLastAuthenticatedRoute(): string {
    return localStorage.getItem(this.lastAuthenticatedRouteKey) ?? '/dashboard';
  }

  private storeToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }
}
