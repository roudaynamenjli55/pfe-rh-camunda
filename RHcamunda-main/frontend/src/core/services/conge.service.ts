import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Conge, StatsConges } from '../../shared/models/conge.model';

@Injectable({
  providedIn: 'root'
})
export class CongeService {

  getMockDemandes(): Conge[] {
    return [
      {
        id: 1,
        type: 'Congé Annuel',
        dateDemande: '2024-02-10',
        dateDebut: '2024-03-20',
        dateFin: '2024-03-25',
        duree: 4,
        motif: 'Vacances familiales',
        statut: 'En attente',
        justificatif: false,
        progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
        etapeActuelle: 1
      },
      {
        id: 2,
        type: 'Congé Annuel',
        dateDemande: '2024-01-05',
        dateDebut: '2024-02-15',
        dateFin: '2024-02-20',
        duree: 4,
        motif: 'Repos',
        statut: 'Approuvé',
        validePar: 'Mme KHALIL Sarah',
        justificatif: false,
        progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
        etapeActuelle: 3
      },
      {
        id: 3,
        type: 'Congé Maladie',
        dateDemande: '2024-02-12',
        dateDebut: '2024-02-12',
        dateFin: '2024-02-14',
        duree: 3,
        motif: 'Maladie',
        statut: 'En attente',
        justificatif: true,
        progression: ['Soumise', 'Chef hiér.', 'Service RH', 'Finalisée'],
        etapeActuelle: 2
      }
    ];
  }

  getStats(): Observable<StatsConges> {
    return of({
      joursRestants: 22,
      demandesAnnee: 5,
      enAttente: 2,
      joursPris: 18
    }).pipe(delay(300));
  }

  saveDemande(demande: Conge): Observable<Conge> {
    console.log('💾 Sauvegarde:', demande);
    return of(demande).pipe(delay(500));
  }
}