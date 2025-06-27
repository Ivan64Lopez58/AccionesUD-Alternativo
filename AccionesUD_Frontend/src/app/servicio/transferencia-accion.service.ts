import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Order } from './acciones/order.service'; // Asegúrate de que la ruta sea correcta

@Injectable({ providedIn: 'root' })
export class TransferenciaAccionService {
  private accionesSubject = new BehaviorSubject<Order[]>([]);
  acciones$ = this.accionesSubject.asObservable();

  setAcciones(acciones: Order[]) {
    this.accionesSubject.next(acciones);
  }

  limpiar() {
    this.accionesSubject.next([]);
  }
}
