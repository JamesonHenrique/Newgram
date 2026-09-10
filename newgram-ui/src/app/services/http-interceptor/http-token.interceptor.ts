import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';

import { Router } from '@angular/router';
import { catchError, Observable, shareReplay, switchMap, throwError, finalize, of } from 'rxjs';
import { TokenService } from '../token/token.service';

let refreshInFlight$: Observable<unknown> | null = null;

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (req.url.includes('/auth')) {
    return next(req);
  }

  return tokenService.renewTokenIfAboutToExpire().pipe(
    switchMap(() => {
      const authReq = addTokenToRequest(req, tokenService);

      return next(authReq).pipe(
        catchError(error => {
          if (error instanceof HttpErrorResponse && error.status === 401) {
            return handle401Error(authReq, next, tokenService, router);
          }
          return throwError(() => error);
        })
      );
    })
  );
};

function handle401Error(
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
  tokenService: TokenService,
  router: Router
): Observable<HttpEvent<unknown>> {
  if (!tokenService.refreshToken) {
    tokenService.logout();
    router.navigate(['/login']);
    return throwError(() => new Error('Refresh token não disponível'));
  }

  // Single-flight: N requests 401 simultâneos disparam 1 refresh só.
  if (!refreshInFlight$) {
    refreshInFlight$ = tokenService.renewToken().pipe(
      shareReplay(1),
      finalize(() => (refreshInFlight$ = null)),
      catchError(error => {
        tokenService.logout();
        router.navigate(['/login']);
        return throwError(() => error);
      })
    );
  }

  return refreshInFlight$.pipe(
    switchMap(() => {
      const newRequest = addTokenToRequest(request, tokenService);
      return next(newRequest);
    })
  );
}

function addTokenToRequest(request: HttpRequest<unknown>, tokenService: TokenService): HttpRequest<unknown> {
  if (tokenService.token) {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${tokenService.token}`
      }
    });
  }
  return request;
}
