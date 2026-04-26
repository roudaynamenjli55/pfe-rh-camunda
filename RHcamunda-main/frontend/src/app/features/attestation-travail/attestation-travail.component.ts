import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; // ✅ Import indispensable

@Component({
  selector: 'app-attestation-travail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attestation-travail.component.html',
  styleUrls: ['./attestation-travail.component.css']
})
export class AttestationTravailComponent {

  // ✅ Données simulées (à remplacer par un service plus tard)
  attestationData = {
    reference: 'ATT/2024/00123',
    date: '13/02/2026',
    lieu: 'Tunis',
    banque: {
      nom: 'Banque Internationale',
      adresse: 'Avenue Habib Bourguiba, Tunis 1000',
      telephone: '+216 71 123 456',
      email: 'contact@banque-internationale.tn',
      website: 'www.banque-internationale.tn',
      rc: 'B123456789',
      tva: '1234567/A/M/000'
    },
    employe: {
      nomPrenom: 'MEJRI Ahmed',
      matricule: 'EMP-2024-001',
      cin: '12345678',
      dateNaissance: '15/05/1985',
      fonction: 'Responsable Informatique',
      departement: 'IT & Développement',
      dateEmbauche: '15/01/2020',
      typeContrat: 'CDI - Temps plein'
    }
  };

  // ✅ Injection du Router dans le constructeur pour éviter l'erreur "Property router does not exist"
  constructor(private router: Router) {}

  // Actions des boutons
  retourDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  imprimer(): void {
    window.print();
  }

  telechargerPDF(): void {
    alert('Téléchargement du PDF...');
  }

  envoyerEmail(): void {
    window.location.href = `mailto:${this.attestationData.banque.email}`;
  }
}