import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, catchError, filter, finalize, switchMap, take, throwError } from 'rxjs';
import { TokenService } from '../../../../../../token/token.service';

@Injectable()
export class HttpTokenInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private tokenService: TokenService, private router: Router) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    if (this.shouldSkipAuth(request.url)) {
      return next.handle(request);
    }

    const token = this.tokenService.token;
    if (token) {
      request = this.addTokenToRequest(request, token);
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  private shouldSkipAuth(url: string): boolean {
    return url.includes('/auth/login') ||
           url.includes('/auth/register') ||
           url.includes('/auth/refresh-token');
  }

  private addTokenToRequest(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return request.clone({
      headers: request.headers.set('Authorization', `Bearer ${token}`)
    });
  }

  private handle401Error(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.tokenService.renewToken().pipe(
        switchMap(response => {
          this.refreshTokenSubject.next(response.accessToken);
          return next.handle(this.addTokenToRequest(request, response.accessToken));
        }),
        catchError(error => {
          this.tokenService.logout();
          this.router.navigate(['/login'], { queryParams: { expired: 'true' } });
          return throwError(() => new Error('Sessão expirada. Por favor, faça login novamente.'));
        }),
        finalize(() => {
          this.isRefreshing = false;
        })
      );
    } else {
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => {
          return next.handle(this.addTokenToRequest(request, token!));
        })
      );
    }
  }
}