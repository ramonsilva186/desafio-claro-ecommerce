import { OrderStatus } from './order-status';

export interface Order {
  id: number;
  displayName: string;
  items: number;
  weight: number;
  totalWeight: number;
  status: OrderStatus;
  createdAt: string;
  updatedAt: string;
}
