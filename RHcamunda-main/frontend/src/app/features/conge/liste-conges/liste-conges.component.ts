import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';  // ✅ Contient déjà NgFor, NgIf
import { FormsModule, ReactiveFormsModule } from '@angular/forms';     // ✅ Pour [(ngModel)] des filtres
import { Router } from '@angular/router';
import { ViewEncapsulation } from '@angular/core';

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
  selector: 'app-liste-conges',  // ✅ Correct
  standalone: true,
    templateUrl: './liste-conges.component.html',
 imports: [CommonModule, ReactiveFormsModule, FormsModule],  // ✅ Simplifié : pas besoin de NgFor/NgIf séparés


  styleUrl: './liste-conges.component.css',
  encapsulation: ViewEncapsulation.None 
})

export class ListeCongesComponent implements OnInit {
  
  // ✅ DONNÉES POUR LA LISTE DES CONGÉS
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

  constructor(private router: Router) {}  // ✅ Pas besoin de FormBuilder ici

  ngOnInit(): void {
    this.filtrerDemandes();  // ✅ Initialisation des filtres
  }

  // ── Navigation ─────────────────────────────────────────────
  navigateTo(route: string): void {
    console.log('Naviguer vers:', route);
    this.router.navigate([`/conges/${route}`]);
  }

  logout(): void {
    console.log('Déconnexion...');
    this.router.navigate(['/login']);
  }

  nouvelleDemande(): void {
    console.log('📝 Nouvelle demande');
    this.router.navigate(['/conges/nouvelle-demande']);
  }

  // ── Actions sur les demandes ───────────────────────────────
  voirDetails(demande: DemandeConge): void {
    alert(`📋 Détails #${demande.id}\nType: ${demande.type}\nDurée: ${demande.duree} jours`);
  }

  modifier(demande: DemandeConge): void {
    alert(`✏️ Modification demande #${demande.id}`);
  }

  annuler(demande: DemandeConge): void {
    if (confirm(`⚠️ Annuler demande #${demande.id} ?`)) {
      demande.statut = 'Annulé';
      this.stats.enAttente--;
      alert('✅ Demande annulée');
    }
  }

  telechargerTitre(demande: DemandeConge): void {
    alert(`📄 Téléchargement titre pour demande #${demande.id}`);
  }

  voirJustificatif(demande: DemandeConge): void {
    alert(`📎 Affichage justificatif pour demande #${demande.id}`);
  }

  // ── Filtres ────────────────────────────────────────────────
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
      return typeMatch && statutMatch;
    });
  }

  // ── Helpers UI ─────────────────────────────────────────────
  getTypeIcon(type: string): string {
    if (type.includes('Maladie')) return '🏥';
    if (type.includes('Autorisation')) return '🔐';
    if (type.includes('Maternité')) return '👶';
    return '📅';
  }

  getTypeIconClass(type: string): string {
    if (type.includes('Maladie')) return 'maladie';
    if (type.includes('Autorisation')) return 'autorisation';
    if (type.includes('Maternité')) return 'maternite';
    return '';
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