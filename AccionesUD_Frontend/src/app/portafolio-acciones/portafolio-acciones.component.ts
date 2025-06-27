import { Component } from '@angular/core';
import { StockItem } from './portafolio-acciones.model';
import { CommonModule } from '@angular/common';
import { PiePaginaPrincipalComponent } from '../pie-pagina-principal/pie-pagina-principal.component';
import { Menu2Component } from "../menu2/menu2.component";

@Component({
  selector: 'app-portafolio-acciones',
  standalone: true,
  imports: [
    CommonModule,
    PiePaginaPrincipalComponent,
    Menu2Component,
  ],
  templateUrl: './portafolio-acciones.component.html',
  styleUrl: './portafolio-acciones.component.css',
})
export class PortafolioAccionesComponent {

  portfolio: StockItem[] = [
    {
      name: 'TESLA',
      quantity: 10,
      currentPrice: 800,
      initialPrice: 600,
      sector: 'Tecnología',
    },
    {
      name: 'ECOPETROL',
      quantity: 50,
      currentPrice: 3000,
      initialPrice: 2800,
      sector: 'Energía',
    },
    // otros elementos...
  ];
  sortBy: string = '';
  filterSector: string = '';


  onSortChange(event: Event) {
    this.sortBy = (event.target as HTMLSelectElement).value;
  }

  onSectorChange(event: Event) {
    this.filterSector = (event.target as HTMLSelectElement).value;
  }

  filteredPortfolio() {
    let data = [...this.portfolio];

    if (this.filterSector) {
      data = data.filter((stock) => stock.sector === this.filterSector);
    }

    if (this.sortBy === 'valor') {
      data.sort(
        (a, b) => b.currentPrice * b.quantity - a.currentPrice * a.quantity
      );
    } else if (this.sortBy === 'nombre') {
      data.sort((a, b) => a.name.localeCompare(b.name));
    } else if (this.sortBy === 'rendimiento') {
      data.sort((a, b) => this.rendimiento(b) - this.rendimiento(a));
    }

    return data;
  }

  rendimiento(stock: StockItem): number {
    return (
      ((stock.currentPrice - stock.initialPrice) / stock.initialPrice) * 100
    );
  }

  totalValue(stock: StockItem): number {
    return stock.quantity * stock.currentPrice;
  }

  sectores(): string[] {
    return [...new Set(this.portfolio.map((stock) => stock.sector))];
  }
}
