import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environments';
import { HealthResponse } from '../models/health-response';
import { Order } from '../models/order';

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

  checkHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(this.healthUrl);
  }
}
