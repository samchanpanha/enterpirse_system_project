import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

// Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';

// Shared Module
import { SharedModule } from '../../shared/shared.module';

// Components
import { ReportsComponent } from './components/reports/reports.component';
import { ReportListComponent } from './components/report-list/report-list.component';
import { ReportDetailComponent } from './components/report-detail/report-detail.component';
import { ReportFormComponent } from './components/report-form/report-form.component';
import { ReportExecutionComponent } from './components/report-execution/report-execution.component';
import { ReportExecutionDialogComponent } from './components/report-execution-dialog/report-execution-dialog.component';

const ROUTES = [
  {
    path: '',
    component: ReportsComponent,
    children: [
      { path: '', redirectTo: 'list', pathMatch: 'full' },
      { path: 'list', component: ReportListComponent },
      { path: 'new', component: ReportFormComponent },
      { path: ':id', component: ReportDetailComponent },
      { path: ':id/edit', component: ReportFormComponent },
      { path: ':id/execute', component: ReportExecutionComponent }
    ]
  }
];

@NgModule({
  declarations: [
    ReportsComponent,
    ReportListComponent,
    ReportDetailComponent,
    ReportFormComponent,
    ReportExecutionComponent,
    ReportExecutionDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forChild(ROUTES),
    
    // Angular Material Modules
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatChipsModule,
    MatTooltipModule,
    MatTabsModule,
    
    SharedModule
  ],
  exports: [
    ReportsComponent,
    ReportListComponent,
    ReportDetailComponent,
    ReportFormComponent,
    ReportExecutionComponent
  ]
})
export class ReportsModule { }
