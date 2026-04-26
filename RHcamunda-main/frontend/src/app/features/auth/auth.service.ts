import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private keycloakService: KeycloakService) {}

  login(email: string, password: string, rememberMe: boolean): Observable<any> {
    console.log('=== AUTH SERVICE LOGIN ===');
    // Ne pas appeler keycloakService.login() ici
    // Car login.component.ts l'appelle déjà
    return of({ success: true });
  }

  logout(): void {
    this.keycloakService.logout();
  }

  isAuthenticated(): boolean {
    return this.keycloakService.isAuthenticated();
  }

  getToken(): string | null {
    return this.keycloakService.getToken() ?? null;
  }

  getUsername(): string {
    return this.keycloakService.getUsername();
  }
}