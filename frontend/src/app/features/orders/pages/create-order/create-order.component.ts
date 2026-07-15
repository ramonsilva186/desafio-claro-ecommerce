import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../../../core/services/auth.service';
import { OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-create-order',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './create-order.component.html',
  styleUrl: './create-order.component.scss'
})
export class CreateOrderComponent implements OnInit {
  readonly orderLimit = 5;

  apiOrderCount = 0;
  isLoading = false;
  isSubmitting = false;
  apiUnavailable = false;
  errorMessage = '';

  readonly orderForm = this.formBuilder.nonNullable.group({
    displayName: ['', [Validators.required, Validators.minLength(5)]],
    weight: [null as number | null, [Validators.required, Validators.min(1)]],
    items: [null as number | null, [Validators.required, Validators.min(1), Validators.pattern(/^[0-9]+$/)]]
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly orderService: OrderService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrderLimit();
  }

  get fallbackOrderCount(): number {
    return this.orderService.getFallbackOrders().length;
  }

  get totalOrders(): number {
    return this.apiOrderCount + this.fallbackOrderCount;
  }

  get isLimitReached(): boolean {
    return this.totalOrders >= this.orderLimit;
  }

  get remainingSlots(): number {
    return Math.max(this.orderLimit - this.totalOrders, 0);
  }

  get displayNameControl() {
    return this.orderForm.controls.displayName;
  }

  get weightControl() {
    return this.orderForm.controls.weight;
  }

  get itemsControl() {
    return this.orderForm.controls.items;
  }

  submit(): void {
    if (this.orderForm.invalid || this.isSubmitting || this.isLimitReached) {
      this.orderForm.markAllAsTouched();
      return;
    }

    const rawValue = this.orderForm.getRawValue();
    const request = {
      displayName: rawValue.displayName.trim(),
      weight: Number(rawValue.weight),
      items: Number(rawValue.items)
    };

    this.isSubmitting = true;
    this.errorMessage = '';

    this.orderService
      .create(request)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.apiOrderCount += 1;
          this.orderForm.reset();
          this.snackBar.open('Pedido cadastrado com sucesso.', 'OK', {
            duration: 3000
          });
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401 || error.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
            return;
          }

          if (error.status === 0) {
            this.orderService.saveFallbackOrder(request);
            this.orderForm.reset();
            this.apiUnavailable = true;
            this.snackBar.open(
              'API indisponivel. Pedido salvo localmente.',
              'OK',
              { duration: 4000 }
            );
            return;
          }

          this.errorMessage =
            error.error?.message ?? 'Nao foi possivel cadastrar o pedido.';
        }
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private loadOrderLimit(): void {
    this.isLoading = true;

    this.orderService
      .findAll()
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: orders => {
          this.apiOrderCount = orders.length;
          this.apiUnavailable = false;
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 401 || error.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
            return;
          }

          this.apiOrderCount = 0;
          this.apiUnavailable = true;
        }
      });
  }
}
