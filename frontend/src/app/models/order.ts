// DTO levinho para a Tabela Principal
export interface OrderSummary {
  id: string;
  totalPrice: number;
  lastStatusName: string;
  createdAt: number;
}

// Entidade completa para a Aba de Detalhes
export interface Order {
  id: string;
  totalPrice: number;
  lastStatusName: string;
  createdAt: number;
  items: OrderItem[];
  payments: OrderPayment[]; // <--- OLHA ELE AQUI!
  statuses: OrderStatusHistory[];
}

// Sub-tipos para o TypeScript te ajudar com o autocompletar:
export interface OrderItem {
  id: number;
  name: string;
  price: number;
  quantity: number;
  totalPrice: number;
  observations: string | null;
}

export interface OrderPayment {
  id: number;
  prepaid: boolean;
  value: number;
  origin: string;
}

export interface OrderStatusHistory {
  id: number;
  name: string;
  createdAt: number;
}