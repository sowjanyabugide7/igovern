import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Program {
  id?: number;
  name: string;
  description?: string;
  startDate: string;
  endDate?: string | null;
  budget?: number | null;
  participants?: Participant[];
  attachments?: Attachment[];
}

export interface Participant {
  id?: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  enrollmentDate: string;
  weightKg?: number | null;
}

export interface Attachment {
  id: number;
  originalName: string;
  contentType: string;
  size: number;
  uploadedAt: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private dataBase = 'http://localhost:8081/api';
  private amqBase = 'http://localhost:8082';

  constructor(private http: HttpClient) {}

  // Programs
  listPrograms(): Observable<Program[]> { return this.http.get<Program[]>(`${this.dataBase}/programs`); }
  getProgram(id: number): Observable<Program> { return this.http.get<Program>(`${this.dataBase}/programs/${id}`); }
  createProgram(p: Program): Observable<Program> { return this.http.post<Program>(`${this.dataBase}/programs`, p); }
  updateProgram(id: number, p: Program): Observable<Program> { return this.http.put<Program>(`${this.dataBase}/programs/${id}`, p); }
  deleteProgram(id: number): Observable<void> { return this.http.delete<void>(`${this.dataBase}/programs/${id}`); }

  // Participants
  listParticipants(programId: number): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.dataBase}/programs/${programId}/participants`);
  }
  addParticipant(programId: number, p: Participant): Observable<Participant> {
    return this.http.post<Participant>(`${this.dataBase}/programs/${programId}/participants`, p);
  }
  deleteParticipant(participantId: number): Observable<void> {
    return this.http.delete<void>(`${this.dataBase}/programs/participants/${participantId}`);
  }

  // Attachments
  listAttachments(programId: number): Observable<Attachment[]> {
    return this.http.get<Attachment[]>(`${this.dataBase}/attachments/programs/${programId}`);
  }
  uploadAttachment(programId: number, file: File): Observable<Attachment> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post<Attachment>(`${this.dataBase}/attachments/programs/${programId}`, fd);
  }
  deleteAttachment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.dataBase}/attachments/${id}`);
  }
  downloadUrl(id: number): string { return `${this.dataBase}/attachments/${id}/download`; }

  // AMQ
  listEchoed(): Observable<string[]> { return this.http.get<string[]>(`${this.amqBase}/messages/echoed`); }
}
