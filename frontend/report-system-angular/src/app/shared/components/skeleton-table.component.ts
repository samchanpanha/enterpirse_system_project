import { Component, Input } from '@angular/core';
import { SkeletonModule } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';

@Component({
  selector: 'app-skeleton-table',
  standalone: true,
  imports: [SkeletonModule, TableModule],
  template: `
    <p-table [value]="dummyRows" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          @for (col of colsArray; track col) {
            <th>
              <p-skeleton width="80%" height="1rem" />
            </th>
          }
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-row>
        <tr>
          @for (col of colsArray; track col) {
            <td>
              <p-skeleton width="60%" height="1rem" />
            </td>
          }
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class SkeletonTableComponent {
  @Input() rows = 5;
  @Input() cols = 5;

  get colsArray(): number[] {
    return Array.from({ length: this.cols }, (_, i) => i);
  }

  get dummyRows(): number[] {
    return Array.from({ length: this.rows }, (_, i) => i);
  }
}
