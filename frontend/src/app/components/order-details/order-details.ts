import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { OrderFullResponse } from '../../models/order';

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
  
  @Output() statusChanged = new EventEmitter<string>();
  @Output() orderDeleted = new EventEmitter<string>();

  orderFull: OrderFullResponse | null = null;
  isLoading: boolean = false;
  isClosing: boolean = false;

  showConfirmCancel: boolean = false;
  showConfirmDelete: boolean = false;
  isProcessing: boolean = false;

  constructor(private orderService: OrderService, private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['orderId'] && this.orderId) {
      this.isClosing = false;
      this.showConfirmCancel = false;
      this.showConfirmDelete = false;
      this.buscarDetalhes(this.orderId);
    }
  }

  buscarDetalhes(id: string): void {
    this.isLoading = true;
    this.orderService.findById(id).subscribe({
      next: (data: OrderFullResponse) => {
        this.orderFull = data;
        this.isLoading = false;
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
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

  confirmarCancelamento(): void {
    if (!this.orderId || !this.orderFull) return;
    
    this.isProcessing = true;
    this.orderService.updateStatus(this.orderId, 'CANCELED').subscribe({
      next: (updated) => {
        this.orderFull = updated;
        this.showConfirmCancel = false;
        this.isProcessing = false;
        this.statusChanged.emit('CANCELED');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isProcessing = false;
        alert('Erro ao tentar cancelar o pedido.');
      }
    });
  }

  confirmarExclusao(): void {
    if (!this.orderId) return;

    this.isProcessing = true;
    this.orderService.delete(this.orderId).subscribe({
      next: () => {
        this.isProcessing = false;
        this.orderDeleted.emit(this.orderId!);
        this.fechar();
      },
      error: (err) => {
        console.error(err);
        this.isProcessing = false;
        alert('Erro ao excluir pedido.');
      }
    });
  }

  getStatusNome(status: string): string {
    const statusMap: any = { 'RECEIVED': 'Recebido', 'CONFIRMED': 'Confirmado', 'DISPATCHED': 'Despachado', 'DELIVERED': 'Entregue', 'CANCELED': 'Cancelado' };
    return statusMap[status] || status;
  }

  getStatusCor(status: string): string {
    const colorMap: any = { 'RECEIVED': 'bg-secondary', 'CONFIRMED': 'bg-info text-dark', 'DISPATCHED': 'bg-warning text-dark', 'DELIVERED': 'bg-success', 'CANCELED': 'bg-danger' };
    return colorMap[status] || 'bg-light text-dark';
  }

  getOrigemPagamento(origin: string): string {
    const map: any = { 'CREDIT_CARD': 'Cartão de Crédito', 'DEBIT_CARD': 'Cartão de Débito', 'CASH': 'Dinheiro', 'PIX': 'PIX', 'VR': 'Vale Refeição' };
    return map[origin] || origin;
  }

  getIconePagamento(origin: string): string {
    const icons: any = { 'CREDIT_CARD': '💳', 'DEBIT_CARD': '💳', 'CASH': '💵', 'PIX': '❖', 'VR': '🎟️' };
    return icons[origin] || '💲';
  }
}