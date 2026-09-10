import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

/**
 * Toasts globais para erros que nenhuma tela trata: 409 (conflito/idempotência)
 * e 5xx. 401 passa direto (fluxo de auth/refresh) e 400 de validação fica com
 * os formulários.
 */
export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const toastr = inject(ToastrService);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 409) {
          toastr.warning(mensagemErro(error, 'Registro duplicado'), 'Conflito');
        } else if (error.status >= 500) {
          toastr.error('Tente novamente em instantes', 'Erro no servidor');
        }
      }
      return throwError(() => error);
    })
  );
};

function mensagemErro(error: HttpErrorResponse, padrao: string): string {
  const corpo = error.error as { message?: string } | string | null;
  if (typeof corpo === 'string' && corpo.trim()) {
    return corpo;
  }
  if (corpo && typeof corpo === 'object' && corpo.message) {
    return corpo.message;
  }
  return padrao;
}
