import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/login/login.component')
        .then((m) => m.LoginComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/pages/dashboard/dashboard.component')
        .then((m) => m.DashboardComponent)
  },
  {
    path: 'pedidos',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/pages/order-list/order-list.component')
        .then((m) => m.OrderListComponent)
  },
  {
    path: 'pedidos/novo',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/orders/pages/create-order/create-order.component')
        .then((m) => m.CreateOrderComponent)
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
