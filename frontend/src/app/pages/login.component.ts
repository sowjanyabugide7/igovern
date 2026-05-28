import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="card" style="max-width:400px;margin:40px auto">
      <h2>{{ mode === 'login' ? 'Sign in' : 'Register' }}</h2>
      <label>Username</label>
      <input [(ngModel)]="username" name="username" autocomplete="username" />
      <label>Password</label>
      <input type="password" [(ngModel)]="password" name="password" autocomplete="current-password" />
      <div style="margin-top:14px;display:flex;gap:8px">
        <button class="btn" (click)="submit()">{{ mode === 'login' ? 'Login' : 'Register' }}</button>
        <button class="btn secondary" (click)="toggle()">
          {{ mode === 'login' ? 'Need an account?' : 'Have an account?' }}
        </button>
      </div>
      <div *ngIf="error" class="error">{{ error }}</div>
      <p class="muted" style="margin-top:14px">
        Default users: <strong>admin / admin123</strong> or <strong>user / user123</strong>
      </p>
    </div>
  `
})
export class LoginComponent {
  mode: 'login' | 'register' = 'login';
  username = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = '';
    const obs = this.mode === 'login'
      ? this.auth.login(this.username, this.password)
      : this.auth.register(this.username, this.password);
    obs.subscribe({
      next: () => this.router.navigate(['/programs']),
      error: err => this.error = err?.error?.message || 'Authentication failed'
    });
  }

  toggle() {
    this.mode = this.mode === 'login' ? 'register' : 'login';
    this.error = '';
  }
}
