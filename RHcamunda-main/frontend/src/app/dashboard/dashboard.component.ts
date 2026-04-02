import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, User } from '../core/services/user.service';
import { KeycloakService } from '../core/services/keycloak.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  template: `
    <div class="dashboard">
      <h1>Bienvenue {{ username }}!</h1>
      
      <h2>Utilisateurs:</h2>
      <ul *ngIf="users.length > 0">
        <li *ngFor="let user of users">
          {{ user.username }} - {{ user.email }}
        </li>
      </ul>
      
      <p *ngIf="users.length === 0">Aucun utilisateur</p>
      
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