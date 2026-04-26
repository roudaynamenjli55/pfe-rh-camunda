import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-attestation-salaire',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attestation-salaire.component.html',
  styleUrls: ['./attestation-salaire.component.css']
})
export class AttestationSalaireComponent {
  
  attestation = {
    reference: 'SAL/2024/00456',
    dateEmission: '13/02/2026',
    lieu: 'Tunis',
    banque: {
      nom: 'BANQUE INTERNATIONALE',
      direction: 'Direction des Ressources Humaines',
      adresse: 'Avenue Habib Bourguiba, Tunis 1000',
      telephone: '+216 71 123 456',
      email: 'rh@banque-internationale.tn',
      website: 'www.banque-internationale.tn'
    },
    employe: {
      nomPrenom: 'souissi zaineb',
      matricule: 'EMP-2024-001',
      cin: '12345678',
      fonction: 'Responsable Informatique',
      departement: 'IT & Développement',
      dateEmbauche: '15/01/2020'
    },
    remuneration: {
      salaireBase: 2800.000,
      primeAnciennete: 350.000,
      primeFonction: 500.000,
      indemniteTransport: 150.000,
      salaireBrut: 3800.000,
      retenueCNSS: 348.840,
      tauxCNSS: 9.18,
      salaireNet: 3451.160
    }
  };

  constructor(private router: Router) {}

  retournerDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  imprimerAttestation(): void {
    window.print();
  }

  telechargerPDF(): void {
    alert(`📥 Téléchargement de l'attestation\nRéf: ${this.attestation.reference}`);
  }

  envoyerEmail(): void {
    window.location.href = `mailto:${this.attestation.banque.email}?subject=Attestation de salaire - ${this.attestation.reference}`;
  }

  formatMontant(montant: number): string {
    return montant.toFixed(3).replace('.', ',');
  }
}