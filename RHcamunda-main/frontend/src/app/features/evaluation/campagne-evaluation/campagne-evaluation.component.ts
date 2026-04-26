import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

// ─── Interfaces ────────────────────────────────────────────────────────────

export interface NavItem {
  icon: string;
  label: string;
  badge?: number;
  active?: boolean;
}

export interface NavSection {
  label: string;
  items: NavItem[];
}

export interface StatCard {
  stripeColor: string;
  icon: string;
  label: string;
  value: string | number;
  sub: string;
}

export type CampagneStatus = 'active' | 'draft' | 'closed' | 'planned';

export interface CampagneNum {
  val: string | number;
  lbl: string;
}

export interface Campagne {
  id: number;
  title: string;
  period: string;
  status: CampagneStatus;
  badgeClass: string;
  badgeLabel: string;
  progressPct: number;
  progressColor: string;
  nums: CampagneNum[];
  isActive?: boolean;
  isClosed?: boolean;
}

export type EvaluationStatus = 'validated' | 'pending' | 'auto' | 'waiting' | 'not-started';

export interface EvaluationRow {
  initials: string;
  avatarBg: string;
  avatarColor: string;
  name: string;
  id: string;
  departement: string;
  autoScore: string | null;
  managerScore: string | null;
  managerClass: string;
  finalScore: string | null;
  status: EvaluationStatus;
  badgeClass: string;
  badgeLabel: string;
}

export interface ScoreDistItem {
  label: string;
  value: number;
  color: string;
  sub: string;
}

export interface MiniBar {
  heightPct: number;
  color: string;
  opacity: number;
}

export interface QuickAction {
  icon: string;
  label: string;
  danger?: boolean;
}

export interface NewCampagneForm {
  titre: string;
  annee: string;
  dateDebut: string;
  dateFin: string;
  statut: string;
  type: string;
  description: string;
}

// ─── Component ─────────────────────────────────────────────────────────────

@Component({
  selector: 'app-campagne-evaluation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './campagne-evaluation.component.html',
  styleUrls: ['./campagne-evaluation.component.css']  
})
export class CampagneEvaluationComponent {

  // ── Sidebar navigation ──────────────────────────────────────────────────

  navSections: NavSection[] = [
    {
      label: 'Principal',
      items: [
        { icon: '🏖️', label: 'Mes Congés', badge: 2 },
        { icon: '🔓', label: 'Autorisations' },
        { icon: '📄', label: 'Attestations' },
      ],
    },
    {
      label: 'Suivi',
      items: [
        { icon: '📋', label: 'Historique' },
        { icon: '🔔', label: 'Notifications', badge: 3 },
      ],
    },
    {
      label: 'Administrateur',
      items: [
        { icon: '⚙️', label: "Vue d'ensemble" },
        { icon: '👤', label: 'Utilisateurs', badge: 2 },
        { icon: '🔐', label: 'Rôles & Permissions' },
        { icon: '🏦', label: 'Agences' },
        { icon: '🏢', label: 'Départements' },
        { icon: '📋', label: "Journaux d'audit" },
        { icon: '🔑', label: 'Sessions actives' },
      ],
    },
    {
      label: 'Campagne Évaluation',
      items: [
        { icon: '🎯', label: 'Campagnes', active: true },
        { icon: '📊', label: 'Résultats' },
        { icon: '📝', label: 'Formulaires' },
        { icon: '📤', label: 'Exports' },
      ],
    },
  ];

  activeNavItem = 'Campagnes';

  setActive(item: NavItem): void {
    this.activeNavItem = item.label;
    this.navSections.forEach(sec =>
      sec.items.forEach(i => (i.active = i.label === item.label))
    );
  }

  // ── Stats ────────────────────────────────────────────────────────────────

  stats: StatCard[] = [
    { stripeColor: 'var(--gold)',  icon: '🎯', label: 'Campagnes actives',      value: 2,   sub: 'sur 5 campagnes totales' },
    { stripeColor: 'var(--teal)',  icon: '👤', label: 'Évaluations en cours',   value: 127, sub: '+14 cette semaine' },
    { stripeColor: 'var(--green)', icon: '✅', label: 'Évaluations validées',   value: 89,  sub: '70 % de complétion' },
    { stripeColor: 'var(--amber)', icon: '⏳', label: 'En attente validation',  value: 38,  sub: 'Délai : 15 jours' },
  ];

