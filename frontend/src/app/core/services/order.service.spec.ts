import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environments';
import { OrderService } from './order.service';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should list orders', () => {
    service.findAll().subscribe(orders => {
      expect(orders.length).toBe(1);
      expect(orders[0].displayName).toBe('Pedido #1 - Joao Silva');
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos`);
    expect(request.request.method).toBe('GET');
    request.flush([
      {
        id: 1,
        displayName: 'Pedido #1 - Joao Silva',
        items: 2,
        weight: 1024,
        status: 'EM_PROCESSAMENTO',
        createdAt: '2026-07-16T00:00:00',
        updatedAt: '2026-07-16T00:00:00'
      }
    ]);
  });

  it('should create an order', () => {
    const payload = { displayName: 'Ramon Silva', items: 2, weight: 500 };

    service.create(payload).subscribe(order => {
      expect(order.id).toBe(4);
    });

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ id: 4, ...payload, status: 'EM_PROCESSAMENTO' });
  });

  it('should update order status', () => {
    service.updateStatus(1, 'PAUSADO').subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos/1/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ status: 'PAUSADO' });
    request.flush({});
  });

  it('should delete an order', () => {
    service.delete(1).subscribe();

    const request = httpMock.expectOne(`${environment.apiUrl}/pedidos/1`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('should check API health', () => {
    service.checkHealth().subscribe(response => {
      expect(response.status).toBe('UP');
    });

    const request = httpMock.expectOne(`${environment.actuatorUrl}/health`);
    expect(request.request.method).toBe('GET');
    request.flush({ status: 'UP' });
  });
});