import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nouvelle-demande',
  templateUrl: './nouvelle-demande.component.html',
  styleUrl: './nouvelle-demande.component.css', // ✅ Angular 15+ : 'styleUrl' au singulier
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class NouvelleDemandeComponent implements OnInit {
  congeForm!: FormGroup;
  soldeDisponible: number = 22;
  
  joursOuvrables: number = 0;
  joursCalendaires: number = 0;
  fileName: string = '';

  typesConge = ['Congé Annuel', 'Congé Maladie', 'Congé Sans Solde', 'RTT', 'Congé Exceptionnel'];
  motifs = ['Vacances', 'Fatigue', 'Raison Personnelle', 'Maladie', 'Famille', 'Autre'];

  constructor(private fb: FormBuilder, private router: Router) {}

  ngOnInit(): void {
    this.initForm();
    // Écoute les changements de dates pour recalculer automatiquement
    this.congeForm.get('dateDebut')?.valueChanges.subscribe(() => this.calculerJours());
    this.congeForm.get('dateFin')?.valueChanges.subscribe(() => this.calculerJours());
  }

  private initForm(): void {
    this.congeForm = this.fb.group({
      typeConge: ['', Validators.required],
      motif: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      commentaire: [''],
      adresseConge: [''],
      telephone: [''],
      certifie: [false, Validators.requiredTrue]
    });
  }

  // ✅ Helper pour accéder facilement aux controls dans le HTML
  get f() { return this.congeForm.controls; }

  calculerJours(): void {
    const start = this.f['dateDebut'].value;
    const end = this.f['dateFin'].value;

    if (!start || !end) {
      this.joursOuvrables = 0;
      this.joursCalendaires = 0;
      return;
    }

    // ✅ Ajout de 'T00:00:00' pour éviter les décalages de fuseau horaire
    const dateDebut = new Date(start + 'T00:00:00');
    const dateFin = new Date(end + 'T00:00:00');

    if (dateDebut > dateFin) {
      this.joursOuvrables = 0;
      this.joursCalendaires = 0;
      return;
    }

    let joursTotal = 0;
    let joursOuv = 0;
    const current = new Date(dateDebut);

    while (current <= dateFin) {
      joursTotal++;
      const day = current.getDay(); // 0 = Dimanche, 6 = Samedi
      if (day !== 0 && day !== 6) joursOuv++;
      current.setDate(current.getDate() + 1);
    }

    this.joursCalendaires = joursTotal;
    this.joursOuvrables = joursOuv;
  }

  formatDateLong(date: string): string {
    if (!date) return '--';
    const d = new Date(date + 'T00:00:00');
    const months = ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];
    return `${d.getDate()} ${months[d.getMonth()]} ${d.getFullYear()}`;
  }

  onFileChange(event: any): void {
    const file = event.target.files?.[0];
    if (!file) return;

    const maxSize = 5 * 1024 * 1024; // 5 Mo
    if (file.size > maxSize) {
      alert('⚠️ Le fichier ne doit pas dépasser 5 Mo');
      return;
    }
    
    const validTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'];
    if (!validTypes.includes(file.type)) {
      alert('⚠️ Seuls les fichiers PDF, JPG et PNG sont acceptés');
      return;
    }
    
    this.fileName = file.name;
  }

  onSubmit(): void {
    if (this.congeForm.invalid) {
      this.congeForm.markAllAsTouched();
      alert('⚠️ Veuillez remplir tous les champs obligatoires et accepter la certification.');
      return;
    }

    const formData = {
      ...this.congeForm.value,
      joursOuvrables: this.joursOuvrables,
      joursCalendaires: this.joursCalendaires,
      dateSoumission: new Date().toISOString()
    };

    console.log('✅ Demande prête pour le backend :', formData);
    alert(`✔ Demande soumise avec succès !\n📅 Jours ouvrables : ${this.joursOuvrables}\n📆 Total jours : ${this.joursCalendaires}`);
    this.router.navigate(['/conges/mes-conges']);
  }

  onCancel(): void {
    if (confirm('Êtes-vous sûr de vouloir annuler cette demande ?')) {
      this.router.navigate(['/conges/mes-conges']);
    }
  }

  onSaveDraft(): void {
    localStorage.setItem('congeDraft', JSON.stringify({
      ...this.congeForm.value,
      joursOuvrables: this.joursOuvrables,
      joursCalendaires: this.joursCalendaires
    }));
    alert('📝 Brouillon sauvegardé localement !');
  }
}