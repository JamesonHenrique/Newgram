import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenService } from '../token/token.service';

export const LoginRedirectGuard: CanActivateFn = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isTokenValid()) {
    return router.createUrlTree(['/feed']);
  } else {
    return router.createUrlTree(['/login']);
  }
};
