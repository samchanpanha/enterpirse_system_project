import { Injectable, isDevMode } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiErrorHandler {
  handle(error: HttpErrorResponse): string {
    if (isDevMode()) {
      console.error('[ApiError]', error);
    }

    if (!(error instanceof HttpErrorResponse)) {
      return 'An unexpected error occurred.';
    }

    switch (error.status) {
      case 0:
        return 'Unable to connect to the server. Please check your network connection.';
      case 400:
        return error.error?.message || 'Invalid request. Please check your input.';
      case 401:
        return 'Your session has expired. Please sign in again.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 409:
        return error.error?.message || 'A conflict occurred. The resource may have been modified.';
      case 422:
        return error.error?.message || 'Validation failed. Please check your input.';
      case 429:
        return 'Too many requests. Please wait a moment and try again.';
      case 500:
        return 'An internal server error occurred. Please try again later.';
      case 502:
      case 503:
        return 'The service is temporarily unavailable. Please try again.';
      default:
        return error.error?.message || error.message || 'An unexpected error occurred.';
    }
  }
}
