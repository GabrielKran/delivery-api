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
  
  // NOVOS EVENTOS PARA AVISAR A LISTA:
  @Output() statusChanged = new EventEmitter<string>();
  @Output() orderDeleted = new EventEmitter<string>();

  orderFull: OrderFullResponse | null = null;
  isLoading: boolean = false;
  isClosing: boolean = false;

  // CONTROLES DOS NOSSOS MODAIS CUSTOMIZADOS
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

  // --- NOVA LÓGICA DE CANCELAMENTO ---
  confirmarCancelamento(): void {
    if (!this.orderId || !this.orderFull) return;
    
    this.isProcessing = true;
    this.orderService.updateStatus(this.orderId, 'CANCELED').subscribe({
      next: (updated) => {
        this.orderFull = updated;
        this.showConfirmCancel = false; // Fecha o modalzinho
        this.isProcessing = false;
        this.statusChanged.emit('CANCELED'); // Avisa a tabela no fundo para atualizar!
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isProcessing = false;
        alert('Erro ao tentar cancelar o pedido.'); // Mantemos alert só para erros críticos de rede
      }
    });
  }

  // --- NOVA LÓGICA DE EXCLUSÃO ---
  confirmarExclusao(): void {
    if (!this.orderId) return;

    this.isProcessing = true;
    this.orderService.delete(this.orderId).subscribe({
      next: () => {
        this.isProcessing = false;
        this.orderDeleted.emit(this.orderId!); // Avisa a lista para remover a linha
        this.fechar(); // Fecha os detalhes
      },
      error: (err) => {
        console.error(err);
        this.isProcessing = false;
        alert('Erro ao excluir pedido.');
      }
    });
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
    const map: any = { 'CREDIT_CARD': 'Cartão de Crédito', 'DEBIT_CARD': 'Cartão de Débito', 'CASH': 'Dinheiro', 'PIX': 'PIX', 'VR': 'Vale Refeição' };
    return map[origin] || origin;
  }

  // NOVO: Ícones dinâmicos de pagamento!
  getIconePagamento(origin: string): string {
    const icons: any = { 'CREDIT_CARD': '💳', 'DEBIT_CARD': '💳', 'CASH': '💵', 'PIX': '❖', 'VR': '🎟️' };
    return icons[origin] || '💲';
  }
}