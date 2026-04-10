import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';  // ← AJOUTE ÇA ✅

@Component({
  selector: 'app-titre-conge',
  standalone: true,
  imports: [CommonModule, FormsModule],  // ← AJOUTE FormsModule ICI ✅
  templateUrl: './titre-conge.component.html',
  styleUrls: ['./titre-conge.component.css']
})
export class TitreCongeComponent {
  @Input() titreConge: any = {
    reference: 'TC/2024/00789',
    dateEmission: '13/02/2026',
    statut: 'APPROUVÉ',
    employe: {
      nomPrenom: 'MEJRI Ahmed',
      matricule: 'EMP-2024-001',
      fonction: 'Responsable Informatique',
      departement: 'IT& Développement',
      cin: '12345678',
      agence: 'Siège- Tunis',
      chefHierarchique: 'Mme KHALIL Sarah',
      telephone: '+216 XX XXX XXX'
    },
    conge: {
      type: 'CONGÉ ANNUEL',
      dateDebut: '2026-02-20',  // Format YYYY-MM-DD pour input date
      dateFin: '2026-02-26',
      dateReprise: '2026-02-27',
      dureeCalendaires: 7,
      joursOuvrables: 5,
      motif: 'Vacances annuelles',
      soldeAvant: 22,
      soldeApres: 17
    },
    validations: [
      {
        role: 'Chef hiérarchique',
        nom: 'KHALIL Sarah',
        date: '2026-02-13',
        statut: 'Approuvé'
      },
      {
        role: 'Service RH',
        nom: 'TRABELSI Karim',
        date: '2026-02-13',
        statut: 'Validé'
      }
    ]
  };

  imprimerTitreConge(): void {
    window.print();
  }

  sauvegarderTitreConge(): void {
    console.log('Titre de congé sauvegardé:', this.titreConge);
    alert('Titre de congé sauvegardé avec succès!');
    // Ici tu peux appeler ton service pour sauvegarder dans la BDD
  }
}