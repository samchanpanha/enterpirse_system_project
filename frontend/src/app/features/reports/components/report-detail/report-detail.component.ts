import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-report-detail',
  template: `
    <div class="report-detail-container">
      <h1>Report Detail (Coming Soon)</h1>
      <p>Report detail view with execution history and analytics will be implemented here.</p>
    </div>
  `,
  styles: [`
    .report-detail-container {
      padding: 24px;
      text-align: center;
      
      h1 {
        color: #666;
      }
      
      p {
        color: #999;
      }
    }
  `]
})
export class ReportDetailComponent implements OnInit {
  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log('Viewing report:', id);
  }
}
