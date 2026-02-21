import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { OrderSummary } from '../../models/order';

@Component({
  selector: 'app-order-list',
  standalone: true,
  template: `<h1>Olhe o Console do Navegador (F12)</h1>`,
  imports: []
})
export class OrderListComponent implements OnInit {
  orders: OrderSummary[] = [];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.orderService.findAll().subscribe({
      next: (data) => {
        console.log('PEDIDOS RECEBIDOS DO BACKEND:', data);
        this.orders = data;
      },
      error: (err) => console.error('ERRO AO CHAMAR API:', err)
    });
  }
}