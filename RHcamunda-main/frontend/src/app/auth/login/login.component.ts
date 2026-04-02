import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';
import { KeycloakService } from '../../core/services/keycloak.service'; // ← Ajoute hadhi

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink]
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private keycloakService: KeycloakService, // ← Ajoute hadhi
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    console.log('=== LOGIN SUBMITTED ===');
    this.isLoading = true;
    this.errorMessage = '';

    // Rediriger vers Keycloak (sans subscribe)
    this.keycloakService.login();
    
    // MA PAS de router.navigate ici!
    // Keycloak va gérer la redirection
  }

  loginWithGoogle(): void {
    console.log('Connexion Google — à implémenter');
  }

  loginWithLinkedIn(): void {
    console.log('Connexion LinkedIn — à implémenter');
  }
}