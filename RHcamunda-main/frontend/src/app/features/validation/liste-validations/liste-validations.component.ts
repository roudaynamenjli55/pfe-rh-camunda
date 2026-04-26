import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Interface pour structurer les données
export interface DemandeValidation {
  id: number;
  initiales: string;
  nom: string;
  role: string;
  matricule: string;
  statut: 'urgent' | 'en-attente' | 'approuve' | 'refuse';
  type: string;
  dateDebut: string;
  dateFin?: string;
  duree?: string;
  horaire?: string;
  dateSoumission: string;
  motif: string;
  justificatif?: boolean;
  progression?: string[];
  etapeActuelle?: number;
}

@Component({
  selector: 'app-liste-validations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './liste-validations.component.html',
  styleUrls: ['./liste-validations.component.css']
})
export class ListeValidationsComponent implements OnInit {
  
  // Stats du manager
  stats = {
    enAttente: 8,
    approuvees: 156,
    refusees: 12,
    tempsTraitement: '2.5j'
  };

  // Filtres
  filters = {
    type: 'Tous les types',
    motif: 'Tous les motifs',
    statut: 'Tous les statuts',
    periode: 'Mois courant'
  };

  typesOptions = ['Tous les types', 'Congé Annuel', 'Congé Maladie', 'Autorisation de Sortie', 'Congé Exceptionnel'];
  motifsOptions = ['Tous les motifs', 'Motif personnel', 'Motif de service', 'Raison médicale', 'Vacances'];
  statutsOptions = ['Tous les statuts', 'Urgent', 'En attente', 'Approuvé', 'Refusé'];
  periodesOptions = ['Mois courant', 'Mois précédent', '3 derniers mois', 'Cette année'];

  // Données exactes de la maquette
  demandes: DemandeValidation[] = [
    {
      id: 1,
      initiales: 'KT',
      nom: 'Karim Trabelsi',
      role: 'Opérations Manager',
      matricule: 'EMP-2024-005',
      statut: 'urgent',
      type: 'Congé Maladie',
      dateDebut: '15/02/2024',
      dateFin: '17/02/2024',
      duree: '3 jours',
      dateSoumission: '13/02/2024 14:30',
      motif: 'Grippe saisonnière avec fièvre. Certificat médical joint. Repos recommandé par le médecin.',
      justificatif: true,
      progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
      etapeActuelle: 1
    },
    {
      id: 2,
      initiales: 'SM',
      nom: 'Sarah Mansour',
      role: 'Développeuse',
      matricule: 'EMP-2024-012',
      statut: 'en-attente',
      type: 'Congé Annuel',
      dateDebut: '20/02/2024',
      dateFin: '25/02/2024',
      duree: '5 jours',
      dateSoumission: '12/02/2024 10:15',
      motif: 'Congé annuel pour vacances familiales. Solde disponible : 18 jours.',
      justificatif: false,
      progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
      etapeActuelle: 1
    },
    {
      id: 3,
      initiales: 'LB',
      nom: 'Leila Ben Ali',
      role: 'Analyste Financier',
      matricule: 'EMP-2024-008',
      statut: 'en-attente',
      type: 'Autorisation de Sortie',
      dateDebut: '14/02/2024',
      duree: '2h (14:00 - 16:00)',
      horaire: '14:00 - 16:00',
      dateSoumission: '13/02/2024 09:45',
      motif: 'Rendez-vous médical chez le dentiste. Justificatif de rendez-vous disponible.',
      justificatif: true,
      progression: ['Soumise', 'Chef hiér.', 'Finalisée'],
      etapeActuelle: 1
    },
    {
      id: 4,
      initiales: 'MB',
      nom: 'Mohamed Bouazizi',
      role: 'Conseiller Clientèle',
      matricule: 'EMP-2024-023',
      statut: 'urgent',
      type: 'Congé Exceptionnel',
      dateDebut: '16/02/2024',
      dateFin: '16/02/2024',
      duree: '1 jour',
      dateSoumission: '14/02/2024 16:20',
      motif: 'Décès d\'un proche. Demande de congé exceptionnel pour obsèques.',
      justificatif: true,
      progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
      etapeActuelle: 1
    }
  ];

