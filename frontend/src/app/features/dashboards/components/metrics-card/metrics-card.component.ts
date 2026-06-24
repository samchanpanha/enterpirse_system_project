import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-metrics-card',
  templateUrl: './metrics-card.component.html',
  styleUrls: ['./metrics-card.component.scss']
})
export class MetricsCardComponent {
  @Input() title = '';
  @Input() value: number | string = 0;
  @Input() icon = 'info';
  @Input() color = '#2196F3';
  @Input() suffix = '';
}
