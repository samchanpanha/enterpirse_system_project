import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  user = signal<User | null>(null);
  isAuthenticated = signal(false);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.restoreSession();
  }

  login(credentials: LoginRequest) {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((res) => {
          this.setSession(res);
        }),
      );
  }

  ssoLogin(keycloakToken: string) {
    return this.http
      .post<LoginResponse>(
        `${environment.apiUrl}/auth/sso-login`,
        {},
        { headers: { Authorization: `Bearer ${keycloakToken}` } },
      )
      .pipe(
        tap((res) => {
          this.setSession(res);
        }),
      );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.user.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/auth/login']);
  }

  get token(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private setSession(res: LoginResponse) {
    localStorage.setItem(this.TOKEN_KEY, res.token);
    localStorage.setItem(this.REFRESH_KEY, res.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(res.user));
    this.user.set(res.user);
    this.isAuthenticated.set(true);
  }

  private restoreSession() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userRaw = localStorage.getItem(this.USER_KEY);
    if (token && userRaw) {
      try {
        this.user.set(JSON.parse(userRaw));
        this.isAuthenticated.set(true);
      } catch {
        this.logout();
      }
    }
  }
}
