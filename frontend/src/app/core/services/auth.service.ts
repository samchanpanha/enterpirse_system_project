import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { environment } from '@environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private keycloakService: KeycloakService) {}

  async init(): Promise<void> {
    await this.keycloakService.init({
      config: {
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId
      },
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: true,
        checkLoginIframeInterval: 5
      },
      enableBearerInterceptor: true,
      bearerPrefix: 'Bearer',
      bearerExcludedUrls: ['/assets', '/public']
    });
  }

  login(): Promise<void> {
    return this.keycloakService.login();
  }

  logout(redirectUri?: string): Promise<void> {
    return this.keycloakService.logout(redirectUri || window.location.origin);
  }

  isLoggedIn(): Promise<boolean> {
    return this.keycloakService.isLoggedIn();
  }

  getToken(): string {
    return this.keycloakService.getToken();
  }

  getUsername(): string {
    return this.keycloakService.getUsername();
  }

  getUserRoles(): string[] {
    return this.keycloakService.getUserRoles();
  }

  hasRole(role: string): boolean {
    return this.keycloakService.isUserInRole(role);
  }

  isTokenExpired(minValidity: number = 30): boolean {
    return this.keycloakService.isTokenExpired(minValidity);
  }

  updateToken(minValidity: number = 30): Promise<boolean> {
    return this.keycloakService.updateToken(minValidity);
  }
}
