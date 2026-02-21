export interface OrderSummary {
  id: string;
  totalPrice: number;
  lastStatusName: string;
  createdAt: number;
}

export interface Order {
  id: string;
  totalPrice: number;
  lastStatusName: string;
  createdAt: number;
  items: any[];
  statuses: any[];
}