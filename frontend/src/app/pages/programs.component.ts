import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, Program } from '../services/api.service';

@Component({
  selector: 'app-programs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="card">
      <h2>Medical Research Programs</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Start</th><th>End</th><th>Budget</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let p of programs">
            <td>{{ p.id }}</td>
            <td><a [routerLink]="['/programs', p.id]">{{ p.name }}</a></td>
            <td>{{ p.startDate }}</td>
            <td>{{ p.endDate || '-' }}</td>
            <td>{{ p.budget ?? '-' }}</td>
            <td><button class="btn danger" (click)="remove(p)">Delete</button></td>
          </tr>
          <tr *ngIf="programs.length === 0">
            <td colspan="6" class="muted">No programs yet.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card">
      <h3>Create Program</h3>
      <label>Name</label>
      <input [(ngModel)]="form.name" name="name" />
      <label>Description</label>
      <textarea [(ngModel)]="form.description" name="description" rows="2"></textarea>
      <label>Start Date</label>
      <input type="date" [(ngModel)]="form.startDate" name="startDate" />
      <label>End Date</label>
      <input type="date" [(ngModel)]="form.endDate" name="endDate" />
      <label>Budget</label>
      <input type="number" [(ngModel)]="form.budget" name="budget" />
      <div style="margin-top:14px"><button class="btn" (click)="create()">Save</button></div>
      <div *ngIf="error" class="error">{{ error }}</div>
    </div>
  `
})
export class ProgramsComponent implements OnInit {
  programs: Program[] = [];
  form: Program = { name: '', description: '', startDate: '', endDate: '', budget: null };
  error = '';

  constructor(private api: ApiService) {}

  ngOnInit() { this.load(); }

  load() {
    this.api.listPrograms().subscribe({
      next: data => this.programs = data,
      error: err => this.error = err?.error?.message || 'Load failed'
    });
  }

  create() {
    this.error = '';
    this.api.createProgram(this.form).subscribe({
      next: () => {
        this.form = { name: '', description: '', startDate: '', endDate: '', budget: null };
        this.load();
      },
      error: err => this.error = err?.error?.message || 'Create failed'
    });
  }

  remove(p: Program) {
    if (!p.id) return;
    if (!confirm(`Delete program "${p.name}"?`)) return;
    this.api.deleteProgram(p.id).subscribe(() => this.load());
  }
}
