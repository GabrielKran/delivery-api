import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { OrderSummary } from '../../models/order';
import { OrderDetailsComponent } from '../order-details/order-details';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, OrderDetailsComponent],
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
      next: (data: OrderSummary[]) => {
        this.orders = data || [];
        this.aplicarFiltrosEOrdenacao();
        this.isLoading = false; 
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
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
      temp = this.orders.filter((order) => this.activeFilters.includes(order.last_status_name));
    }

    temp.sort((a, b) => {
      let valA = this.sortColumn === 'date' ? a.created_at : a.total_price;
      let valB = this.sortColumn === 'date' ? b.created_at : b.total_price;

      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredOrders = temp;
  }

  abrirDetalhes(order: OrderSummary): void {
    this.selectedOrderId = order.id;
    this.isDetailsOpen = true;
  }

  fecharDetalhes(): void {
    this.isDetailsOpen = false;
    this.selectedOrderId = null;
  }

  // --- MÁQUINA DE ESTADOS (AGORA FUNCIONANDO) ---

  obterProximoStatusParaBackend(statusAtual: string): string | null {
    const transicoes: any = {
      'RECEIVED': 'CONFIRMED',
      'CONFIRMED': 'DISPATCHED',
      'DISPATCHED': 'DELIVERED'
    };
    return transicoes[statusAtual] || null;
  }

  avancarStatus(order: OrderSummary, event: Event): void {
    event.stopPropagation(); 
    
    const nextStatus = this.obterProximoStatusParaBackend(order.last_status_name);
    if (!nextStatus) return;

    const btn = event.target as HTMLButtonElement;
    const textoOriginal = btn.innerText;
    btn.innerText = 'Processando...';
    btn.disabled = true;

    this.orderService.updateStatus(order.id, nextStatus).subscribe({
      next: (updatedOrder) => {
        // Atualiza o status localmente para refletir na tela imediatamente
        order.last_status_name = updatedOrder.order.last_status_name;

        btn.innerText = this.getAcaoProximoStatus(order.last_status_name) || 'Finalizado';
        btn.disabled = false;

        this.aplicarFiltrosEOrdenacao(); 
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao atualizar status', err);
        alert('Falha ao avançar o status do pedido.');
        btn.innerText = textoOriginal;
        btn.disabled = false;
      }
    });
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

  // Chamado quando o modal de detalhes cancela um pedido
  atualizarStatusNaLista(novoStatus: string): void {
    const pedido = this.orders.find(o => o.id === this.selectedOrderId);
    if (pedido) {
      pedido.last_status_name = novoStatus;
      this.aplicarFiltrosEOrdenacao(); // Reorganiza a tabela na hora!
    }
  }

  // Chamado quando o modal de detalhes exclui um pedido
  removerPedidoDaLista(idParaRemover: string): void {
    this.orders = this.orders.filter(o => o.id !== idParaRemover);
    this.aplicarFiltrosEOrdenacao(); // Some da tabela na hora!
  }
}