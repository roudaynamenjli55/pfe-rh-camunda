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

export interface ModuleCard {
  iconBg: string;
  icon: string;
  title: string;
  desc: string;
  count: string;
}

export interface User {
  initials: string;
  avatarBg: string;
  avatarColor: string;
  name: string;
  email: string;
  role: 'Administrateur' | 'RH' | 'Chef Hiérarchique' | 'Employé';
  roleBadge: string;
  agence: string;
  active: boolean;
  lastLogin: string;
}

export interface LogEntry {
  icon: string;
  user: string;
  message: string;
  time: string;
}

export interface Session {
  initials: string;
  avatarBg: string;
  avatarColor: string;
  name: string;
  ip: string;
}

export interface ProgressBar {
  label: string;
  value: string;
  percent: number;
  colorClass: string;
}

export interface NewUserForm {
  prenom: string;
  nom: string;
  email: string;
  matricule: string;
  agence: string;
  departement: string;
  role: string;
  password: string;
  confirmPassword: string;
}

// ─── Component ─────────────────────────────────────────────────────────────

@Component({
  selector: 'app-administrateur',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './administrateur.component.html',
  styleUrls: ['./administrateur.component.css'],
})
export class AdministrateurComponent {

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
        { icon: '⚙️', label: "Vue d'ensemble", active: true },
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
        { icon: '🎯', label: 'Campagnes' },
        { icon: '📊', label: 'Résultats' },
        { icon: '📝', label: 'Formulaires' },
        { icon: '📤', label: 'Exports' },
      ],
    },
  ];

  activeNavItem = "Vue d'ensemble";

  setActive(item: NavItem): void {
    this.activeNavItem = item.label;
    this.navSections.forEach(sec =>
      sec.items.forEach(i => (i.active = i.label === item.label))
    );
  }

  // ── Stats ────────────────────────────────────────────────────────────────

  stats: StatCard[] = [
    { stripeColor: 'var(--gold)', icon: '👥', label: 'Utilisateurs actifs',  value: 143, sub: '2 comptes désactivés' },
    { stripeColor: 'var(--teal)', icon: '🏦', label: 'Agences',               value: 12,  sub: 'sur 3 régions' },
    { stripeColor: 'var(--green)',icon: '🏢', label: 'Départements',          value: 8,   sub: 'actifs' },
    { stripeColor: 'var(--amber)',icon: '📋', label: "Actions aujourd'hui",   value: 247, sub: "journaux d'audit" },
  ];

  // ── Module cards ─────────────────────────────────────────────────────────

  modules: ModuleCard[] = [
    { iconBg: '#111111',          icon: '👤', title: 'Gestion des utilisateurs',  desc: 'Créer, modifier, désactiver les comptes et gérer les accès.',       count: '143 actifs · 2 désactivés' },
    { iconBg: 'var(--gold-pale)', icon: '🔐', title: 'Rôles & Permissions',       desc: 'Configurer les droits d\'accès par rôle et par module.',            count: '4 rôles · 38 permissions' },
    { iconBg: 'var(--teal-lt)',   icon: '🏦', title: 'Gestion des agences',       desc: 'Créer et gérer les agences et leurs rattachements organisationnels.',count: '12 agences · 3 régions' },
    { iconBg: 'var(--green-lt)',  icon: '🏢', title: 'Départements & Postes',     desc: 'Structurer l\'organigramme, les départements et les postes.',        count: '8 dépts · 24 postes' },
    { iconBg: 'var(--purple-lt)', icon: '📋', title: "Journaux d'audit",          desc: 'Traçabilité complète des actions utilisateurs sur le système.',      count: '247 actions aujourd\'hui' },
    { iconBg: 'var(--amber-lt)',  icon: '⚙️', title: 'Paramètres système',        desc: 'Configuration générale, sécurité, notifications et intégrations.',  count: 'Dernière modif. aujourd\'hui' },
  ];

  // ── Users table ──────────────────────────────────────────────────────────

  searchQuery = '';
  filterRole = '';
  filterStatus = '';

  users: User[] = [
    { initials: 'AD', avatarBg: 'var(--red-lt)',   avatarColor: 'var(--red)',    name: 'Admin Système',    email: 'admin@bankcorp.tn',      role: 'Administrateur',    roleBadge: 'b-admin', agence: 'Siège Social',  active: true,  lastLogin: "Aujourd'hui 09:14" },
    { initials: 'SH', avatarBg: '#eff6ff',         avatarColor: '#1d4ed8',      name: 'Sara Haddad',      email: 's.haddad@bankcorp.tn',   role: 'RH',                roleBadge: 'b-rh',    agence: 'Tunis Centre',  active: true,  lastLogin: "Aujourd'hui 08:32" },
    { initials: 'MB', avatarBg: 'var(--green-lt)', avatarColor: 'var(--green)', name: 'Mohamed Ben Ali',  email: 'm.benali@bankcorp.tn',   role: 'Chef Hiérarchique', roleBadge: 'b-chef',  agence: 'Sfax Médina',   active: true,  lastLogin: 'Hier 16:45' },
    { initials: 'AM', avatarBg: 'var(--amber-lt)', avatarColor: 'var(--amber)', name: 'Ahmed Mansour',    email: 'a.mansour@bankcorp.tn',  role: 'Employé',           roleBadge: 'b-emp',   agence: 'Tunis Nord',    active: true,  lastLogin: 'Hier 14:20' },
    { initials: 'NZ', avatarBg: 'var(--slate)',    avatarColor: 'var(--text-mute)', name: 'Nadia Zouari', email: 'n.zouari@bankcorp.tn',   role: 'Employé',           roleBadge: 'b-emp',   agence: 'Sousse Centre', active: true,  lastLogin: '22/04/2025' },
    { initials: 'OT', avatarBg: 'var(--slate)',    avatarColor: 'var(--text-mute)', name: 'Omar Trabelsi', email: 'o.trabelsi@bankcorp.tn', role: 'Employé',          roleBadge: 'b-emp',   agence: 'Monastir',      active: false, lastLogin: '12/01/2025' },
  ];

  get filteredUsers(): User[] {
    return this.users.filter(u => {
      const matchSearch =
        !this.searchQuery ||
        u.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        u.email.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchRole   = !this.filterRole   || u.role === this.filterRole;
      const matchStatus = !this.filterStatus ||
        (this.filterStatus === 'Actif'      &&  u.active) ||
        (this.filterStatus === 'Désactivé'  && !u.active);
      return matchSearch && matchRole && matchStatus;
    });
  }

  toggleUser(user: User): void {
    user.active = !user.active;
  }

  // ── Audit log ────────────────────────────────────────────────────────────

  auditLog: LogEntry[] = [
    { icon: '🔑', user: 'Sara Haddad', message: "s'est connectée au système",                           time: "Aujourd'hui · 08:32:14" },
    { icon: '✅', user: 'M. Ben Ali',  message: 'a approuvé la demande de congé EMP-0042',             time: "Aujourd'hui · 09:05:47" },
    { icon: '👤', user: 'Admin',       message: 'a créé l\'utilisateur <strong>Nadia Zouari</strong>', time: 'Hier · 15:22:09' },
    { icon: '🎯', user: 'Sara Haddad', message: 'a lancé la campagne <strong>S1 2025</strong>',        time: 'Hier · 10:00:00' },
    { icon: '🔒', user: 'Admin',       message: 'a désactivé <strong>Omar Trabelsi</strong>',          time: '12/01/2025 · 08:14' },
  ];

  // ── Active sessions ──────────────────────────────────────────────────────

  sessions: Session[] = [
    { initials: 'AD', avatarBg: 'var(--red-lt)',   avatarColor: 'var(--red)',    name: 'Admin Système', ip: '192.168.1.10' },
    { initials: 'SH', avatarBg: '#eff6ff',         avatarColor: '#1d4ed8',      name: 'Sara Haddad',   ip: '192.168.1.45' },
    { initials: 'MB', avatarBg: 'var(--green-lt)', avatarColor: 'var(--green)', name: 'M. Ben Ali',    ip: '192.168.2.18' },
  ];

  disconnectSession(session: Session): void {
    this.sessions = this.sessions.filter(s => s !== session);
  }

  // ── Progress bars ────────────────────────────────────────────────────────

  progressBars: ProgressBar[] = [
    { label: 'Connexions (30 j.)', value: '1 847', percent: 74, colorClass: '' },
    { label: 'Demandes traitées',  value: '342',   percent: 85, colorClass: 'teal' },
    { label: "Taux d'adoption",    value: '96%',   percent: 96, colorClass: 'green' },
  ];

  // ── Add user form ────────────────────────────────────────────────────────

  agences   = ['Siège Social', 'Tunis Centre', 'Tunis Nord', 'Sfax Médina', 'Sousse Centre'];
  departements = ['Crédits', 'Commerce', 'Informatique', 'Opérations', 'Compliance', 'Audit'];
  roles = ['Employé', 'Chef Hiérarchique', 'Ressources Humaines', 'Administrateur'];

  newUser: NewUserForm = {
    prenom: '', nom: '', email: '', matricule: '',
    agence: '', departement: '', role: '',
    password: '', confirmPassword: '',
  };

  submitForm(): void {
    if (!this.newUser.prenom || !this.newUser.nom || !this.newUser.email) {
      alert('Veuillez remplir tous les champs obligatoires.');
      return;
    }
    if (this.newUser.password !== this.newUser.confirmPassword) {
      alert('Les mots de passe ne correspondent pas.');
      return;
    }
    console.log('Nouvel utilisateur :', this.newUser);
    this.resetForm();
  }

  resetForm(): void {
    this.newUser = {
      prenom: '', nom: '', email: '', matricule: '',
      agence: '', departement: '', role: '',
      password: '', confirmPassword: '',
    };
  }

  // ── Pagination ───────────────────────────────────────────────────────────

  currentPage = 1;
  totalPages  = 25;

  get pageNumbers(): (number | '…')[] {
    return [1, 2, 3, '…', this.totalPages];
  }

  goToPage(p: number | '…'): void {
    if (typeof p === 'number') this.currentPage = p;
  }
}