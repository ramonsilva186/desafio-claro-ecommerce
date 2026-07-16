import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter, Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../../../core/services/auth.service';
import { OrderService } from '../../../../core/services/order.service';
import { Order } from '../../../../core/models/order';

const ordersMock: Order[] = [
  {
    id: 1,
    displayName: 'Pedido #1 - Joao Silva',
    items: 2,
    weight: 1024,
    totalWeight: 2048,
    status: 'EM_PROCESSAMENTO',
    createdAt: '2026-07-16T00:00:00',
    updatedAt: '2026-07-16T00:00:00'
  },
  {
    id: 2,
    displayName: 'Pedido #2 - Maria Souza',
    items: 1,
    weight: 512,
    totalWeight: 512,
    status: 'PAUSADO',
    createdAt: '2026-07-16T00:00:00',
    updatedAt: '2026-07-16T00:00:00'
  },
  {
    id: 3,
    displayName: 'Pedido #3 - Carlos Lima',
    items: 4,
    weight: 2048,
    totalWeight: 8192,
    status: 'CANCELADO',
    createdAt: '2026-07-16T00:00:00',
    updatedAt: '2026-07-16T00:00:00'
  }
];

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let orderService: jasmine.SpyObj<OrderService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    orderService = jasmine.createSpyObj<OrderService>('OrderService', [
      'findAll',
      'checkHealth'
    ]);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['logout']);

    orderService.findAll.and.returnValue(of(ordersMock));
    orderService.checkHealth.and.returnValue(of({ status: 'UP' }));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent, NoopAnimationsModule],
      providers: [
        provideRouter([]),
        { provide: OrderService, useValue: orderService },
        { provide: AuthService, useValue: authService }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load orders and API status on init', () => {
    fixture.detectChanges();

    expect(orderService.findAll).toHaveBeenCalled();
    expect(orderService.checkHealth).toHaveBeenCalled();
    expect(component.orders).toEqual(ordersMock);
    expect(component.apiStatus).toBe('UP');
  });

  it('should calculate dashboard totals', () => {
    component.orders = ordersMock;

    expect(component.totalOrders).toBe(3);
    expect(component.processingOrders).toBe(1);
    expect(component.pausedOrders).toBe(1);
    expect(component.canceledOrders).toBe(1);
    expect(component.totalItems).toBe(7);
    expect(component.totalWeightInKg).toBe('10.75');
    expect(component.remainingSlots).toBe(2);
    expect(component.usagePercent).toBe(60);
  });

  it('should generate pie chart style based on usage percent', () => {
    component.orders = ordersMock;

    expect(component.pieChartStyle['background']).toContain('60%');
    expect(component.pieChartStyle['background']).toContain('conic-gradient');
  });

  it('should keep a minimum visible bar height', () => {
    component.orders = ordersMock;

    expect(component.barHeight(0)).toBe('4%');
    expect(component.barHeight(1)).toBe('100%');
  });

  it('should clear orders and show an error when dashboard loading fails', () => {
    orderService.findAll.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 0 }))
    );

    component.orders = ordersMock;
    component.loadDashboard();

    expect(component.orders).toEqual([]);
    expect(component.errorMessage).toBe('Nao foi possivel carregar os indicadores de pedidos.');
  });

  it('should logout and redirect to login when dashboard returns unauthorized', () => {
    orderService.findAll.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401 }))
    );

    component.loadDashboard();

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should mark API as unavailable when health check fails', () => {
    orderService.checkHealth.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 0 }))
    );

    component.refreshDashboard();

    expect(component.apiStatus).toBe('INDISPONIVEL');
  });
});