  // ── Campagnes ────────────────────────────────────────────────────────────

  campagnes: Campagne[] = [
    {
      id: 1,
      title: 'Évaluation Annuelle 2025',
      period: '01/01/2025 → 31/12/2025',
      status: 'active',
      badgeClass: 'b-active',
      badgeLabel: '● Actif',
      progressPct: 70,
      progressColor: '',
      isActive: true,
      nums: [
        { val: 89,  lbl: 'Validées' },
        { val: 38,  lbl: 'En cours' },
        { val: 127, lbl: 'Total' },
      ],
    },
    {
      id: 2,
      title: 'Évaluation S1 2025',
      period: '01/01/2025 → 30/06/2025',
      status: 'active',
      badgeClass: 'b-active',
      badgeLabel: '● Actif',
      progressPct: 92,
      progressColor: 'green',
      isActive: true,
      nums: [
        { val: 112, lbl: 'Validées' },
        { val: 10,  lbl: 'En attente' },
        { val: 122, lbl: 'Employés' },
      ],
    },
    {
      id: 3,
      title: 'Évaluation S2 2025',
      period: '01/07/2025 → 31/12/2025',
      status: 'draft',
      badgeClass: 'b-draft',
      badgeLabel: '◌ Brouillon',
      progressPct: 0,
      progressColor: 'teal',
      nums: [
        { val: 0,  lbl: 'Validées' },
        { val: 0,  lbl: 'En attente' },
        { val: 62, lbl: 'Employés' },
      ],
    },
    {
      id: 4,
      title: 'Évaluation Annuelle 2024',
      period: '01/01/2024 → 31/12/2024',
      status: 'closed',
      badgeClass: 'b-closed',
      badgeLabel: '✓ Clôturé',
      progressPct: 100,
      progressColor: 'green',
      isClosed: true,
      nums: [
        { val: 118, lbl: 'Validées' },
        { val: 2,   lbl: 'Non faites' },
        { val: 120, lbl: 'Total' },
      ],
    },
    {
      id: 5,
      title: 'Évaluation Annuelle 2026',
      period: '01/01/2026 → 31/12/2026',
      status: 'planned',
      badgeClass: 'b-planned',
      badgeLabel: '◌ Planifiée',
      progressPct: 0,
      progressColor: '',
      nums: [
        { val: '—', lbl: 'Démarrage' },
        { val: 65,  lbl: 'Employés' },
      ],
    },
  ];

  // ── Evaluation table ─────────────────────────────────────────────────────

  searchQuery = '';

  evaluations: EvaluationRow[] = [
    {
      initials: 'AM', avatarBg: '#dbeafe', avatarColor: '#1d4ed8',
      name: 'Ahmed Mansour', id: 'EMP-0042', departement: 'Crédits',
      autoScore: '4.2', managerScore: '4.5', managerClass: 'hi',
      finalScore: '4.4', status: 'validated',
      badgeClass: 'b-active', badgeLabel: '✓ Validé',
    },
    {
      initials: 'LB', avatarBg: '#fce7f3', avatarColor: '#9d174d',
      name: 'Leila Benhamed', id: 'EMP-0017', departement: 'Commerce',
      autoScore: '3.8', managerScore: '3.5', managerClass: 'med',
      finalScore: '3.6', status: 'pending',
      badgeClass: 'b-pending', badgeLabel: '⏳ Manager',
    },
    {
      initials: 'KT', avatarBg: '#d1fae5', avatarColor: '#065f46',
      name: 'Karim Tlili', id: 'EMP-0063', departement: 'Informatique',
      autoScore: '4.7', managerScore: null, managerClass: 'hi',
      finalScore: null, status: 'auto',
      badgeClass: 'b-draft', badgeLabel: '◌ Auto-éval.',
    },
    {
      initials: 'NZ', avatarBg: '#fef9c3', avatarColor: '#854d0e',
      name: 'Nadia Zouari', id: 'EMP-0088', departement: 'Opérations',
      autoScore: '4.0', managerScore: '4.2', managerClass: 'hi',
      finalScore: '4.1', status: 'validated',
      badgeClass: 'b-active', badgeLabel: '✓ Validé',
    },
    {
      initials: 'YC', avatarBg: '#f3e8ff', avatarColor: '#6b21a8',
      name: 'Yassine Cherif', id: 'EMP-0051', departement: 'Compliance',
      autoScore: null, managerScore: null, managerClass: '',
      finalScore: null, status: 'not-started',
      badgeClass: 'b-closed', badgeLabel: '○ Non démarrée',
    },
    {
      initials: 'HB', avatarBg: '#fee2e2', avatarColor: '#991b1b',
      name: 'Hanen Boughanmi', id: 'EMP-0033', departement: 'Audit',
      autoScore: '3.5', managerScore: '3.8', managerClass: 'med',
      finalScore: '3.7', status: 'waiting',
      badgeClass: 'b-waiting', badgeLabel: '⏳ Validation',
    },
  ];

