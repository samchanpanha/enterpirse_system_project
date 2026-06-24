import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, ActivatedRoute } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { Report, DataSource, ReportParameter, CreateReportRequest } from '../../models/report.model';

@Component({
  selector: 'app-report-form',
  templateUrl: './report-form.component.html',
  styleUrls: ['./report-form.component.scss']
})
export class ReportFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  submitting = false;
  editMode = false;
  reportId?: string;
  
  dataSources: DataSource[] = [];
  reportTypes = [
    { value: 'SQL', label: 'SQL Query' },
    { value: 'JASPER', label: 'Jasper Report' },
    { value: 'CUSTOM', label: 'Custom Report' }
  ];

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadDataSources();
    
    this.reportId = this.route.snapshot.paramMap.get('id') || undefined;
    this.editMode = !!this.reportId;
    
    if (this.editMode) {
      this.loadReport();
    }
  }

  initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      type: ['SQL', [Validators.required]],
      dataSourceId: ['', [Validators.required]],
      query: ['', []],
      active: [true, []],
      parameters: this.fb.array([])
    });
  }

  loadDataSources(): void {
    this.reportService.getDataSources().subscribe({
      next: (sources) => {
        this.dataSources = sources;
      },
      error: () => {
        this.snackBar.open('Failed to load data sources', 'Close', { duration: 3000 });
      }
    });
  }

  loadReport(): void {
    if (!this.reportId) return;
    
    this.loading = true;
    this.reportService.getReport(this.reportId).subscribe({
      next: (report) => {
        this.form.patchValue({
          name: report.name,
          description: report.description || '',
          type: report.type,
          dataSourceId: report.dataSourceId,
          query: report.query || '',
          active: report.active
        });
        
        if (report.parameters && report.parameters.length > 0) {
          // TODO: Load parameters into form array
        }
        
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load report', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const formValue = this.form.value;
    
    const request: CreateReportRequest = {
      name: formValue.name,
      description: formValue.description || undefined,
      type: formValue.type,
      dataSourceId: formValue.dataSourceId,
      query: formValue.query || undefined,
      active: formValue.active,
      parameters: formValue.parameters
    };

    const save$ = this.editMode 
      ? this.reportService.updateReport(this.reportId!, request)
      : this.reportService.createReport(request);

    save$.subscribe({
      next: (savedReport) => {
        this.snackBar.open(
          this.editMode ? 'Report updated successfully' : 'Report created successfully',
          'Close',
          { duration: 3000 }
        );
        this.router.navigate(['/reports', savedReport.id]);
      },
      error: (error) => {
        this.snackBar.open(
          this.editMode ? 'Failed to update report' : 'Failed to create report',
          'Close',
          { duration: 3000 }
        );
        this.submitting = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/reports']);
  }

  get name() { return this.form.get('name'); }
  get description() { return this.form.get('description'); }
  get type() { return this.form.get('type'); }
  get dataSourceId() { return this.form.get('dataSourceId'); }
  get query() { return this.form.get('query'); }
}
