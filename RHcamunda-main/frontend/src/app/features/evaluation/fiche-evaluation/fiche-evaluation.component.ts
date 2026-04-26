import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface CritereEvaluation {
  nom: string;
  note: number;
  maxNote: number;
}

interface Employe {
  id: number;
  nom: string;
  matricule: string;
  departement: string;
  fonction: string;
  dateEmbauche: string;
  statut: 'actif' | 'inactif';
  performance: 'excellente' | 'bonne' | 'moyenne';
}

@Component({
  selector: 'app-fiche-evaluation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fiche-evaluation.component.html',
  styleUrls: ['./fiche-evaluation.component.css']
})
export class FicheEvaluationComponent implements OnInit {
  
  annee = 2024;
  reference = 'EVAL-2024-001';
  dateEvaluation = '12/02/2024';

  evalue = {
    nomPrenom: 'MELKI Ahmed',
    matricule: 'EMP-2024-001',
    fonction: 'Responsable Informatique',
    departement: 'IT & Développement',
    anciennete: '4 ans et 2 mois',
    evaluateur: 'KHALIL Sarah (Manager)'
  };

  periodeEval = {
    dateDebut: '01/01/2024',
    dateFin: '31/12/2024',
    joursTravailles: 243,
    totalJours: 365,
    congesPris: 18,
    congesAnnuel: 18,
    congesMaladie: 0,
    congesPersonnels: 0,
    tauxPresence: 93.1,
    autorisations: 8,
    detailsAutorisations: '8 autorisations (Personnel & Service)'
  };

  criteres: CritereEvaluation[] = [
    { nom: 'Compétences techniques', note: 5, maxNote: 5 },
    { nom: 'Qualité du travail', note: 4, maxNote: 5 },
    { nom: 'Respect des délais', note: 4.5, maxNote: 5 },
    { nom: 'Autonomie', note: 4, maxNote: 5 },
    { nom: 'Esprit d\'équipe', note: 5, maxNote: 5 },
    { nom: 'Initiative et créativité', note: 4.5, maxNote: 5 },
    { nom: 'Communication', note: 4, maxNote: 5 },
    { nom: 'Gestion du stress', note: 4, maxNote: 5 }
  ];

  statsEmployes = {
    total: 247,
    actifs: 222,
    nouveaux: 11,
    departements: 4
  };

  employes: Employe[] = [
    { id: 1, nom: 'Ahmed Melki', matricule: 'EMP-2024-001', departement: 'IT & Développement', fonction: 'Senior Developer', dateEmbauche: '15 Jan 2020', statut: 'actif', performance: 'excellente' },
    { id: 2, nom: 'Sarah Khalil', matricule: 'EMP-2024-002', departement: 'IT & Développement', fonction: 'Project Manager', dateEmbauche: '10 Mar 2019', statut: 'actif', performance: 'excellente' },
    { id: 3, nom: 'Mohamed Ali', matricule: 'EMP-2024-003', departement: 'Ressources Humaines', fonction: 'HR Manager', dateEmbauche: '22 Sep 2018', statut: 'actif', performance: 'bonne' },
    { id: 4, nom: 'Fatma Ben Ali', matricule: 'EMP-2024-004', departement: 'Finance', fonction: 'Financial Analyst', dateEmbauche: '05 Jul 2021', statut: 'actif', performance: 'bonne' },
    { id: 5, nom: 'Karim Trabelsi', matricule: 'EMP-2024-005', departement: 'Opérations', fonction: 'Operations Manager', dateEmbauche: '18 Nov 2020', statut: 'actif', performance: 'excellente' }
  ];

  constructor(private router: Router) {}

  ngOnInit(): void {}

  getNotePercentage(note: number, maxNote: number): number {
    return (note / maxNote) * 100;
  }

  getNoteColor(note: number): string {
    if (note >= 4.5) return '#4caf50';
    if (note >= 3.5) return '#2196f3';
    if (note >= 2.5) return '#ff9800';
    return '#f44336';
  }

  getPerformanceBadgeClass(performance: string): string {
    switch (performance) {
      case 'excellente': return 'badge-excellente';
      case 'bonne': return 'badge-bonne';
      default: return 'badge-moyenne';
    }
  }

  getStatutBadgeClass(statut: string): string {
    return statut === 'actif' ? 'badge-actif' : 'badge-inactif';
  }

  retourDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  exporterPDF(): void {
    alert('Exportation PDF en cours...');
  }

  imprimerFiche(): void {
    window.print();
  }
}