import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Order } from '../../../../core/models/order';
import { OrderStatus } from '../../../../core/models/order-status';
import { AuthService } from '../../../../core/services/auth.service';
import { OrderService } from '../../../../core/services/order.service';

type StatusFilter = OrderStatus | 'TODOS';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss'
})
export class OrderListComponent implements OnInit {
  readonly orderLimit = 5;
  readonly statusOptions: Array<{ value: StatusFilter; label: string }> = [
    { value: 'TODOS', label: 'Todos' },
    { value: 'EM_PROCESSAMENTO', label: 'Em processamento' },
    { value: 'PAUSADO', label: 'Pausado' },
    { value: 'CANCELADO', label: 'Cancelado' }
  ];

  orders: Order[] = [];
  searchTerm = '';
  statusFilter: StatusFilter = 'TODOS';
  isLoading = false;
  actionOrderId: number | null = null;
  errorMessage = '';
  apiUnavailable = false;

  constructor(
    private readonly orderService: OrderService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  get filteredOrders(): Order[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.orders.filter(order => {
      const matchesStatus =
        this.statusFilter === 'TODOS' || order.status === this.statusFilter;
      const matchesSearch =
        !term || order.displayName.toLowerCase().includes(term);

      return matchesStatus && matchesSearch;
    });
  }

  get canAddOrder(): boolean {
    return this.orders.length < this.orderLimit && !this.apiUnavailable;
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.orderService
      .findAll()
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: orders => {
          this.orders = orders;
          this.apiUnavailable = false;
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401 || error.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
            return;
          }

          this.orders = [];
          this.apiUnavailable = true;
          this.errorMessage = 'API indisponivel. Nao foi possivel carregar os pedidos.';
        }
      });
  }

  updateStatus(order: Order, status: OrderStatus): void {
    if (!this.canTransition(order.status, status) || this.apiUnavailable) {
      return;
    }

    this.actionOrderId = order.id;

    this.orderService
      .updateStatus(order.id, status)
      .pipe(finalize(() => (this.actionOrderId = null)))
      .subscribe({
        next: updatedOrder => {
          this.orders = this.orders.map(currentOrder =>
            currentOrder.id === updatedOrder.id ? updatedOrder : currentOrder
          );
          this.snackBar.open('Status atualizado com sucesso.', 'OK', {
            duration: 3000
          });
        },
        error: (error: HttpErrorResponse) => this.handleActionError(error)
      });
  }

  deleteOrder(order: Order): void {
    const confirmed = window.confirm(
      `Deseja excluir o pedido "${order.displayName}"?`
    );

    if (!confirmed || this.apiUnavailable) {
      return;
    }

    this.actionOrderId = order.id;

    this.orderService
      .delete(order.id)
      .pipe(finalize(() => (this.actionOrderId = null)))
      .subscribe({
        next: () => {
          this.orders = this.orders.filter(currentOrder => currentOrder.id !== order.id);
          this.snackBar.open('Pedido excluido com sucesso.', 'OK', {
            duration: 3000
          });
        },
        error: (error: HttpErrorResponse) => this.handleActionError(error)
      });
  }

  canTransition(currentStatus: OrderStatus, nextStatus: OrderStatus): boolean {
    return this.allowedTransitions(currentStatus).includes(nextStatus);
  }

  statusLabel(status: OrderStatus): string {
    return this.statusOptions.find(option => option.value === status)?.label ?? status;
  }

  weightInKg(weight: number): string {
    return `${(weight / 1000).toFixed(2)} kg`;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private allowedTransitions(status: OrderStatus): OrderStatus[] {
    const transitions: Record<OrderStatus, OrderStatus[]> = {
      EM_PROCESSAMENTO: ['PAUSADO', 'CANCELADO'],
      PAUSADO: ['EM_PROCESSAMENTO', 'CANCELADO'],
      CANCELADO: ['EM_PROCESSAMENTO']
    };

    return transitions[status];
  }

  private handleActionError(error: HttpErrorResponse): void {
    if (error.status === 401 || error.status === 403) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }

    if (error.status === 0) {
      this.orders = [];
      this.apiUnavailable = true;
      this.errorMessage = 'API indisponivel. Nao foi possivel carregar os pedidos.';
    }

    this.snackBar.open(
      error.error?.message ?? 'Nao foi possivel concluir a acao.',
      'OK',
      { duration: 4000 }
    );
  }
}
