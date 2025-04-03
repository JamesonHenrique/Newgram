import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable, catchError, tap, throwError } from 'rxjs';

interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  private readonly ACCESS_TOKEN_KEY = 'token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly jwtHelper = new JwtHelperService();

  constructor(private http: HttpClient) {}

  set token(token: string) {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, token);
  }

  get token(): string {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY) || '';
  }

  set refreshToken(refreshToken: string) {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  get refreshToken(): string {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY) || '';
  }

  get userName(): string {
    if (!this.token) return '';

    const decodedToken = this.jwtHelper.decodeToken(this.token);
    return decodedToken?.nome || '';
  }

  get userId(): number {
    if (!this.token) return 0;

    const decodedToken = this.jwtHelper.decodeToken(this.token);
    return decodedToken?.id || 0;
  }

  get userRoles(): string[] {
    if (!this.token) return [];

    const decodedToken = this.jwtHelper.decodeToken(this.token);
    return decodedToken?.authorities || [];
  }

  isTokenValid(): boolean {
    if (!this.token) return false;
    return !this.jwtHelper.isTokenExpired(this.token);
  }

  isTokenExpired(): boolean {
    return !this.isTokenValid();
  }

  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
  }

  renewToken(): Observable<TokenResponse> {
    if (!this.refreshToken) {
      return throwError(() => new Error('Refresh token não encontrado'));
    }

    return this.http
      .post<TokenResponse>('/auth/refresh-token', { refreshToken: this.refreshToken })
      .pipe(
        tap((response) => {
          this.token = response.accessToken;
          this.refreshToken = response.refreshToken;
        }),
        catchError((error) => {
          this.logout();
          return throwError(() =>
            new Error('Falha ao renovar token: ' + (error.message || 'Erro desconhecido'))
          );
        })
      );
  }

  hasRequiredRole(requiredRoles: string[]): boolean {
    const userRoles = this.userRoles;
    return requiredRoles.some(role => userRoles.includes(role));
  }
}