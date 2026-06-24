import { Component, Input } from '@angular/core';
import { ExecutionHistory } from '../../services/dashboard.service';

@Component({
  selector: 'app-recent-executions',
  templateUrl: './recent-executions.component.html',
  styleUrls: ['./recent-executions.component.scss']
})
export class RecentExecutionsComponent {
  @Input() executions: ExecutionHistory[] = [];

  getStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return '#4CAF50';
      case 'FAILED':
        return '#F44336';
      case 'RUNNING':
        return '#FF9800';
      default:
        return '#9E9E9E';
    }
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
