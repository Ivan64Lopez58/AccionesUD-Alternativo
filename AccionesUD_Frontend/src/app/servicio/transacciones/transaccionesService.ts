import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface BalanceInfo {
  availableBalance: number;
  pendingBalance: number;
  totalBalance: number;
  currency: string;
}

export interface Transaction {
  id: string;
  bank: string;
  date: string;
  fullDate: string;
  amount: string;
  rawAmount: number;
  status: 'success' | 'pending' | 'failed';
  type: string;
  reference: string;
  details?: string;
}

export interface ChartDataPoint {
  value: number;
  label: string;
  rawValue: number;
  date: string;
}

export interface ChartData {
  data: ChartDataPoint[];
  maxValue: number;
  minValue: number;
  avgValue: number;
}

@Injectable({ providedIn: 'root' })
export class TransaccionesService {
  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los headers con el JWT almacenado.
   */
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwt');
    return token
      ? new HttpHeaders({ Authorization: `Bearer ${token}` })
      : new HttpHeaders();
  }

  /**
   * Obtiene la información del saldo del usuario
   */
  getUserBalance(): Observable<BalanceInfo> {
    const headers = this.getAuthHeaders();
    return this.http.get<BalanceInfo>(`${this.baseUrl}/balance`, {
      headers,
    });
  }

  /**
   * Obtiene las transacciones del usuario con filtros opcionales
   * @param period Período de tiempo ('month', 'week', 'day')
   * @param bankName Filtro opcional por nombre de banco
   */
  getUserTransactions(period: string, bankName?: string): Observable<Transaction[]> {
    const headers = this.getAuthHeaders();
    let params = new HttpParams().set('period', period);

    if (bankName) {
      params = params.set('bank', bankName);
    }

    return this.http.get<Transaction[]>(`${this.baseUrl}/history`, {
      headers,
      params
    });
  }

  /**
   * Obtiene datos para generar el gráfico de transacciones
   * @param period Período de tiempo ('month', 'week', 'day')
   */
  getChartData(period: string): Observable<ChartData> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('period', period);

    return this.http.get<ChartData>(`${this.baseUrl}/chart-data`, {
      headers,
      params
    });
  }

  /**
   * Inicia el proceso de ingreso de saldo
   * @param amount Cantidad a ingresar
   */
  depositFunds(amount: number): Observable<{ redirectUrl: string }> {
    const headers = this.getAuthHeaders();
    return this.http.post<{ redirectUrl: string }>(`${this.baseUrl}/deposit`, { amount }, { headers });
  }
}
