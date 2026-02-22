import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-order-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-create.html',
  styleUrl: './order-create.css'
})
export class OrderCreateComponent {
  
  novoPedido: any = {
    store_id: 'f052054c-e0a0-4768-ab55-7cb7ead57371',
    store_name: 'Coco Bambu Loja Teste',
    total_price: 0,
    customer: { name: '', temporary_phone: '' },
    delivery_address: {
      street_name: '', street_number: '', neighborhood: '', city: 'Brasília',
      state: 'Distrito Federal', postal_code: '70000-000', reference: '',
      country: 'BR', latitude: -15.7975, longitude: -47.8919, coord_id: Math.floor(Math.random() * 1000000)
    },
    items: [
      // Aqui está a mágica: price iniciado como null para o input vir vazio!
      { name: '', price: null, quantity: 1, total_price: 0, observations: '', code: Math.floor(Math.random() * 1000), condiments: [] }
    ],
    payments: [
      { origin: 'CREDIT_CARD', value: 0, prepaid: true }
    ]
  };

  isSubmitting = false;

  // Variáveis para o Toast Customizado
  showToast = false;
  toastMessage = '';
  toastType: 'success' | 'danger' = 'success';

  constructor(private orderService: OrderService, private router: Router) {}

  adicionarItem() {
    this.novoPedido.items.push({ 
      name: '', price: null, quantity: 1, total_price: 0, observations: '', code: Math.floor(Math.random() * 1000), condiments: [] 
    });
    this.calcularTotal();
  }

  removerItem(index: number) {
    this.novoPedido.items.splice(index, 1);
    this.calcularTotal();
  }

  calcularTotal() {
    let total = 0;
    this.novoPedido.items.forEach((item: any) => {
      // Se o utilizador apagar tudo e ficar nulo, tratamos como 0 para não dar erro
      const precoAtual = item.price || 0; 
      item.total_price = precoAtual * item.quantity;
      total += item.total_price;
    });
    this.novoPedido.total_price = total;
    this.novoPedido.payments[0].value = total;
  }

  mostrarToast(mensagem: string, tipo: 'success' | 'danger') {
    this.toastMessage = mensagem;
    this.toastType = tipo;
    this.showToast = true;
    setTimeout(() => this.showToast = false, 3000); // Some sozinho após 3s
  }

  salvarPedido() {
    this.calcularTotal(); 
    
    if (this.novoPedido.total_price <= 0 || !this.novoPedido.customer.name || !this.novoPedido.delivery_address.street_name) {
      this.mostrarToast('Preencha os campos obrigatórios e adicione um preço!', 'danger');
      return;
    }

    this.isSubmitting = true;

    this.orderService.create(this.novoPedido).subscribe({
      next: () => { // Removido o "res" inútil!
        this.mostrarToast('Pedido criado com sucesso!', 'success');
        // Atrasamos a navegação 1.5s para dar tempo de o utilizador ver a mensagem verde
        setTimeout(() => {
          this.router.navigate(['/']); 
        }, 1500);
      },
      error: (err) => {
        console.error('Erro detalhado:', err);
        this.mostrarToast('Erro ao criar pedido. Verifique a ligação.', 'danger');
        this.isSubmitting = false;
      }
    });
  }

  voltar() {
    this.router.navigate(['/']);
  }
}