import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, interval, takeUntil } from 'rxjs';
import { TokenService } from '../../services/token/token.service';
import { NotificacoesService, PushService, UsuariosService } from '../../services/services';
import { NotificacaoResponseDto } from '../../services/models';
import { UsuarioSummaryDto } from '../../services/models';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  constructor(
    private tokenService: TokenService,
    private usuarioService: UsuariosService,
    private notificacoesService: NotificacoesService,
    private pushService: PushService,
    private route: ActivatedRoute,

  ) {}
  usuarioLogado: any;

  naoLidas = 0;
  notificacoes: NotificacaoResponseDto[] = [];
  sinoAberto = false;
  pushAtivo = false;
  pushMsg: string | null = null;

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
  this.atualizarContagem();
  // Badge atualizada a cada minuto sem recarregar a página.
  interval(60000)
    .pipe(takeUntil(this.destroy$))
    .subscribe(() => this.atualizarContagem());
}

ngOnDestroy(): void {
  this.destroy$.next();
  this.destroy$.complete();
}

atualizarContagem(): void {
  if (!this.tokenService.token) {
    return;
  }
  this.notificacoesService
    .contarNaoLidas()
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (res) => (this.naoLidas = res?.['naoLidas'] ?? 0),
      error: () => {},
    });
}

alternarSino(): void {
  this.sinoAberto = !this.sinoAberto;
  if (this.sinoAberto) {
    this.carregarNotificacoes();
  }
}

carregarNotificacoes(): void {
  this.notificacoesService
    .listarNotificacoes({ pageable: { page: 0, size: 10, sort: [''] } })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (page) => (this.notificacoes = (page.content as NotificacaoResponseDto[] | undefined) || []),
      error: () => (this.notificacoes = []),
    });
}

  marcarTodasComoLidas(): void {
    this.notificacoesService
      .marcarTodasComoVisualizadas()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.naoLidas = 0;
          this.notificacoes = this.notificacoes.map((n) => ({ ...n, lida: true }));
        },
        error: () => {},
      });
  }

  ativarPush(): void {
    this.pushMsg = null;
    this.pushService
      .ativarNesteDispositivo()
      .then(() => {
        this.pushAtivo = true;
        this.pushMsg = 'Push ativado neste dispositivo.';
      })
      .catch(() => {
        this.pushMsg = 'Não foi possível ativar o push.';
      });
  }

marcarComoLida(notificacao: NotificacaoResponseDto): void {
  if (!notificacao.id || notificacao.lida) {
    return;
  }
  this.notificacoesService
    .marcarComoVisualizada({ id: notificacao.id })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: () => {
        notificacao.lida = true;
        this.naoLidas = Math.max(0, this.naoLidas - 1);
      },
      error: () => {},
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
