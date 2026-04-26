export interface Conge {
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

export interface StatsConges {
  joursRestants: number;
  demandesAnnee: number;
  enAttente: number;
  joursPris: number;
}

export interface FiltresConges {
  typeConge: string;
  statut: string;
  periode: string;
}