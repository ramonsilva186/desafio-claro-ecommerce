import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environments';
import { CreateOrderRequest } from '../models/create-order-request';
import { HealthResponse } from '../models/health-response';
import { Order } from '../models/order';
import { OrderStatus } from '../models/order-status';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly ordersUrl = `${environment.apiUrl}/pedidos`;
  private readonly healthUrl = `${environment.actuatorUrl}/health`;

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<Order[]> {
    return this.http.get<Order[]>(this.ordersUrl);
  }

  create(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.ordersUrl, request);
  }

  updateStatus(id: number, status: OrderStatus): Observable<Order> {
    return this.http.patch<Order>(this.ordersUrl + '/' + id + '/status', { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.ordersUrl + '/' + id);
  }

  checkHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(this.healthUrl);
  }
}
