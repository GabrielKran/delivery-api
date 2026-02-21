import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderSummary } from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/orders';

  constructor(private http: HttpClient) { }

  // GET /orders (Retorna a lista de DTOs)
  findAll(): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(this.apiUrl);
  }

  // GET /orders/{id} (Busca um pedido detalhado)
  findById(id: string): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  // POST /orders (Cria um novo pedido)
  create(order: Order): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order);
  }

  // DELETE /orders/{id} (Remove um pedido)
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // PATCH /orders/{id}/status?newStatus=... (Máquina de Estados)
  updateStatus(id: string, newStatus: string): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${id}/status?newStatus=${newStatus}`, {});
  }
}