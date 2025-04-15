import { Component } from '@angular/core';
import { TokenService } from '../../services/token/token.service';
import { UsuariosService } from '../../services/services';
import { UsuarioSummaryDto } from '../../services/models';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  constructor(
    private tokenService: TokenService,
    private usuarioService: UsuariosService
  ) {}
  usuarioLogado: UsuarioSummaryDto = {} as UsuarioSummaryDto;

  private __fotoPerfil: string | undefined;
  getFotoPerfil(): string {
    if (
      this.usuarioLogado?.fotoPerfil &&
      this.usuarioLogado.fotoPerfil.trim() !== ''
    ) {
      return 'data:image/jpg;base64,' + this.usuarioLogado.fotoPerfil;
    }
    return '/icons/profile-placeholder.svg';
  }
  ngOnInit(): void {
    this.findUsuarioLogado();
  }
  findUsuarioLogado() {
    this.usuarioService
      .buscarUsuarioPorId({ id: this.tokenService.userId })
      .subscribe((res) => {
        this.usuarioLogado = res;
      });
  }
}
