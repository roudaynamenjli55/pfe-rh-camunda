import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const routes: Routes = [
  // Redirect par défaut
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },

  // Auth (Module)
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then(m => m.AuthModule)
  },

  // Dashboard (Standalone)
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },

  // Congés (Module) - Une seule fois ✅
  {
    path: 'conges',
    loadChildren: () => import('./features/conge/conges.module').then(m => m.CongesModule)
  },

  // Profil (Standalone)
  {
    path: 'profil-employe',
    loadComponent: () => import('./features/profil-employe/profil-employe.component').then(m => m.ProfilEmployeComponent)
  },

  // Attestations (Standalone)
  {
    path: 'attestation-salaire',
    loadComponent: () => import('./features/attestation-salaire/attestation-salaire.component').then(m => m.AttestationSalaireComponent)
  },
  {
    path: 'attestation-travail',
    loadComponent: () => import('./features/attestation-travail/attestation-travail.component').then(m => m.AttestationTravailComponent)
  },

  // Validation (Module)
  {
    path: 'validation',
    loadChildren: () => import('./features/validation/validation.module').then(m => m.ValidationModule)
  },

  // Évaluations (Standalone)
  {
    path: 'evaluation/fiche-annuelle',
    loadComponent: () => import('./features/evaluation/fiche-evaluation/fiche-evaluation.component').then(m => m.FicheEvaluationComponent)
  },
  {
    path: 'evaluation/campagne',
    loadComponent: () => import('./features/evaluation/campagne-evaluation/campagne-evaluation.component').then(m => m.CampagneEvaluationComponent) // ✅ Vérifie pas d'espace !
  },

  // Administrateur (Standalone)
  {
    path: 'administrateur',
    loadComponent: () => import('./features/administrateur/administrateur.component').then(m => m.AdministrateurComponent)
  },

  // Redirects
  { path: 'conges/historique', redirectTo: '/conges/mes-conges', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

