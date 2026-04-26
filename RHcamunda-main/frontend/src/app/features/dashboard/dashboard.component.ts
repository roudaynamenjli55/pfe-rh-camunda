import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

// ── Interfaces (Optionnel mais recommandé pour la maintenance) ──
export interface NavItem {
  label: string;
  route: string;
  icon: SafeHtml;
  badge?: number;
}
export interface StatCard {
  value: number;
  label: string;
  iconColor: string;
  icon: SafeHtml;
  trend: { direction: 'up' | 'down'; value: string };
}
export interface PendingRequest {
  initials: string;
  name: string;
  type: string;
  duration: string;
  status: string;
}
export interface Activity {
  type: string;
  icon: SafeHtml;
  label: string;
  description: string;
  time: string;
}

// ── Component ─────────────────────────────────────────────────────────────
@Component({
  selector: 'app-dashboard',
  standalone: true,
  // ✅ CRUCIAL : Laisse ce tableau VIDE. Ta nouvelle syntaxe HTML ne nécessite AUCUN import.
  imports: [], 
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  constructor(private sanitizer: DomSanitizer) {}

  // Helper pour sécuriser les SVG
  private svg(path: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(
      `<svg viewBox="0 0 24 24" fill="currentColor" width="24" height="24">${path}</svg>`
    );
  }

  // ── State & Properties (Exactement ce que ton HTML utilise) ──
  activeRoute = 'dashboard';
  currentUser = { initials: 'AM', name: 'Ahmed Mejri', role: 'Administrateur RH' };
  searchQuery = '';
  notificationCount = 5;
  messageCount = 12;
  chartFilters = ['Semaine', 'Mois', 'Année'];
  activeChartFilter = 'Semaine';

  mainMenuItems: NavItem[] = [];
  workflowMenuItems: NavItem[] = [];
  reportsMenuItems: NavItem[] = [];
  statsCards: StatCard[] = [];
  recentActivities: Activity[] = [];

  pendingRequests: PendingRequest[] = [
    { initials: 'SM', name: 'Sarah Mansour',  type: 'Congé annuel',       duration: '5 jours', status: 'pending' },
    { initials: 'KT', name: 'Karim Trabelsi', type: 'Autorisation',       duration: '2h',      status: 'urgent'  },
    { initials: 'LB', name: 'Leila Ben Ali',  type: 'Congé maladie',      duration: '3 jours', status: 'pending' },
    { initials: 'MH', name: 'Mohamed Hamdi',  type: 'Congé exceptionnel', duration: '1 jour',  status: 'pending' }
  ];

  ngOnInit(): void {
    this.buildData();
  }

  private buildData(): void {
    // Navigation
    this.mainMenuItems = [
      { label: 'Dashboard',    route: 'dashboard',    icon: this.svg('<path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>') },
      { label: 'Employés',     route: 'employes',     icon: this.svg('<path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>') },
      { label: 'Départements', route: 'departements', icon: this.svg('<path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>') }
    ];
    this.workflowMenuItems = [
      { label: 'Demandes de congés', route: 'conges',        badge: 8, icon: this.svg('<path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zM9 14H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2zm-8 4H7v-2h2v2zm4 0h-2v-2h2v2zm4 0h-2v-2h2v2z"/>') },
      { label: 'Autorisations',      route: 'autorisations', badge: 3, icon: this.svg('<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>') },
      { label: 'Justificatifs',      route: 'justificatifs',           icon: this.svg('<path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>') }
    ];
    this.reportsMenuItems = [
      { label: 'Statistiques', route: 'statistiques', icon: this.svg('<path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>') },
      { label: 'Rapports',     route: 'rapports',     icon: this.svg('<path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>') }
    ];

    // Stats
    this.statsCards = [
      { value: 347, label: 'Employés Actifs',      iconColor: 'orange', trend: { direction: 'up',   value: '+12%' }, icon: this.svg('<path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>') },
      { value: 23,  label: 'Demandes en attente',  iconColor: 'yellow', trend: { direction: 'up',   value: '+8%'  }, icon: this.svg('<path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10z"/>') },
      { value: 156, label: 'Validations ce mois',  iconColor: 'dark',   trend: { direction: 'up',   value: '+15%' }, icon: this.svg('<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>') },
      { value: 4,   label: 'Demandes urgentes',    iconColor: 'orange', trend: { direction: 'down', value: '-3%'  }, icon: this.svg('<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>') }
    ];

    // Activités
    this.recentActivities = [
      { type: 'approved', icon: this.svg('<path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>'), label: 'Demande approuvée', description: 'Congé annuel de Fatma Khalil (5 jours)', time: 'Il y a 15 minutes' },
      { type: 'pending',  icon: this.svg('<path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>'), label: 'Nouvelle demande',  description: 'Autorisation de sortie de Youssef Gharbi (3h)', time: 'Il y a 1 heure' },
      { type: 'rejected', icon: this.svg('<path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>'), label: 'Demande refusée',   description: 'Congé sans solde de Sami Bouaziz (Solde insuffisant)', time: 'Il y a 2 heures' },
      { type: 'approved', icon: this.svg('<path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>'), label: 'Justificatif validé', description: 'Certificat médical de Nadia Amri', time: 'Il y a 3 heures' }
    ];
  }

  // ── Methods (Exactement ce que ton HTML appelle) ──
  navigateTo(route: string): void {
    this.activeRoute = route;
    console.log('Navigation vers:', route);
  }

  onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
  }

  setChartFilter(filter: string): void {
    this.activeChartFilter = filter;
  }

  openNotifications(): void {}
  openMessages(): void {}
}