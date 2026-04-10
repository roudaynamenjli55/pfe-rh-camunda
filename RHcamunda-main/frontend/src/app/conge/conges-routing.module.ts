import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TitreCongeComponent } from './titre-conge/titre-conge.component';
import { ListeCongesComponent } from './liste-conges/liste-conges.component';

const routes: Routes = [
  // ✅ Route par défaut du module conges
  { path: '', redirectTo: 'mes-conges', pathMatch: 'full' },
  
  // ✅ Routes spécifiques
  { path: 'mes-conges', component: ListeCongesComponent },
  { path: 'titre-conge', component: TitreCongeComponent },
  
  // ❌ PAS de wildcard (**) ici pour éviter les boucles!
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CongesRoutingModule { }