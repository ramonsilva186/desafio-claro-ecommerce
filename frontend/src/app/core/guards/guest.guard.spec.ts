import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { guestGuard } from './guest.guard';
import { AuthService } from '../services/auth.service';

describe('guestGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'isAuthenticated',
      'getLastAuthenticatedRoute'
    ]);
    router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    });
  });

  it('should allow guests to access login', () => {
    authService.isAuthenticated.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as never, {} as never)
    );

    expect(result).toBeTrue();
  });

  it('should redirect authenticated users to the last authenticated route', () => {
    const urlTree = {} as ReturnType<Router['createUrlTree']>;
    authService.isAuthenticated.and.returnValue(true);
    authService.getLastAuthenticatedRoute.and.returnValue('/pedidos/novo');
    router.createUrlTree.and.returnValue(urlTree);

    const result = TestBed.runInInjectionContext(() =>
      guestGuard({} as never, {} as never)
    );

    expect(result).toBe(urlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/pedidos/novo']);
  });
});