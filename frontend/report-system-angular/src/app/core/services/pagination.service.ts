import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PaginationService {
  readonly page = signal(0);
  readonly size = signal(20);
  readonly total = signal(0);
  readonly totalPages = signal(0);

  goToPage(n: number) {
    if (n >= 0 && n < this.totalPages()) {
      this.page.set(n);
    }
  }

  nextPage() {
    this.goToPage(this.page() + 1);
  }

  prevPage() {
    this.goToPage(this.page() - 1);
  }

  setTotal(total: number) {
    this.total.set(total);
    const size = this.size();
    this.totalPages.set(total > 0 ? Math.ceil(total / size) : 0);

    if (this.page() >= this.totalPages()) {
      this.page.set(Math.max(0, this.totalPages() - 1));
    }
  }
}
