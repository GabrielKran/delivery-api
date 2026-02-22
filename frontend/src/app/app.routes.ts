import { Routes } from '@angular/router';
import { OrderListComponent } from './components/order-list/order-list';
import { OrderCreateComponent } from './components/order-create/order-create';

export const routes: Routes = [
    { path: '', component: OrderListComponent },
    { path: 'novo-pedido', component: OrderCreateComponent }
];
