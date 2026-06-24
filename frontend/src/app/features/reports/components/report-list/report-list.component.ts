import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { Report } from '../../models/report.model';
import { ReportExecutionDialogComponent } from '../report-execution-dialog/report-execution-dialog.component';

export interface ReportDisplay {
  id: string;
  name: string;
  description: string;
  type: string;
  status: string;
  createdAt: string;
  createdBy: string;
}

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.scss']
})
export class ReportListComponent implements OnInit {
  displayedColumns: string[] = ['name', 'description', 'type', 'status', 'createdAt', 'createdBy', 'actions'];
  dataSource: MatTableDataSource<ReportDisplay> = new MatTableDataSource();
  loading = false;
  
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.reportService.getReports().subscribe({
      next: (reports) => {
        this.dataSource.data = reports.map(report => ({
          id: report.id,
          name: report.name,
          description: report.description || 'No description',
          type: report.type,
          status: report.active ? 'Active' : 'Inactive',
          createdAt: new Date(report.createdAt).toLocaleDateString(),
          createdBy: report.createdBy || 'Unknown'
        }));
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.loading = false;
      },
      error: (error) => {
        this.snackBar.open('Failed to load reports', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  viewReport(id: string): void {
    this.router.navigate(['/reports', id]);
  }

  editReport(id: string): void {
    this.router.navigate(['/reports', id, 'edit']);
  }

  deleteReport(id: string): void {
    const confirmDelete = confirm('Are you sure you want to delete this report?');
    if (confirmDelete) {
      this.reportService.deleteReport(id).subscribe({
        next: () => {
          this.snackBar.open('Report deleted successfully', 'Close', { duration: 3000 });
          this.loadReports();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete report', 'Close', { duration: 3000 });
        }
      });
    }
  }

  executeReport(report: Report): void {
    const dialogRef = this.dialog.open(ReportExecutionDialogComponent, {
      width: '600px',
      data: { report }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.router.navigate(['/reports', result.executionId, 'results']);
      }
    });
  }

  createNewReport(): void {
    this.router.navigate(['/reports/new']);
  }
}
