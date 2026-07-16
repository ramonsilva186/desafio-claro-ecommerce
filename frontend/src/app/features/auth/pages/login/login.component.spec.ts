import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should keep the form invalid when email and password are empty', () => {
    expect(component.loginForm.invalid).toBeTrue();

    component.submit();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.emailControl.touched).toBeTrue();
    expect(component.passwordControl.touched).toBeTrue();
  });

  it('should call login and navigate to dashboard on success', () => {
    authService.login.and.returnValue(of({
      token: 'jwt-token',
      tokenType: 'Bearer',
      expiresIn: 3600000
    }));

    component.loginForm.setValue({
      email: 'admin@claro.com',
      password: '123456'
    });
    component.submit();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'admin@claro.com',
      password: '123456'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard'], { state: { showWelcome: true } });
    expect(component.isLoading).toBeFalse();
  });

  it('should show an API error message when login fails', () => {
    authService.login.and.returnValue(
      throwError(() => ({ error: { message: 'Email ou senha invalidos' } }))
    );

    component.loginForm.setValue({
      email: 'admin@claro.com',
      password: 'errada'
    });
    component.submit();

    expect(component.errorMessage).toBe('Email ou senha invalidos');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(component.isLoading).toBeFalse();
  });
});