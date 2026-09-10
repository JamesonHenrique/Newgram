// usuario.resolver.ts
import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { UsuariosService } from '../services';
import { TokenService } from '../token/token.service';


@Injectable({ providedIn: 'root' })
export class UsuarioResolver implements Resolve<any> {
  constructor(
    private usuarioService: UsuariosService,
    private tokenService: TokenService
  ) {}

  resolve(route: ActivatedRouteSnapshot) {
    return this.usuarioService.buscarUsuarioPorId({ id: this.tokenService.userId });
  }
}