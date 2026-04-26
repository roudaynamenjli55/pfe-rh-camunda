import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profil-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil-employe.component.html',
  styleUrls: ['./profil-employe.component.css']
})
export class ProfilEmployeComponent implements OnInit {
  
  activeTab: string = 'informations';
  
  employe = {
    nomComplet: 'Zaineb Souissi',
    matricule: 'EMP-2024-001',
    fonction: 'Responsable Informatique',
    departement: 'IT & Développement',
    agence: 'Siège Social - Tunis',
    dateEmbauche: '15 Janvier 2020',
    anciennete: '4 ans et 2 mois',
    email: 'zaineb.souissi@banque.tn',
    telephone: '+216 22 333 444',
    statut: 'Actif',
    cin: '12345678',
    dateNaissance: '15 Mai 1985',
    situationFamiliale: 'Marié(e)',
    nombreEnfants: 2,
    adresse: 'Tunis, Tunisie',
    typeContrat: 'CDI - Temps plein',
    chefHierarchique: 'Mme. KHALIL Sarah',
    niveauHierarchique: 'Cadre'
  };

  stats = {
    joursCongeRestants: 22,
    congesPris: 18,
    autorisations: 8,
    tauxPresence: 93.1
  };

  historiqueCarriere = [
    {
      date: 'Janvier 2023',
      type: 'Promotion',
      poste: 'Responsable Informatique',
      description: 'Promotion au poste de Responsable du département IT & Développement'
    },
    {
      date: 'Mars 2021',
      type: 'Changement de département',
      poste: 'Mutation vers le département IT & Développement',
      description: ''
    },
    {
      date: 'Janvier 2020',
      type: 'Embauche',
      poste: 'Développeur Senior',
      description: 'Intégration à la Banque Internationale en tant que Développeur Senior'
    }
  ];

  documents = [
    {
      titre: 'Attestation de travail',
      dateGeneration: '10/02/2024',
      reference: 'ATT/2024/00123'
    },
    {
      titre: 'Fiche d\'évaluation 2023',
      dateGeneration: '05/01/2024',
      reference: 'EVAL/2023/001'
    },
    {
      titre: 'Titre de congé',
      dateGeneration: '01/02/2024',
      reference: 'TC/2024/00789'
    }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {}

  // ✅ MÉTHODE AJOUTÉE : Retour au dashboard
  retourDashboard(): void {
    this.router.navigate(['/conges/mes-conges']);
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  envoyerEmail(): void {
    window.location.href = `mailto:${this.employe.email}`;
  }

  genererAttestation(): void {
    alert(`📄 Génération d'une attestation pour ${this.employe.nomComplet}\nMatricule: ${this.employe.matricule}`);
  }

  voirEvaluations(): void {
    alert('📊 Redirection vers les évaluations de performance');
    this.setActiveTab('evaluations');
  }

  consulterConges(): void {
    this.router.navigate(['/conges/mes-conges']);
  }

  voirDocument(doc: any): void {
    alert(`👁️ Affichage du document:\n${doc.titre}\nRéf: ${doc.reference}`);
  }

  telechargerDocument(doc: any): void {
    alert(`📥 Téléchargement de:\n${doc.titre}\nRéf: ${doc.reference}`);
  }

  nouveauDocument(): void {
    alert('📄 Formulaire de demande de nouveau document');
  }

  modifierProfil(): void {
    alert('✏️ Mode édition activé');
  }

  // ✅ MÉTHODE AJOUTÉE : Pour éviter la Regex dans le HTML
  getTimelineTypeClass(type: string): string {
    return 'type-' + type.toLowerCase().replace(/\s+/g, '-');
  }

  // Helpers pour les classes CSS
  getStatutClass(): string {
    return this.employe.statut === 'Actif' ? 'status-active' : 'status-inactive';
  }

  getHistoriqueIcon(type: string): string {
    switch (type) {
      case 'Promotion': return '🚀';
      case 'Embauche': return '🎉';
      case 'Changement de département': return '🔄';
      default: return '📌';
    }
  }
}