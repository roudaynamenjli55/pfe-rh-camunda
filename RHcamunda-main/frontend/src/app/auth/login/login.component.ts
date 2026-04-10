import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface DemandeConge {
  id: number;
  type: string;
  dateDemande: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  motif: string;
  statut: 'En attente' | 'Approuvé' | 'Refusé' | 'Annulé';
  validePar?: string;
  justificatif: boolean;
  progression: string[];
  etapeActuelle: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  showPassword = false;

  // Propriétés pour la gestion des congés
  stats = {
    joursRestants: 22,
    demandesAnnee: 5,
    enAttente: 2,
    joursPris: 18
  };

  filters = {
    typeConge: 'Tous les types',
    statut: 'Tous les statuts',
    periode: 'Toutes les périodes'
  };

  typesConge = ['Tous les types', 'Congé annuel', 'Congé maladie', 'Congé exceptionnel', 'Congé maternité/paternité'];
  statuts = ['Tous les statuts', 'En attente', 'Approuvé', 'Refusé'];
  periodes = ['Toutes les périodes', 'Ce mois', '3 derniers mois', 'Cette année', 'Année dernière'];

  demandes: DemandeConge[] = [
    {
      id: 1, type: 'Congé Annuel', dateDemande: '2024-02-10', dateDebut: '2024-03-20', dateFin: '2024-03-25',
      duree: 4, motif: 'Vacances familiales', statut: 'En attente', justificatif: false,
      progression: ['Soumise', 'En validation', 'Service RH', 'Finalisée'], etapeActuelle: 1
    },
    {
      id: 2, type: 'Congé Annuel', dateDemande: '2024-01-05', dateDebut: '2024-02-15', dateFin: '2024-02-20',
      duree: 4, motif: 'Repos', statut: 'Approuvé', validePar: 'Mme KHALIL Sarah', justificatif: false,
      progression: ['Soumise', 'Chef', 'RH', 'Finalisée'], etapeActuelle: 3
    },
    {
      id: 3, type: 'Congé Maladie', dateDemande: '2024-02-12', dateDebut: '2024-02-12', dateFin: '2024-02-14',
      duree: 3, motif: 'Maladie', statut: 'En attente', justificatif: true,
      progression: ['Soumise', 'Chef', 'Service RH', 'Finalisée'], etapeActuelle: 2
    }
  ];

  demandesFiltrees: DemandeConge[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
    this.filtrerDemandes();
  }

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  showForgotPasswordAlert(): void {
    alert('Fonctionnalité "Mot de passe oublié" sera disponible prochainement.');
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    console.log('Tentative de connexion...');

    // Simulation de connexion réussie
    setTimeout(() => {
      this.isLoading = false;
      this.router.navigate(['/conges/mes-conges']);
    }, 1000);
  }

  // Méthodes pour la navigation
  navigateTo(route: string): void {
    console.log('Naviguer vers:', route);
  }

  logout(): void {
    console.log('Déconnexion...');
    alert('Vous êtes déconnecté.');
    this.router.navigate(['/login']);
  }

  // Méthodes pour les demandes de congé
  nouvelleDemande(): void {
    alert('📝 Création nouvelle demande de congé');
  }

  voirDetails(demande: DemandeConge): void {
    alert(`📋 Détails de la demande #${demande.id}\nType: ${demande.type}\nDurée: ${demande.duree} jours`);
  }

  modifier(demande: DemandeConge): void {
    alert(`✏️ Modification de la demande #${demande.id}`);
  }

  annuler(demande: DemandeConge): void {
    if (confirm(`⚠️ Êtes-vous sûr de vouloir annuler la demande #${demande.id} ?`)) {
      demande.statut = 'Annulé';
      this.stats.enAttente--;
      alert('✅ Demande annulée');
    }
  }

  telechargerTitre(demande: DemandeConge): void {
    alert(`📄 Téléchargement du titre de congé pour la demande #${demande.id}`);
  }

  voirJustificatif(demande: DemandeConge): void {
    alert(`📎 Affichage du justificatif pour la demande #${demande.id}`);
  }

  // Méthodes pour les filtres
  appliquerFiltres(): void {
    console.log('Filtres appliqués:', this.filters);
    this.filtrerDemandes();
  }

  reinitialiserFiltres(): void {
    this.filters = {
      typeConge: 'Tous les types',
      statut: 'Tous les statuts',
      periode: 'Toutes les périodes'
    };
    this.filtrerDemandes();
    console.log('Filtres réinitialisés');
  }

  filtrerDemandes(): void {
    this.demandesFiltrees = this.demandes.filter(demande => {
      const typeMatch = this.filters.typeConge === 'Tous les types' || demande.type.includes(this.filters.typeConge);
      const statutMatch = this.filters.statut === 'Tous les statuts' || demande.statut === this.filters.statut;
      const periodeMatch = this.filters.periode === 'Toutes les périodes'; // Logique de période simplifiée
      return typeMatch && statutMatch && periodeMatch;
    });
  }

  // Méthodes pour les icônes et styles
  getTypeIcon(type: string): string {
    if (type.includes('Maladie')) return '🏥';
    if (type.includes('Autorisation')) return '🔐';
    if (type.includes('Maternité')) return '👶';
    return '📅';
  }

  getTypeIconClass(type: string): string {
    if (type.includes('Maladie')) return 'icon-health';
    if (type.includes('Autorisation')) return 'icon-auth';
    if (type.includes('Maternité')) return 'icon-maternity';
    return 'icon-vacation';
  }

  getStatusIcon(statut: string): string {
    switch (statut) {
      case 'En attente': return '⏳';
      case 'Approuvé': return '✅';
      case 'Refusé': return '❌';
      case 'Annulé': return '🗑️';
      default: return '❓';
    }
  }

  getStatusClass(statut: string): string {
    switch (statut) {
      case 'En attente': return 'en-attente';
      case 'Approuvé': return 'approuve';
      case 'Refusé': return 'refuse';
      case 'Annulé': return 'annule';
      default: return '';
    }
  }

  getStepIcon(demande: DemandeConge, index: number): string {
    if (index < demande.etapeActuelle) return '✅';
    if (index === demande.etapeActuelle) return '⏳';
    return '⭕';
  }
}