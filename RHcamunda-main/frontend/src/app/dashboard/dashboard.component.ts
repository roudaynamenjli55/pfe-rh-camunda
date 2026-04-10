import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, User } from '../core/services/user.service';
import { KeycloakService } from '../core/services/keycloak.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div class="dashboard">
      <h1>Bienvenue {{ username }}!</h1>
      
      <h2>Utilisateurs:</h2>
      @if (users.length > 0) {
        <ul>
          @for (user of users; track user.id) {
            <li>{{ user.username }} - {{ user.email }}</li>
          }
        </ul>
      }
      
      @if (users.length === 0) {
        <p>Aucun utilisateur</p>
      }
      
      <button (click)="loadUsers()">Rafraîchir</button>
      <button (click)="logout()">Déconnexion</button>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  users: User[] = [];
  username = '';

  constructor(
    private userService: UserService,
    private keycloakService: KeycloakService
  ) {}

  ngOnInit(): void {
    this.username = this.keycloakService.getUsername();
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.users = data;
        console.log('✅ Users loaded:', data);
      },
      error: (err) => {
        console.error('❌ Error loading users:', err);
      }
    });
  }

  logout(): void {
    this.keycloakService.logout();
  }
}