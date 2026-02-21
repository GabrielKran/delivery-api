import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order';

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css'
})
export class OrderDetailsComponent implements OnChanges {
  @Input() orderId!: string | null; 
  @Output() closeDetails = new EventEmitter<void>();

  orderFull: Order | null = null;
  isLoading: boolean = false;
  isClosing: boolean = false;

  constructor(
    private orderService: OrderService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['orderId'] && this.orderId) {
      this.isClosing = false; 
      this.buscarDetalhes(this.orderId);
    }
  }

  buscarDetalhes(id: string): void {
    this.isLoading = true;
    this.orderService.findById(id).subscribe({
      next: (data) => {
        this.orderFull = data;
        this.isLoading = false;
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error('Erro ao buscar o pedido completo:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  fechar(): void {
    this.isClosing = true; 
    setTimeout(() => {
      this.closeDetails.emit();
    }, 300); 
  }

  // --- TRADUTORES PARA A INTERFACE ---
  getStatusNome(status: string): string {
    const statusMap: any = { 'RECEIVED': 'Recebido', 'CONFIRMED': 'Confirmado', 'DISPATCHED': 'Despachado', 'DELIVERED': 'Entregue', 'CANCELED': 'Cancelado' };
    return statusMap[status] || status;
  }

  getStatusCor(status: string): string {
    const colorMap: any = { 'RECEIVED': 'bg-secondary', 'CONFIRMED': 'bg-info text-dark', 'DISPATCHED': 'bg-warning text-dark', 'DELIVERED': 'bg-success', 'CANCELED': 'bg-danger' };
    return colorMap[status] || 'bg-light text-dark';
  }

  getOrigemPagamento(origin: string): string {
    const map: any = { 'CREDIT_CARD': 'Cartão de Crédito', 'DEBIT_CARD': 'Cartão de Débito', 'CASH': 'Dinheiro', 'PIX': 'PIX' };
    return map[origin] || origin;
  }
}