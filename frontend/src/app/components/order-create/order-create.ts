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
  
  // Objeto padronizado em SNAKE_CASE para bater 100% com o Backend
  novoPedido: any = {
    store_id: 'f052054c-e0a0-4768-ab55-7cb7ead57371',
    store_name: 'Coco Bambu Loja Teste',
    total_price: 0,
    customer: {
      name: '',
      temporary_phone: ''
    },
    delivery_address: {
      street_name: '',
      street_number: '',
      neighborhood: '',
      city: 'Brasília',
      state: 'Distrito Federal',
      postal_code: '70000-000',
      reference: '',
      country: 'BR',
      latitude: -15.7975,
      longitude: -47.8919,
      coord_id: Math.floor(Math.random() * 1000000)
    },
    items: [
      { name: '', price: 0, quantity: 1, total_price: 0, observations: '', code: Math.floor(Math.random() * 1000), condiments: [] }
    ],
    payments: [
      { origin: 'CREDIT_CARD', value: 0, prepaid: true }
    ]
  };

  isSubmitting = false;

  constructor(private orderService: OrderService, private router: Router) {}

  adicionarItem() {
    this.novoPedido.items.push({ 
      name: '', price: 0, quantity: 1, total_price: 0, observations: '', code: Math.floor(Math.random() * 1000), condiments: [] 
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
      item.total_price = item.price * item.quantity;
      total += item.total_price;
    });
    this.novoPedido.total_price = total;
    this.novoPedido.payments[0].value = total;
  }

  salvarPedido() {
    this.calcularTotal(); 
    
    if (this.novoPedido.total_price <= 0 || !this.novoPedido.customer.name || !this.novoPedido.delivery_address.street_name) {
      alert('Preencha o nome do cliente, a rua e certifique-se de que o total é maior que zero!');
      return;
    }

    this.isSubmitting = true;

    this.orderService.create(this.novoPedido).subscribe({
      next: (res) => {
        // Redireciona de volta para a lista após criar com sucesso
        this.router.navigate(['/']); 
      },
      error: (err) => {
        console.error('Erro detalhado:', err);
        alert('Erro ao criar pedido. Verifique o console do navegador e do Spring Boot.');
        this.isSubmitting = false;
      }
    });
  }

  voltar() {
    this.router.navigate(['/']);
  }
}