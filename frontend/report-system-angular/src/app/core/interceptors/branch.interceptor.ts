import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { BranchService } from '../services/branch.service';

export const branchInterceptor: HttpInterceptorFn = (req, next) => {
  const branch = inject(BranchService);

  let headers = req.headers;
  if (branch.tenantId) {
    headers = headers.set('X-Tenant-Id', branch.tenantId);
  }
  if (branch.branchId) {
    headers = headers.set('X-Branch-Id', branch.branchId);
  }

  if (branch.branchId) {
    const url = new URL(req.url, window.location.origin);
    url.searchParams.set('branchId', branch.branchId);
    req = req.clone({ url: url.toString(), headers });
  } else {
    req = req.clone({ headers });
  }

  return next(req);
};
