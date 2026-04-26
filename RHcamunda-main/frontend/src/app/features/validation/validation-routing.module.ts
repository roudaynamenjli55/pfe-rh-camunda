import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
// Importe ton composant de validation ici si tu en as un
// import { ListeValidationsComponent } from './liste-validations/liste-validations.component';

const routes: Routes = [
  // { path: '', component: ListeValidationsComponent }, // Décommente si tu as un composant
  { path: '', loadChildren: () => import('./liste-validations/liste-validations.component').then(m => m.ListeValidationsComponent) }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ValidationRoutingModule { }