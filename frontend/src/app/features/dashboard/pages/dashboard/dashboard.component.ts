import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Order } from '../../../../core/models/order';
import { OrderStatus } from '../../../../core/models/order-status';
import { AuthService } from '../../../../core/services/auth.service';
import { OrderService } from '../../../../core/services/order.service';

interface StatusSummary {
  status: OrderStatus;
  label: string;
  total: number;
  color: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  readonly orderLimit = 5;

  orders: Order[] = [];
  isLoading = false;
  errorMessage = '';
  apiStatus = 'VERIFICANDO';

  constructor(
    private readonly orderService: OrderService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.loadHealth();
  }

  get totalOrders(): number {
    return this.orders.length;
  }

  get processingOrders(): number {
    return this.countByStatus('EM_PROCESSAMENTO');
  }

  get pausedOrders(): number {
    return this.countByStatus('PAUSADO');
  }

  get canceledOrders(): number {
    return this.countByStatus('CANCELADO');
  }

  get totalItems(): number {
    return this.orders.reduce((total, order) => total + order.items, 0);
  }

  get totalWeightInKg(): string {
    const totalInGrams = this.orders.reduce(
      (total, order) => total + order.weight,
      0
    );

    return (totalInGrams / 1000).toFixed(2);
  }

  get remainingSlots(): number {
    return Math.max(this.orderLimit - this.totalOrders, 0);
  }

  get usagePercent(): number {
    return Math.min((this.totalOrders / this.orderLimit) * 100, 100);
  }

  get pieChartStyle(): Record<string, string> {
    return {
      background: `conic-gradient(#d71920 0 ${this.usagePercent}%, #e8edf3 ${this.usagePercent}% 100%)`
    };
  }

  get statusSummaries(): StatusSummary[] {
    return [
      {
        status: 'EM_PROCESSAMENTO',
        label: 'Em processamento',
        total: this.processingOrders,
        color: '#1f8a5b'
      },
      {
        status: 'PAUSADO',
        label: 'Pausados',
        total: this.pausedOrders,
        color: '#d89a00'
      },
      {
        status: 'CANCELADO',
        label: 'Cancelados',
        total: this.canceledOrders,
        color: '#c62828'
      }
    ];
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.orderService
      .findAll()
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: orders => {
          this.orders = orders;
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401 || error.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
            return;
          }

          this.errorMessage =
            'Nao foi possivel carregar os indicadores de pedidos.';
        }
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  barHeight(total: number): string {
    const maxTotal = Math.max(
      ...this.statusSummaries.map(summary => summary.total),
      1
    );

    return `${Math.max((total / maxTotal) * 100, total > 0 ? 12 : 4)}%`;
  }

  private loadHealth(): void {
    this.orderService.checkHealth().subscribe({
      next: response => {
        this.apiStatus = response.status;
      },
      error: () => {
        this.apiStatus = 'INDISPONIVEL';
      }
    });
  }

  private countByStatus(status: OrderStatus): number {
    return this.orders.filter(order => order.status === status).length;
  }
}
