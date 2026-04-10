import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-liste-conges',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './liste-conges.component.html',
  styleUrls: ['./liste-conges.component.css']
})
export class ListeCongesComponent {

  // ✅ 1. Données pour le Header (Titres)
  pageTitle: string = 'Mes Demandes de Congé';
  pageSubtitle: string = 'Gérez vos demandes de congés et absences';

  // ✅ 2. Données pour la Sidebar (Profil utilisateur)
  currentUser = {
    initials: 'BM',
    name: 'Ben Mohamed Ali',
    role: 'Conseiller · Agence Tunis'
  };

  // ✅ 3. Statistiques
  stats = {
    joursRestants: 22,
    demandesAnnee: 5,
    enAttente: 2,
    joursPris: 18
  };

  // ✅ 4. Filtres
  filters = {
    typeConge: 'Tous les types',
    statut: 'Tous les statuts',
    periode: 'Toutes les périodes'
  };

  typesConge = ['Tous les types', 'Congé annuel', 'Congé maladie', 'Congé exceptionnel', 'Congé maternité/paternité'];
  statuts = ['Tous les statuts', 'En attente', 'Approuvé', 'Refusé'];
  periodes = ['Toutes les périodes', 'Ce mois', '3 derniers mois', 'Cette année', 'Année dernière'];

  // ✅ 5. Liste des demandes (Les cartes)
  demandes = [
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

  // ✅ 7. Propriétés supplémentaires
  showFilters: boolean = true;
  isLoading: boolean = false;

  // ✅ 6. Méthodes pour les boutons
  handleLogout(): void { 
    console.log('Déconnexion...');
    alert('Vous êtes déconnecté.');
  }

  getStatusClass(statut: string): string {
    switch (statut) {
      case 'En attente': return 'en-attente';
      case 'Approuvé': return 'approuve';
      case 'Refusé': return 'refuse';
      default: return '';
    }
  }

  getTypeIcon(type: string): string {
    if (type.includes('Maladie')) return '🏥';
    if (type.includes('Autorisation')) return '🔐';
    return '📅';
  }

  sauvegarderModifications(d: any): void { alert('✅ Modifications sauvegardées !'); }
  nouvelleDemande(): void { alert('📝 Création nouvelle demande'); }
  filtrer(): void { console.log('Filtres appliqués'); }
  voirDetails(d: any): void { console.log('Détails:', d); }
  modifier(d: any): void { console.log('Modifier:', d); }
  annuler(d: any): void { if(confirm('Annuler la demande ?')) d.statut = 'Annulé'; }
  telecharger(d: any): void { alert('📄 Téléchargement du titre de congé'); }

  // ✅ 8. Méthodes supplémentaires manquantes
  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  reinitialiserFiltres(): void {
    this.filters = {
      typeConge: 'Tous les types',
      statut: 'Tous les statuts',
      periode: 'Toutes les périodes'
    };
    console.log('Filtres réinitialisés');
  }

  calculerDuree(dateDebut: string, dateFin: string): number {
    if (!dateDebut || !dateFin) return 0;
    const debut = new Date(dateDebut);
    const fin = new Date(dateFin);
    const diffTime = Math.abs(fin.getTime() - debut.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
  }

  telechargerTitreConge(demande: any): void {
    alert(`📄 Téléchargement du titre de congé pour la demande #${demande.id}`);
  }

  voirJustificatif(demande: any): void {
    alert(`📎 Affichage du justificatif pour la demande #${demande.id}`);
  }
}