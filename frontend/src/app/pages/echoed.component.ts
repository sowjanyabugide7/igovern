import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-echoed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h2>AMQ Echoed Messages</h2>
      <p class="muted">
        Microservice 1 publishes a message every time a record is deleted.
        Microservice 2 receives that message from the queue and echoes it here.
      </p>
      <button class="btn" (click)="load()">Refresh</button>
      <ul style="margin-top:14px">
        <li *ngFor="let m of messages">{{ m }}</li>
        <li *ngIf="messages.length === 0" class="muted">No messages echoed yet.</li>
      </ul>
    </div>
  `
})
export class EchoedComponent implements OnInit, OnDestroy {
  messages: string[] = [];
  private timer?: ReturnType<typeof setInterval>;

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.load();
    this.timer = setInterval(() => this.load(), 3000);
  }

  ngOnDestroy() {
    if (this.timer) clearInterval(this.timer);
  }

  load() {
    this.api.listEchoed().subscribe(m => this.messages = m);
  }
}