  // Modal
  showRefuseModal = false;
  showDetailsModal = false;
  selectedDemande: DemandeValidation | null = null;
  motifRefus: string = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.filtrerDemandes();
  }

  // Navigation
  retourDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  voirProfil(matricule: string): void {
    this.router.navigate(['/profil-employe'], { queryParams: { matricule } });
  }

  // Filtres
  appliquerFiltres(): void {
    console.log('Filtres appliqués:', this.filters);
    this.filtrerDemandes();
  }

  reinitialiserFiltres(): void {
    this.filters = {
      type: 'Tous les types',
      motif: 'Tous les motifs',
      statut: 'Tous les statuts',
      periode: 'Mois courant'
    };
    this.filtrerDemandes();
  }

  filtrerDemandes(): void {
    // Logique de filtrage (à connecter au backend)
    console.log('Filtrage en cours...');
  }

  // Actions sur les demandes
  approuver(demande: DemandeValidation): void {
    if (confirm(`✅ Approuver la demande de ${demande.nom} ?`)) {
      demande.statut = 'approuve';
      if (demande.etapeActuelle !== undefined) {
        demande.etapeActuelle = demande.progression?.length ?? 1;
      }
      this.stats.enAttente--;
      this.stats.approuvees++;
      alert(`Demande #${demande.id} de ${demande.nom} approuvée avec succès !`);
    }
  }

  ouvrirRefus(demande: DemandeValidation): void {
    this.selectedDemande = demande;
    this.motifRefus = '';
    this.showRefuseModal = true;
  }

  fermerModal(): void {
    this.showRefuseModal = false;
    this.showDetailsModal = false;
    this.selectedDemande = null;
    this.motifRefus = '';
  }

  confirmerRefus(): void {
    if (this.selectedDemande && this.motifRefus.trim()) {
      this.selectedDemande.statut = 'refuse';
      this.stats.enAttente--;
      this.stats.refusees++;
      this.fermerModal();
      alert(`❌ Demande #${this.selectedDemande.id} refusée.\nMotif: ${this.motifRefus}`);
    } else if (!this.motifRefus.trim()) {
      alert('⚠️ Veuillez saisir un motif de refus.');
    }
  }

  voirDetails(demande: DemandeValidation): void {
    this.selectedDemande = demande;
    this.showDetailsModal = true;
  }

  telechargerJustificatif(demande: DemandeValidation): void {
    alert(`📎 Téléchargement du justificatif pour la demande #${demande.id}\nEmployé: ${demande.nom}`);
  }

  exporterExcel(): void {
    alert('📊 Export Excel en cours...\nLe fichier sera téléchargé sous quelques secondes.');
  }

  // Helpers UI
  getBadgeClass(statut: string): string {
    const map: Record<string, string> = {
      'urgent': 'badge-urgent',
      'approuve': 'badge-success',
      'refuse': 'badge-danger',
      'en-attente': 'badge-warning'
    };
    return map[statut] || 'badge-default';
  }

  getStatutLabel(statut: string): string {
    const map: Record<string, string> = {
      'urgent': '🔥 URGENT',
      'approuve': '✓ Approuvé',
      'refuse': '✗ Refusé',
      'en-attente': '⏳ En attente'
    };
    return map[statut] || statut;
  }

  getTypeIcon(type: string): string {
    if (type.includes('Maladie')) return '🏥';
    if (type.includes('Autorisation')) return '🔐';
    if (type.includes('Exceptionnel')) return '⭐';
    return '📅';
  }

  getProgressClass(index: number, current: number | undefined): string {
    if (current === undefined) return '';
    if (index < current) return 'completed';
    if (index === current) return 'active';
    return '';
  }
}