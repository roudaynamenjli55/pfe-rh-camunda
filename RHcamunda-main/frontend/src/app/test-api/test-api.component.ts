import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule, JsonPipe } from '@angular/common';
import { environment } from '../../environments/environment'; // Importé ici

@Component({
  selector: 'app-test-api',
  standalone: true,
  imports: [CommonModule, JsonPipe],
  template: `
    <div style="padding: 20px; font-family: Arial;">
      <h2>🧪 Test Connexion Frontend ↔ Backend</h2>
      
      <button 
        (click)="testApi()" 
        [disabled]="loading || !isAuthenticated()"
        style="padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; margin-bottom: 15px;">
        {{ loading ? 'Chargement...' : 'Tester API /users' }}
      </button>
      
      <!-- Loading -->
      <div *ngIf="loading" style="color: blue; font-weight: bold;">
        ⏳ Chargement en cours...
      </div>
      
      <!-- Erreur -->
      <div *ngIf="error" style="margin-top: 10px; color: red; padding: 10px; background: #ffe6e6; border-radius: 5px; border: 1px solid red;">
        ❌ Erreur: {{ error }}
      </div>
      
      <!-- Données (Succès) -->
      <div *ngIf="data" style="margin-top: 20px;">
        <h3 style="color: green;">✅ Résultat (Données du backend):</h3>
        <pre style="background: #f4f4f4; padding: 15px; border-radius: 5px; overflow-x: auto; border: 1px solid #ddd;">{{ data | json }}</pre>
      </div>
      
      <!-- Pas connecté -->
      <div *ngIf="!isAuthenticated()" style="margin-top: 20px; padding: 15px; background: #fff3cd; border-radius: 5px; border: 1px solid orange; color: #856404;">
        <strong>⚠️ Attention:</strong><br>
        Vous n'êtes pas connecté avec Keycloak.<br>
        Veuillez vous connecter d'abord sur la page de login, puis revenez ici.
      </div>
      
      <div style="margin-top: 30px; padding: 15px; background: #e2e3e5; border-radius: 5px;">
        <strong>ℹ️ Info Technique:</strong><br>
        URL appelée: <code>{{ apiUrl }}/users</code><br>
        Si tu vois les données ci-dessus → <strong>Frontend et Backend sont connectés!</strong> 🎉
      </div>
    </div>
  `
})
export class TestApiComponent {
  // On crée une propriété publique pour l'URL, accessible dans le template
  public apiUrl = environment.apiUrl; 
  
  data: any = null;
  loading = false;
  error = '';

  constructor(private http: HttpClient) {}

  isAuthenticated(): boolean {
    const token = localStorage.getItem('keycloak-token');
    return !!token || !!(window as any).keycloak?.authenticated;
  }

  testApi(): void {
    if (!this.isAuthenticated()) {
      this.error = 'Vous devez être connecté pour tester l\'API.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.data = null;

    // Utilisation de la propriété publique
    const url = `${this.apiUrl}/users`;
    console.log('🔍 Test API:', url);

    this.http.get(url).subscribe({
      next: (response) => {
        console.log('✅ Réponse API:', response);
        this.data = response;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur API:', err);
        this.error = err.status === 401 
          ? 'Non autorisé (401). Vérifiez votre token Keycloak.' 
          : err.status === 0
          ? 'Erreur de connexion (0). Le backend est-il démarré sur le port 8081 ?'
          : `Erreur ${err.status}: ${err.message}`;
        this.loading = false;
      }
    });
  }
}