import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { OrderSummary } from '../../models/order';
import { OrderDetailsComponent } from '../order-details/order-details';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, OrderDetailsComponent], // Injetando o Filho aqui!
  templateUrl: './order-list.html',
  styleUrl: './order-list.css',
})
export class OrderListComponent implements OnInit {
  orders: OrderSummary[] = [];
  filteredOrders: OrderSummary[] = [];
  activeFilters: string[] = [];
  isLoading: boolean = false;

  sortColumn: 'date' | 'total' = 'date';
  sortDirection: 'asc' | 'desc' = 'asc';

  // --- VARIÁVEIS DO PAINEL DE DETALHES ---
  selectedOrderId: string | null = null;
  isDetailsOpen: boolean = false;

  constructor(
    private orderService: OrderService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarPedidos();
  }

  carregarPedidos(): void {
    this.isLoading = true;
    this.orderService.findAll().subscribe({
      next: (data) => {
        this.orders = data || [];
        this.aplicarFiltrosEOrdenacao();
        this.isLoading = false; 
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error('Erro na API:', err);
        this.isLoading = false; 
        this.cdr.detectChanges(); 
      },
    });
  }

  copiarId(id: string, event: Event): void {
    event.stopPropagation(); 
    navigator.clipboard.writeText(id).then(() => {
      console.log('ID Copiado para a área de transferência:', id);
    });
  }

  mudarOrdenacao(coluna: 'date' | 'total'): void {
    if (this.sortColumn === coluna) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = coluna;
      this.sortDirection = 'asc';
    }
    this.aplicarFiltrosEOrdenacao();
  }

  toggleFilter(status: string): void {
    if (status === 'ALL') {
      this.activeFilters = [];
    } else {
      const index = this.activeFilters.indexOf(status);
      if (index > -1) {
        this.activeFilters.splice(index, 1);
      } else {
        this.activeFilters.push(status);
      }

      if (this.activeFilters.length === 5 || this.activeFilters.length === 0) {
        this.activeFilters = [];
      }
    }
    this.aplicarFiltrosEOrdenacao();
  }

  isFilterActive(status: string): boolean {
    if (status === 'ALL') return this.activeFilters.length === 0;
    return this.activeFilters.includes(status);
  }

  aplicarFiltrosEOrdenacao(): void {
    let temp = [];

    if (this.activeFilters.length === 0) {
      temp = [...this.orders];
    } else {
      temp = this.orders.filter((order) => this.activeFilters.includes(order.lastStatusName));
    }

    temp.sort((a, b) => {
      let valA = this.sortColumn === 'date' ? a.createdAt : a.totalPrice;
      let valB = this.sortColumn === 'date' ? b.createdAt : b.totalPrice;

      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredOrders = temp;
  }

  // --- AÇÕES DO PAINEL DE DETALHES ---
  
  // Abre o painel passando o ID
  abrirDetalhes(order: OrderSummary): void {
    this.selectedOrderId = order.id;
    this.isDetailsOpen = true;
  }

  // Fecha o painel (A função que o Angular estava sentindo falta!)
  fecharDetalhes(): void {
    this.isDetailsOpen = false;
    this.selectedOrderId = null;
  }

  // -----------------------------------

  avancarStatus(order: OrderSummary, event: Event): void {
    event.stopPropagation(); 
    console.log('CHAMAR BACKEND PARA AVANÇAR O STATUS DE:', order.id);
  }

  getAcaoProximoStatus(statusAtual: string): string | null {
    const acoes: any = {
      RECEIVED: 'Confirmar',
      CONFIRMED: 'Despachar',
      DISPATCHED: 'Entregue',
    };
    return acoes[statusAtual] || null;
  }

  getStatusNome(status: string): string {
    const statusMap: any = {
      RECEIVED: 'Recebido',
      CONFIRMED: 'Confirmado',
      DISPATCHED: 'Despachado',
      DELIVERED: 'Entregue',
      CANCELED: 'Cancelado',
    };
    return statusMap[status] || status;
  }

  getStatusCor(status: string): string {
    const colorMap: any = {
      RECEIVED: 'bg-secondary',
      CONFIRMED: 'bg-info text-dark',
      DISPATCHED: 'bg-warning text-dark',
      DELIVERED: 'bg-success',
      CANCELED: 'bg-danger',
    };
    return colorMap[status] || 'bg-light text-dark';
  }
}