import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'programs', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login.component').then(m => m.LoginComponent) },
  {
    path: 'programs',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/programs.component').then(m => m.ProgramsComponent)
  },
  {
    path: 'programs/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/program-detail.component').then(m => m.ProgramDetailComponent)
  },
  {
    path: 'echoed',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/echoed.component').then(m => m.EchoedComponent)
  },
  { path: '**', redirectTo: 'programs' }
];
