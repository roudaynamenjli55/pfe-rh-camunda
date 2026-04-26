// src/app/app.component.ts
import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  title = 'RHcamunda';
  isLoggedIn = true;
  isManager = true; // ← Pour afficher le menu "Validation Demandes"

  constructor(private router: Router) {}
/*
  ngOnInit(): void {
    // Vérifier l'authentification au chargement initial
    this.checkAuth();
    
    // Mettre à jour l'état à chaque changement de route
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkAuth();
    });
  }
  */
ngOnInit(): void {
  this.checkAuth();
  
  this.router.events.pipe(
    filter(event => event instanceof NavigationEnd)
  ).subscribe(() => {
    this.checkAuth();
  });
}


checkAuth(): void {
  const params = new URLSearchParams(window.location.search);
  const keycloakError = params.get('error');
  
  // 🔴 Handle error - redirect to login
  if (keycloakError === 'login_required') {
    console.log('🔑 Keycloak: User needs to login');
    
    if (window.history.replaceState) {
      window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    this.isLoggedIn = false;
    this.isManager = false;
    
    // ✅ Actually redirect if not already on login page
    if (!this.router.url.includes('/auth/login')) {
      this.router.navigate(['/auth/login']);
    }
    return;
  }
  
  // Check token
  const token = localStorage.getItem('auth_token');
  const userRole = localStorage.getItem('user_role');
  
  this.isLoggedIn = !!token;
  this.isManager = userRole === 'manager' || userRole === 'admin';
  
  console.log('Auth check:', { isLoggedIn: this.isLoggedIn, isManager: this.isManager });
}
  /**
   * Vérifie si l'utilisateur est authentifié
   * (Méthode unique fusionnant toutes les vérifications)
   */

  /*
  checkAuth(): void {
    // 1️⃣ Handle Keycloak callback: error=login_required
    const params = new URLSearchParams(window.location.search);
    const keycloakError = params.get('error');
    
    if (keycloakError === 'login_required') {
      console.log('🔑 Keycloak: User needs to login');
      
      // Clear the query params to avoid re-processing
      if (window.history.replaceState) {
        window.history.replaceState({}, document.title, window.location.pathname);
      }
      
      // Show login state (but don't force redirect - let UI handle it)
      this.isLoggedIn = false;
      this.isManager = false;
      
      // Optional: Auto-redirect to login page (uncomment if you want)
      // if (!this.router.url.startsWith('/auth')) {
      //   this.router.navigate(['/auth/login']);
      // }
      
      return; // ← Important: on sort ici pour ne pas exécuter la suite
    }
    
    // 2️⃣ DEV BYPASS (for testing without Keycloak)
    if (window.location.search.includes('dev-bypass=true')) {
      console.log('🔓 Dev bypass activé via URL');
      this.isLoggedIn = true;
      this.isManager = false;
      if (!localStorage.getItem('auth_token')) {
        localStorage.setItem('auth_token', 'dev-bypass-token');
        localStorage.setItem('user_role', 'employee');
      }
      return;
    }

    // 3️⃣ Normal auth check (unchanged - ton code original)
    const token = localStorage.getItem('auth_token') || sessionStorage.getItem('auth_token');
    const userRole = localStorage.getItem('user_role');
    
    this.isLoggedIn = !!token;
    this.isManager = userRole === 'manager' || userRole === 'admin';
    
    // Rediriger vers login si pas authentifié et pas déjà sur une page auth
    // (Ton code original commenté, conservé tel quel)
    // if (!this.isLoggedIn && !this.router.url.includes('/auth')) {
    //  this.router.navigate(['/auth/login']);
    // }
  }
*/
  /**
   * Déconnexion de l'utilisateur
   */
  logout(): void {
    if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      // Supprimer les tokens de session
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user_role');
      sessionStorage.removeItem('auth_token');
      
      // Mettre à jour l'état local
      this.isLoggedIn = false;
      this.isManager = false;
      
      // Rediriger vers la page de login
      this.router.navigate(['/auth/login']);
    }
  }

  /**
   * Navigation vers le dashboard principal
   */
  goToDashboard(): void {
    if (this.isManager) {
      this.router.navigate(['/validation/demandes']);
    } else {
      this.router.navigate(['/conges/mes-conges']);
    }
  }
}