import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

// Charts
import { NgChartsModule } from 'ng2-charts';

// Shared Module
import { SharedModule } from '../../shared/shared.module';

// Components
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { MetricsCardComponent } from './components/metrics-card/metrics-card.component';
import { ReportsChartComponent } from './components/reports-chart/reports-chart.component';
import { RecentExecutionsComponent } from './components/recent-executions/recent-executions.component';

const ROUTES = [
  {
    path: '',
    component: DashboardComponent
  }
];

@NgModule({
  declarations: [
    DashboardComponent,
    MetricsCardComponent,
    ReportsChartComponent,
    RecentExecutionsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(ROUTES),
    
    // Angular Material Modules
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    
    // Charts
    NgChartsModule,
    
    SharedModule
  ],
  exports: [
    DashboardComponent,
    MetricsCardComponent,
    ReportsChartComponent,
    RecentExecutionsComponent
  ]
})
export class DashboardsModule { }
