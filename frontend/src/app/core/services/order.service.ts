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
  private readonly fallbackOrdersKey = 'fallback_orders';
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

  getFallbackOrders(): Order[] {
    const storedOrders = localStorage.getItem(this.fallbackOrdersKey);

    if (!storedOrders) {
      return [];
    }

    try {
      return JSON.parse(storedOrders) as Order[];
    } catch {
      localStorage.removeItem(this.fallbackOrdersKey);
      return [];
    }
  }

  deleteFallbackOrder(id: number): void {
    const orders = this.getFallbackOrders().filter(order => order.id !== id);

    localStorage.setItem(this.fallbackOrdersKey, JSON.stringify(orders));
  }

  saveFallbackOrder(request: CreateOrderRequest): Order {
    const now = new Date().toISOString();
    const order: Order = {
      id: -Date.now(),
      displayName: request.displayName,
      items: request.items,
      weight: request.weight,
      status: 'EM_PROCESSAMENTO',
      createdAt: now,
      updatedAt: now
    };
    const orders = [...this.getFallbackOrders(), order];

    localStorage.setItem(this.fallbackOrdersKey, JSON.stringify(orders));

    return order;
  }
}
