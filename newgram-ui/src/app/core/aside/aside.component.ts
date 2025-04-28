import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TokenService } from '../../services/token/token.service';
import { UsuariosService } from '../../services/services';
import { UsuarioResponseDto, UsuarioSummaryDto } from '../../services/models';

@Component({
  selector: 'app-aside',
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './aside.component.html',
  styleUrl: './aside.component.css',
})
export class AsideComponent {
  constructor(
    private tokenService: TokenService,
    private router: Router,
    private usuarioService: UsuariosService
  ) {}
  menuItems = [
    { path: '/feed', icon: 'fa-home', label: 'Página Inicial' },
    { path: '/explorar', icon: 'fa-compass', label: 'Explorar' },
    { path: '/pessoas', icon: 'fa-users', label: 'Pessoas' },
    { path: '/salvos', icon: 'fa-bookmark', label: 'Salvos' },
    { path: '/criar-post', icon: 'fa-plus-circle', label: 'Criar Post' },
  ];
  usuarioLogado: UsuarioSummaryDto = {} as UsuarioSummaryDto;
  particles = Array.from({ length: 8 }, (_, i) => ({
    x: Math.random() * 120,
    y: Math.random() * 60,
    delay: i * 150,
  }));
  private __fotoPerfil: string | undefined;
  getFotoPerfil(): string {
    if (
      !this.usuarioLogado.fotoPerfil ||
      this.usuarioLogado.fotoPerfil.trim() === ''
    ) {
      return '/icons/profile-placeholder.svg';
    }
    if (
      this.usuarioLogado.fotoPerfil.includes('/icons/profile-placeholder.svg')
    ) {
      return this.usuarioLogado.fotoPerfil;
    }
    return this.usuarioLogado.fotoPerfil;
  }
  get nomeComQuebra(): string {
    const nome = this.usuarioLogado.nome;
    if (nome && nome.length > 18) {
      return nome.slice(0, 18) + '<br>' + nome.slice(18);
    }
    return nome || '';
  }
  handleFotoPerfilError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';

    imgElement.onerror = null;
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
  updateSpotlight(event: MouseEvent, isActive: boolean) {
    const spotlight = document.querySelector('.active-route-spotlight');
    if (spotlight && isActive) {
      const target = event.currentTarget as HTMLElement;
      const rect = target.getBoundingClientRect();
      spotlight.setAttribute(
        'style',
        `opacity: 0.3; transform: translateY(${rect.top + rect.height / 2}px)`
      );
    }
  }
  logout() {
    this.tokenService.logout();
    this.router.navigate(['login']);
  }
}
