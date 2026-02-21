import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderSummary, OrderFullResponse } from '../models/order';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = 'http://localhost:8080/orders';

  constructor(private http: HttpClient) { }

  findAll(): Observable<OrderSummary[]> {
    return this.http.get<OrderSummary[]>(this.apiUrl);
  }

  findById(id: string): Observable<OrderFullResponse> {
    return this.http.get<OrderFullResponse>(`${this.apiUrl}/${id}`);
  }

  create(order: any): Observable<OrderFullResponse> {
    return this.http.post<OrderFullResponse>(this.apiUrl, order);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateStatus(id: string, newStatus: string): Observable<OrderFullResponse> {
    return this.http.patch<OrderFullResponse>(`${this.apiUrl}/${id}/status?newStatus=${newStatus}`, {});
  }
}