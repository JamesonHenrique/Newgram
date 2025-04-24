import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ComentarioCreateDto, Pageable, UsuarioSummaryDto } from '../services/models';
import { ComentariosService, UsuariosService } from '../services/services';
import { TokenService } from '../services/token/token.service';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [DateFormatPipe, CommonModule, FormsModule],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnChanges {
  @Input() postId!: number;

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

    if (user?.fotoPerfil?.trim()) {
      return 'data:image/jpg;base64,' + user.fotoPerfil;
    }

    return '/icons/profile-placeholder.svg';
  }

  findUsuarioLogado() {
    this.usuariosService
      .buscarUsuarioPorId({ id: this.tokenService.userId })
      .subscribe((res) => {
        this.usuarioLogado = res;
      });
  }
}