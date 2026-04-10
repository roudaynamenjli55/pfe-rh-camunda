import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { TitreCongeComponent } from './titre-conge/titre-conge.component';

const routes: Routes = [
  { path: 'titre-conge', component: TitreCongeComponent }
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    TitreCongeComponent
  ]
})
export class CongesModule { }