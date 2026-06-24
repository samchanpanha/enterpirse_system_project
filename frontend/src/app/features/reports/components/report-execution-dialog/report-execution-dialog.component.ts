import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReportService } from '../../services/report.service';
import { Report, ReportParameter } from '../../models/report.model';

interface ExecutionDialogData {
  report: Report;
}

@Component({
  selector: 'app-report-execution-dialog',
  templateUrl: './report-execution-dialog.component.html',
  styleUrls: ['./report-execution-dialog.component.scss']
})
export class ReportExecutionDialogComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  executing = false;
  
  report!: Report;
  parameters: ReportParameter[] = [];

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ReportExecutionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ExecutionDialogData
  ) {}

  ngOnInit(): void {
    this.report = this.data.report;
    this.parameters = this.report.parameters || [];
    this.initForm();
  }

  initForm(): void {
    const formControls: any = {};
    
    // Add controls for each parameter
    this.parameters.forEach(param => {
      const validators = param.required ? [Validators.required] : [];
      formControls[param.name] = [param.defaultValue || '', validators];
    });

    this.form = this.fb.group(formControls);
  }

  onExecute(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.executing = true;
    const parameters = this.form.value;

    this.reportService.executeReport(this.report.id, { parameters }).subscribe({
      next: (execution) => {
        this.snackBar.open('Report execution started', 'Close', { duration: 3000 });
        this.dialogRef.close({ executionId: execution.id });
      },
      error: (error) => {
        this.snackBar.open('Failed to execute report', 'Close', { duration: 3000 });
        this.executing = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  get hasParameters(): boolean {
    return this.parameters.length > 0;
  }
}
