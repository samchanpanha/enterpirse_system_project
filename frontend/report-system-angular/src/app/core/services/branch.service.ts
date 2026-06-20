import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BranchService {
  private branchId$ = new BehaviorSubject<string | null>(null);
  private tenantId$ = new BehaviorSubject<string>('00000000-0000-0000-0000-000000000001');

  get branchId() {
    return this.branchId$.value;
  }

  get tenantId() {
    return this.tenantId$.value;
  }

  get branchIdChanges() {
    return this.branchId$.asObservable();
  }

  setBranch(id: string | null) {
    this.branchId$.next(id);
  }

  setTenant(id: string) {
    this.tenantId$.next(id);
  }
}
