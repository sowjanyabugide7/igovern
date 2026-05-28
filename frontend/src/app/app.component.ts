import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <nav>
      <a routerLink="/programs">iGovern</a>
      <a *ngIf="auth.isLoggedIn()" routerLink="/programs">Programs</a>
      <a *ngIf="auth.isLoggedIn()" routerLink="/echoed">AMQ Messages</a>
      <span class="spacer"></span>
      <span *ngIf="auth.isLoggedIn()" class="muted" style="color:#cdd2da">
        {{ auth.username() }} ({{ auth.role() }})
      </span>
      <a *ngIf="auth.isLoggedIn()" (click)="logout()" style="cursor:pointer">Logout</a>
      <a *ngIf="!auth.isLoggedIn()" routerLink="/login">Login</a>
    </nav>
    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}
  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
