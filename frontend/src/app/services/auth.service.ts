import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

const TOKEN_KEY = 'igovern.token';
const USER_KEY = 'igovern.user';
const ROLE_KEY = 'igovern.role';

export interface TokenResponse {
  token: string;
  username: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = 'http://localhost:8081/api/auth';
  loggedIn = signal<boolean>(!!localStorage.getItem(TOKEN_KEY));

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.base}/login`, { username, password })
      .pipe(tap(r => this.store(r)));
  }

  register(username: string, password: string): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.base}/register`, { username, password })
      .pipe(tap(r => this.store(r)));
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ROLE_KEY);
    this.loggedIn.set(false);
  }

  token(): string | null { return localStorage.getItem(TOKEN_KEY); }
  username(): string | null { return localStorage.getItem(USER_KEY); }
  role(): string | null { return localStorage.getItem(ROLE_KEY); }
  isLoggedIn(): boolean { return this.loggedIn(); }

  private store(r: TokenResponse) {
    localStorage.setItem(TOKEN_KEY, r.token);
    localStorage.setItem(USER_KEY, r.username);
    localStorage.setItem(ROLE_KEY, r.role);
    this.loggedIn.set(true);
  }
}
