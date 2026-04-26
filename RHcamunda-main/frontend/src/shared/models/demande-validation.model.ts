// src/app/validation/shared/models/demande-validation.model.ts
export interface DemandeValidation {
  id: number;
  employe: {
    initiales: string;
    nomComplet: string;
    fonction: string;
    matricule: string;
    email?: string;
  };
  statut: 'urgent' | 'en-attente' | 'approuve' | 'refuse';
  type: 'Congé Annuel' | 'Congé Maladie' | 'Autorisation de Sortie' | 'RTT';
  dateDebut: Date;
  dateFin: Date;
  duree: number;
  dateSoumission: Date;
  motif: string;
  justificatif?: boolean;
  soldeDisponible?: number;
  horaire?: string; // Pour autorisations
}

export interface StatsValidation {
  enAttente: number;
  approuvees: number;
  refusees: number;
  tempsTraitementMoyen: string;
}