import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { Subject, Subscription, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule, InputTextModule, IconFieldModule, InputIconModule],
  template: `
    <p-iconfield>
      <p-inputicon>
        <i class="pi pi-search"></i>
      </p-inputicon>
      <input
        pInputText
        type="text"
        [placeholder]="placeholder"
        [(ngModel)]="query"
        (ngModelChange)="onInput()"
        class="w-20rem"
      />
    </p-iconfield>
  `,
})
export class SearchBarComponent implements OnInit, OnDestroy {
  @Input() placeholder = 'Search...';
  @Input() debounceMs = 300;
  @Output() search = new EventEmitter<string>();

  query = '';
  private searchSubject = new Subject<string>();
  private sub?: Subscription;

  ngOnInit() {
    this.sub = this.searchSubject.pipe(
      debounceTime(this.debounceMs),
      distinctUntilChanged(),
    ).subscribe((value) => {
      this.search.emit(value);
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  onInput() {
    this.searchSubject.next(this.query);
  }
}
