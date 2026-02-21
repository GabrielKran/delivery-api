// DTO levinho para a Tabela Principal
export interface OrderSummary {
  id: string;
  total_price: number;
  last_status_name: string;
  created_at: number;
  customer_name: string;
}

// A resposta completa que vem do findById (O Envelope)
export interface OrderFullResponse {
  store_id: string;
  order_id: string;
  order: OrderDetails;
}

// O miolo do pedido com todos os detalhes
export interface OrderDetails {
  total_price: number;
  last_status_name: string;
  created_at: number;
  customer: Customer;
  delivery_address: DeliveryAddress;
  store: Store;
  items: OrderItem[];
  payments: OrderPayment[];
  statuses: OrderStatusHistory[];
}

export interface Customer {
  name: string;
  temporary_phone: string;
}

export interface DeliveryAddress {
  reference: string;
  street_name: string;
  postal_code: string;
  country: string;
  city: string;
  neighborhood: string;
  street_number: string;
  state: string;
  coordinates: {
    id: number;
    longitude: number;
    latitude: number;
  };
}

export interface Store {
  id: string;
  name: string;
}

export interface OrderItem {
  code: number;
  name: string;
  price: number;
  quantity: number;
  total_price: number;
  observations: string | null;
  discount: number;
  condiments: string[];
}

export interface OrderPayment {
  prepaid: boolean;
  value: number;
  origin: string;
}

export interface OrderStatusHistory {
  name: string;
  created_at: number;
  origin: string;
}