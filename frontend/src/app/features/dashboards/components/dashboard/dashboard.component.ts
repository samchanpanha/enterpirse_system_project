import { Component, OnInit } from '@angular/core';
import { DashboardService, MetricCard, ExecutionHistory } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  loading = false;
  
  // Metrics
  totalReports = 0;
  activeReports = 0;
  executionsToday = 0;
  successRate = 0;
  
  // Chart data
  executionChartData: any = {
    labels: [],
    datasets: []
  };
  
  chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top' as const
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          stepSize: 1
        }
      }
    }
  };
  
  // Recent executions
  recentExecutions: ExecutionHistory[] = [];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    
    // Load metrics
    this.dashboardService.getMetrics().subscribe({
      next: (metrics) => {
        this.totalReports = metrics.totalReports;
        this.activeReports = metrics.activeReports;
        this.executionsToday = metrics.executionsToday;
        this.successRate = metrics.successRate;
      },
      error: () => {
        this.loading = false;
      }
    });
    
    // Load chart data
    this.dashboardService.getExecutionTrend(7).subscribe({
      next: (chartData) => {
        this.executionChartData = {
          labels: chartData.labels,
          datasets: [
            {
              label: 'Successful Executions',
              data: chartData.successful,
              backgroundColor: 'rgba(76, 175, 80, 0.6)',
              borderColor: 'rgba(76, 175, 80, 1)',
              borderWidth: 1
            },
            {
              label: 'Failed Executions',
              data: chartData.failed,
              backgroundColor: 'rgba(244, 67, 54, 0.6)',
              borderColor: 'rgba(244, 67, 54, 1)',
              borderWidth: 1
            }
          ]
        };
      },
      error: () => {
        this.loading = false;
      }
    });
    
    // Load recent executions
    this.dashboardService.getRecentExecutions(10).subscribe({
      next: (executions) => {
        this.recentExecutions = executions;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
