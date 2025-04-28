import { Component } from '@angular/core';
import { TokenService } from '../../services/token/token.service';
import { UsuariosService } from '../../services/services';
import { UsuarioSummaryDto } from '../../services/models';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  constructor(
    private tokenService: TokenService,
    private usuarioService: UsuariosService,
    private route: ActivatedRoute,
  ) {}
  usuarioLogado: UsuarioSummaryDto = {} as UsuarioSummaryDto;

  private __fotoPerfil: string | undefined;
  getFotoPerfil(): string {
    if (!this.usuarioLogado.fotoPerfil || this.usuarioLogado.fotoPerfil.trim() === '') {
      return '/icons/profile-placeholder.svg';
    }
    if (this.usuarioLogado.fotoPerfil.includes('/icons/profile-placeholder.svg')) {
      return this.usuarioLogado.fotoPerfil;
    }
    return this.usuarioLogado.fotoPerfil;
  }


  handleFotoPerfilError(event: Event): void {
    const imgElement =event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';


    imgElement.onerror = null;
  }
ngOnInit(): void {
  this.route.data.subscribe(data => {
    if (data['usuario']) {
      this.usuarioLogado = data['usuario'];
    } else {
      this.findUsuarioLogado();
    }
  });
}
findUsuarioLogado() {
  const cachedUser = localStorage.getItem('usuarioLogado');
  if (cachedUser) {
    this.usuarioLogado = JSON.parse(cachedUser);
  }

  this.usuarioService
    .buscarUsuarioPorId({ id: this.tokenService.userId })
    .subscribe((res) => {
      this.usuarioLogado = res;
      localStorage.setItem('usuarioLogado', JSON.stringify(res));
    });
}
}
