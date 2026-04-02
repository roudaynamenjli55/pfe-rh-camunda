import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  constructor(private api: ApiService) {}

  // Récupérer tous les utilisateurs
  getUsers(): Observable<User[]> {
    return this.api.get<User[]>('users');
  }

  // Récupérer un utilisateur par ID
  getUserById(id: number): Observable<User> {
    return this.api.get<User>(`users/${id}`);
  }

  // Créer un utilisateur
  createUser(user: Partial<User>): Observable<User> {
    return this.api.post<User>('users', user);
  }

  // Mettre à jour un utilisateur
  updateUser(id: number, user: Partial<User>): Observable<User> {
    return this.api.put<User>(`users/${id}`, user);
  }

  // Supprimer un utilisateur
  deleteUser(id: number): Observable<void> {
    return this.api.delete<void>(`users/${id}`);
  }
}