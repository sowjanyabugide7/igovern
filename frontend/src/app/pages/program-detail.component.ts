import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService, Attachment, Participant, Program } from '../services/api.service';

@Component({
  selector: 'app-program-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <a routerLink="/programs">&larr; Back to programs</a>

    <div class="card" *ngIf="program">
      <h2>{{ program.name }}</h2>
      <p class="muted">{{ program.description }}</p>
      <p>Start: {{ program.startDate }} | End: {{ program.endDate || '-' }} | Budget: {{ program.budget ?? '-' }}</p>
    </div>

    <div class="card">
      <h3>Participants</h3>
      <table>
        <thead><tr><th>ID</th><th>Name</th><th>DOB</th><th>Enrolled</th><th>Weight (kg)</th><th></th></tr></thead>
        <tbody>
          <tr *ngFor="let p of participants">
            <td>{{ p.id }}</td>
            <td>{{ p.firstName }} {{ p.lastName }}</td>
            <td>{{ p.dateOfBirth }}</td>
            <td>{{ p.enrollmentDate }}</td>
            <td>{{ p.weightKg ?? '-' }}</td>
            <td><button class="btn danger" (click)="removeParticipant(p)">Delete</button></td>
          </tr>
          <tr *ngIf="participants.length === 0"><td colspan="6" class="muted">No participants.</td></tr>
        </tbody>
      </table>

      <h4 style="margin-top:20px">Add Participant</h4>
      <label>First name</label>
      <input [(ngModel)]="newParticipant.firstName" name="firstName" />
      <label>Last name</label>
      <input [(ngModel)]="newParticipant.lastName" name="lastName" />
      <label>Date of birth</label>
      <input type="date" [(ngModel)]="newParticipant.dateOfBirth" name="dob" />
      <label>Enrollment date</label>
      <input type="date" [(ngModel)]="newParticipant.enrollmentDate" name="enroll" />
      <label>Weight (kg)</label>
      <input type="number" step="0.1" [(ngModel)]="newParticipant.weightKg" name="weight" />
      <div style="margin-top:14px"><button class="btn" (click)="addParticipant()">Add</button></div>
    </div>

    <div class="card">
      <h3>File Attachments</h3>
      <table>
        <thead><tr><th>ID</th><th>File</th><th>Type</th><th>Size</th><th></th></tr></thead>
        <tbody>
          <tr *ngFor="let a of attachments">
            <td>{{ a.id }}</td>
            <td>{{ a.originalName }}</td>
            <td>{{ a.contentType }}</td>
            <td>{{ a.size }}</td>
            <td>
              <a class="btn secondary" [href]="downloadUrl(a)" target="_blank">Download</a>
              <button class="btn danger" (click)="deleteAttachment(a)">Delete</button>
            </td>
          </tr>
          <tr *ngIf="attachments.length === 0"><td colspan="5" class="muted">No attachments.</td></tr>
        </tbody>
      </table>
      <h4 style="margin-top:20px">Upload File</h4>
      <input type="file" (change)="onFileSelected($event)" />
      <button class="btn" style="margin-left:8px" (click)="upload()" [disabled]="!selectedFile">Upload</button>
    </div>

    <div *ngIf="error" class="error">{{ error }}</div>
  `
})
export class ProgramDetailComponent implements OnInit {
  programId!: number;
  program: Program | null = null;
  participants: Participant[] = [];
  attachments: Attachment[] = [];
  newParticipant: Participant = {
    firstName: '', lastName: '', dateOfBirth: '', enrollmentDate: '', weightKg: null
  };
  selectedFile: File | null = null;
  error = '';

  constructor(private api: ApiService, private route: ActivatedRoute) {}

  ngOnInit() {
    this.programId = Number(this.route.snapshot.paramMap.get('id'));
    this.refresh();
  }

  refresh() {
    this.api.getProgram(this.programId).subscribe(p => this.program = p);
    this.api.listParticipants(this.programId).subscribe(p => this.participants = p);
    this.api.listAttachments(this.programId).subscribe(a => this.attachments = a);
  }

  addParticipant() {
    this.error = '';
    this.api.addParticipant(this.programId, this.newParticipant).subscribe({
      next: () => {
        this.newParticipant = { firstName: '', lastName: '', dateOfBirth: '', enrollmentDate: '', weightKg: null };
        this.refresh();
      },
      error: err => this.error = err?.error?.message || 'Add failed'
    });
  }

  removeParticipant(p: Participant) {
    if (!p.id) return;
    if (!confirm(`Delete participant ${p.firstName} ${p.lastName}?`)) return;
    this.api.deleteParticipant(p.id).subscribe(() => this.refresh());
  }

  onFileSelected(ev: Event) {
    const input = ev.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] || null;
  }

  upload() {
    if (!this.selectedFile) return;
    this.error = '';
    this.api.uploadAttachment(this.programId, this.selectedFile).subscribe({
      next: () => { this.selectedFile = null; this.refresh(); },
      error: err => this.error = err?.error?.message || 'Upload failed'
    });
  }

  deleteAttachment(a: Attachment) {
    if (!confirm(`Delete file ${a.originalName}?`)) return;
    this.api.deleteAttachment(a.id).subscribe(() => this.refresh());
  }

  downloadUrl(a: Attachment): string { return this.api.downloadUrl(a.id); }
}
