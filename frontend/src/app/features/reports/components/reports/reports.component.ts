import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reports',
  template: `<router-outlet></router-outlet>`
})
export class ReportsComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {}
}
