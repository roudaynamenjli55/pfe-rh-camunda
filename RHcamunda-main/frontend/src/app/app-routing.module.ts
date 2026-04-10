import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule) },
  
  // ✅ Route pour le module Congés
  { 
    path: 'conges', 
    loadChildren: () => import('./conge/conges.module').then(m => m.CongesModule) 
  },
  
  // ✅ Redirection par défaut vers login (PAS vers conges!)
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  
  // ✅ Wildcard pour les routes inconnues → login
  { path: '**', redirectTo: '/auth/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }