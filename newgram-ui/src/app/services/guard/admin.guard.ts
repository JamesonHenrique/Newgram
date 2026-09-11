import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { TokenService } from '../token/token.service';
import { UsuariosService } from '../services';

/** Rota /moderacao: só ADMIN (papel vem do perfil). */
export const AdminGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const usuariosService = inject(UsuariosService);
  const router = inject(Router);
  if (!tokenService.isTokenValid() || !tokenService.userId) {
    router.navigate(['login']);
    return false;
  }
  return usuariosService.buscarUsuarioPorId({ id: tokenService.userId }).pipe(
    map((usuario) => {
      if (usuario?.papel === 'ADMIN') {
        return true;
      }
      router.navigate(['feed']);
      return false;
    }),
    catchError(() => {
      router.navigate(['feed']);
      return of(false);
    })
  );
};