  get filteredEvaluations(): EvaluationRow[] {
    if (!this.searchQuery) return this.evaluations;
    const q = this.searchQuery.toLowerCase();
    return this.evaluations.filter(e =>
      e.name.toLowerCase().includes(q) ||
      e.id.toLowerCase().includes(q) ||
      e.departement.toLowerCase().includes(q)
    );
  }

  // ── Score distribution ───────────────────────────────────────────────────

  scoreDistItems: ScoreDistItem[] = [
    { label: 'Excellent >4.5', value: 31, color: 'var(--green)', sub: '24 % des évalués' },
    { label: 'Bon 3.5–4.5',    value: 48, color: 'var(--gold)',  sub: '38 % des évalués' },
    { label: 'Moyen 2.5–3.5',  value: 10, color: 'var(--amber)', sub: '8 % des évalués'  },
    { label: 'À améliorer <2.5', value: 0, color: 'var(--red)',  sub: '0 %'              },
  ];

  miniBars: MiniBar[] = [
    { heightPct: 30,  color: 'var(--gold)',  opacity: 0.5 },
    { heightPct: 50,  color: 'var(--gold)',  opacity: 0.6 },
    { heightPct: 70,  color: 'var(--gold)',  opacity: 0.7 },
    { heightPct: 100, color: 'var(--green)', opacity: 1   },
    { heightPct: 85,  color: 'var(--green)', opacity: 1   },
    { heightPct: 60,  color: 'var(--gold)',  opacity: 1   },
    { heightPct: 45,  color: 'var(--teal)',  opacity: 1   },
    { heightPct: 35,  color: 'var(--teal)',  opacity: 1   },
  ];

  overallScore = 4.1;
  overallValidated = 89;
  overallFillPct = 82;

  // ── Quick actions ────────────────────────────────────────────────────────

  quickActions: QuickAction[] = [
    { icon: '📧', label: 'Relancer les managers (38)' },
    { icon: '📊', label: 'Exporter toutes les fiches PDF' },
    { icon: '📈', label: 'Rapport statistique global' },
    { icon: '🔒', label: 'Clôturer la campagne', danger: true },
  ];

  handleQuickAction(action: QuickAction): void {
    console.log('Action:', action.label);
  }

  // ── Pagination ───────────────────────────────────────────────────────────

  currentPage = 1;
  totalPages  = 22;

  get pageNumbers(): (number | '…')[] {
    return [1, 2, 3, '…', this.totalPages];
  }

  goToPage(p: number | '…'): void {
    if (typeof p === 'number') this.currentPage = p;
  }

  // ── New campagne form ────────────────────────────────────────────────────

  annees    = ['2026', '2025', '2024'];
  statuts   = ['Brouillon', 'Planifiée', 'Active'];
  typesList = ['Annuelle', 'Semestrielle', 'Trimestrielle'];

  newCampagne: NewCampagneForm = {
    titre: '',
    annee: '2026',
    dateDebut: '2026-01-01',
    dateFin: '2026-12-31',
    statut: 'Brouillon',
    type: 'Annuelle',
    description: '',
  };

  saveDraft(): void {
    console.log('Brouillon sauvegardé :', this.newCampagne);
  }

  submitCampagne(): void {
    if (!this.newCampagne.titre) {
      alert('Le titre est obligatoire.');
      return;
    }
    console.log('Campagne créée :', this.newCampagne);
    this.resetForm();
  }

  resetForm(): void {
    this.newCampagne = {
      titre: '', annee: '2026',
      dateDebut: '2026-01-01', dateFin: '2026-12-31',
      statut: 'Brouillon', type: 'Annuelle', description: '',
    };
  }
}