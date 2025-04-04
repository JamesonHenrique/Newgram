import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';

import { Router } from '@angular/router';
import { catchError, Observable, switchMap, throwError } from 'rxjs';
import { TokenService } from '../../../../../../token/token.service';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  let refreshInProgress = false;
  let pendingRequests: HttpRequest<any>[] = [];

  if (req.url.includes('/auth')) {
    return next(req);
  }

  return tokenService.renewTokenIfAboutToExpire().pipe(
    switchMap(renewResponse => {
      // Adiciona o token à requisição
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

  return tokenService.renewToken().pipe(
    switchMap(() => {
      const newRequest = addTokenToRequest(request, tokenService);
      return next(newRequest);
    }),
    catchError(error => {
      tokenService.logout();
      router.navigate(['/login']);
      return throwError(() => error);
    })
  );
}

// Função para adicionar token à requisição (mantida igual)
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