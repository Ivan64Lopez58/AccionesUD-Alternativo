import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { StockDTO } from '../twelve-mercados/stock.model';
import { StockService } from '../twelve-mercados/stock.service';

export enum OrderState {
  PROCESANDO = 'Procesando',
  ACEPTADA = 'Aceptada',
  PENDIENTE = 'Pendiente',
  EN_COLA = 'En cola',
  RECHAZADA = 'Rechazada',
  CANCELADA = 'Cancelada',
  EJECUTADA = 'Ejecutada'
}

interface Accion {
  nombre_empresa: string;
  precio: string;
  cambio: string;
  cambio_pct: string;
  hora_cierre: string;
  pais: string;
  moneda: string;
  simbolo: string;
  logo_empresa: string;
}


export interface Order {
  id: number;
  name: string;
  country: string;
  logo: string;
  grafica: string;
  compraPrecio: number;
  ventaPrecio: number;
  cantidad: number;
  moneda: string;

  spread: number;
  spreadpips: number;
  comision: number;
  comisionporsentaje: number;
  valorPip: number;
  swapDiarioCompra: string;
  swapDiarioVenta: string;
  tipoOrden: string;
  stopLoss: number;
  takeProfit: number;
  totalEstimado: number;
  saldoDisponible: number;

  // Nuevos campos
  fechaCreacion: string; // Fecha de creación de la orden
  estado: OrderState; // Estado de la orden (ej. 'pendiente', 'completada', 'cancelada')
  operacion: string; // Operación asociada a la orden (ej. 'compra', 'venta')
}
@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = ''; // URL de tu API
  private symbols = [
    'AAPL'/*, 'GOOGL', 'MSFT', 'AMZN', 'TSLA',
    'META', 'NVDA', 'NFLX', 'JPM', 'BRK.B',
    'DIS', 'NKE', 'INTC', 'AMD', 'V',
    'MA', 'BAC', 'KO', 'PEP'*/
  ];

  constructor(private http: HttpClient, private stockService: StockService) {}

getOrders(): Observable<Order[]> {
  const cacheRaw = localStorage.getItem('acciones_para_ordenes');
  if (cacheRaw) {
    try {
      const acciones: Accion[] = JSON.parse(cacheRaw);
      const ordenes = acciones.map((accion, index) => {
        const precio = parseFloat(accion.precio);
        const compra = Number((precio - 0.4 / 2).toFixed(3));
        const venta = Number((precio + 0.4 / 2).toFixed(3));
        const cantidad = 5;

        const spread = Number(Math.abs(compra - venta).toFixed(3));
        const spreadpips = Number((spread / 100).toFixed(3));
        const totalEstimado = Number((cantidad * compra).toFixed(3));
        const comisionporsentaje = 0.001;
        const comision = Number((totalEstimado * comisionporsentaje).toFixed(3));
        const valorPip = Number((spread * cantidad).toFixed(3));
        const stopLoss = Number((compra * 0.98).toFixed(3));
        const takeProfit = Number((compra * 1.02).toFixed(3));

        return {
          id: index,
          name: accion.nombre_empresa,
          country: accion.pais,
          logo: accion.logo_empresa,
          grafica: '',
          compraPrecio: compra,
          ventaPrecio: venta,
          cantidad: cantidad,
          moneda: accion.moneda,
          spread: spread,
          spreadpips: spreadpips,
          comision: comision,
          comisionporsentaje: Number((comisionporsentaje * 100).toFixed(3)),
          valorPip: valorPip,
          swapDiarioCompra: '-0.25',
          swapDiarioVenta: '-0.30',
          tipoOrden: 'market',
          stopLoss: stopLoss,
          takeProfit: takeProfit,
          totalEstimado: totalEstimado,
          saldoDisponible: 1000,
          fechaCreacion: new Date().toISOString(),
          estado: OrderState.PENDIENTE,
          operacion: ''
        };
      });

      return of(ordenes);
    } catch (e) {
      console.error('⚠️ Error al leer acciones del localStorage', e);
    }
  }

  return of([]);
}



}
