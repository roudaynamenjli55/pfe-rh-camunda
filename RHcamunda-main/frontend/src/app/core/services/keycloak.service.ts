import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { keycloakConfig } from '../config/keycloak.config';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {
  private keycloak: Keycloak;

  constructor() {
    this.keycloak = new Keycloak(keycloakConfig);
  }

  async init(): Promise<boolean> {
    try {
      console.log('🔑 Initializing Keycloak...');
      console.log('Config:', keycloakConfig);
      
      const authenticated = await this.keycloak.init({
        onLoad: 'login-required',  // ← CHANGÉ: 'check-sso' → 'login-required' ✅
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false,   // ← Désactive iframe check
        pkceMethod: 'S256',        // ← Sécurité OAuth2
        responseMode: 'query'      // ← Meilleure compatibilité
      });
      
      console.log('✅ Keycloak initialized:', authenticated);
      console.log('Authenticated:', this.keycloak.authenticated);
      
      return authenticated;
    } catch (error) {
      console.error('❌ Keycloak init error:', error);
      return false;
    }
  }

  login(): void {
    console.log('🔐 Keycloak login called');
    this.keycloak.login();
  }

  logout(): void {
    console.log('🚪 Keycloak logout called');
    this.keycloak.logout({ 
      redirectUri: window.location.origin + '/auth/login'  // ← Dynamique ✅
    });
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  isAuthenticated(): boolean {
    return this.keycloak.authenticated ?? false;
  }

  getUsername(): string {
    return (this.keycloak.tokenParsed?.['preferred_username'] as string) ?? '';
  }
}