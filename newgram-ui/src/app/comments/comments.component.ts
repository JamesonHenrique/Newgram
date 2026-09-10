import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ComentarioCreateDto, Pageable, UsuarioSummaryDto } from '../services/models';
import { ComentariosService, UsuariosService } from '../services/services';
import { TokenService } from '../services/token/token.service';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [DateFormatPipe, CommonModule, FormsModule],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnChanges {
  @Input() postId!: number;

  private destroy$ = new Subject<void>();

  comentariosSelected: any[] = [];
  usuarioLogado: UsuarioSummaryDto = {} as UsuarioSummaryDto;
  comentarioTexto: string = '';
  pageable: Pageable = {
    page: 0,
    size: 10,
    sort: [''],
  };

  constructor(
    private usuariosService: UsuariosService,
    private tokenService: TokenService,
    private comentariosService: ComentariosService
  ) {
    this.findUsuarioLogado();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['postId'] && this.postId) {
      this.loadComentarios(this.postId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  toggleCurtidaComentario(comentario: any, event: Event): void {
    event.stopPropagation();
    if (!comentario?.id) {
      return;
    }
    const curtido = !!comentario.curtidoPeloUsuario;
    comentario.curtidoPeloUsuario = !curtido;
    comentario.numeroCurtidas = (comentario.numeroCurtidas || 0) + (curtido ? -1 : 1);

    const acao$ = curtido
      ? this.comentariosService.descurtirComentario({ id: comentario.id })
      : this.comentariosService.curtirComentario({ id: comentario.id }).pipe(map(() => undefined));
    acao$.pipe(takeUntil(this.destroy$)).subscribe({
      error: () => {
        comentario.curtidoPeloUsuario = curtido;
        comentario.numeroCurtidas = (comentario.numeroCurtidas || 0) + (curtido ? 1 : -1);
      },
    });
  }

  enviarComentario() {
    if (!this.comentarioTexto.trim() || !this.postId) return;

    const comentarioDto: ComentarioCreateDto = {
      postId: this.postId,
      texto: this.comentarioTexto
    };

    this.comentariosService.criarComentario({ body: comentarioDto })
      .subscribe({
        next: (response) => {
          this.comentarioTexto = '';
          this.loadComentarios(this.postId);
        },
        error: (err) => {
          console.error('Erro ao enviar comentário:', err);
        }
      });
  }
  loadComentarios(postId: number) {
    this.comentariosService.listarComentariosPorPost({
      postId,
      pageable: this.pageable,
    }).subscribe({
      next: (comentarios: any) => {
        this.comentariosSelected = comentarios.content || [];
      },
      error: (err: any) => {
        console.error('Erro ao carregar comentários:', err);
      }
    });
  }

  getFotoPerfil(usuario?: UsuarioSummaryDto | { autor?: UsuarioSummaryDto } | null): string {
    if (usuario && 'autor' in usuario && usuario.autor) {
      return this.getFotoPerfil(usuario.autor);
    }

    const user = (usuario as UsuarioSummaryDto) || this.usuarioLogado;

    if (!user.fotoPerfil || user.fotoPerfil.trim() === '') {
      return '/icons/profile-placeholder.svg';
    }
    if (user.fotoPerfil.includes('post-placeholder.svg')) {
      return user.fotoPerfil;
    }
    return user.fotoPerfil;
  }
  handleFotoPerfilError( event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';
    imgElement.onerror = null;
  }
  findUsuarioLogado() {
    this.usuariosService
      .buscarUsuarioPorId({ id: this.tokenService.userId })
      .subscribe((res) => {
        const usuarioLogado: UsuarioSummaryDto = {
          ...res,
          fotoPerfil: res.fotoPerfil instanceof Blob
            ? URL.createObjectURL(res.fotoPerfil)
            : res.fotoPerfil
        };
        this.usuarioLogado = usuarioLogado;
      });
  }
}
