import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  async canActivate(): Promise<boolean> {
    try {
      const isLoggedIn = await this.keycloakService.isLoggedIn();
      
      if (!isLoggedIn) {
        await this.keycloakService.login();
        return false;
      }

      return true;
    } catch (error) {
      console.error('Auth guard error:', error);
      this.router.navigate(['/auth/login']);
      return false;
    }
  }
}
