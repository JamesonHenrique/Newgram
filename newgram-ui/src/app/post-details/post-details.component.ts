import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  Output,
  SimpleChanges,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormatNumberPipe } from '../services/pipes/format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { Pageable, UsuarioSummaryDto } from '../services/models';
import {
  ComentariosService,
  EnquetesService,
  ModeracaoService,
  PostsService,
  UsuariosService,
} from '../services/services';
import { TokenService } from '../services/token/token.service';
import { ToastrService } from 'ngx-toastr';
import { CommentsComponent } from '../comments/comments.component';
import { finalize, map, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-post-details',
  imports: [CommonModule, FormsModule, FormatNumberPipe, DateFormatPipe, CommentsComponent],
  templateUrl: './post-details.component.html',
  styleUrl: './post-details.component.css',
})
export class PostDetailsComponent {
  @Input() index: number = 1;
  @Input() postSelected: any;
  @Output() postSelectedChange = new EventEmitter<any>();
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @Input() isOpen: boolean = false;
  private destroy$ = new Subject<void>();
  private loading = false;

  isModalActive = false;
  post: any;
  postService: any;

  private postsService = inject(PostsService);
  private tokenService = inject(TokenService);
  private moderacaoService = inject(ModeracaoService);
  private enquetesService = inject(EnquetesService);
  private toastr = inject(ToastrService);

  mostrarFormEnquete = false;
  novaEnquetePergunta = '';
  novaEnqueteOpcoes: string[] = ['', ''];

  trackByIndex(index: number): number {
    return index;
  }

  votarEnquete(enquete: any, opcao: any, event: Event): void {
    event.stopPropagation();
    if (!enquete?.id || !opcao?.id || enquete.minhaOpcaoId || enquete.encerrada) {
      return;
    }
    this.enquetesService
      .votarEnquete({ id: enquete.id, body: { opcaoId: opcao.id } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (atualizada) => {
          if (this.postSelected) {
            this.postSelected = { ...this.postSelected, enquete: atualizada };
          }
        },
        error: () => {},
      });
  }

  criarEnquete(event: Event): void {
    event.stopPropagation();
    const pergunta = this.novaEnquetePergunta.trim();
    const opcoes = this.novaEnqueteOpcoes.map((o) => o.trim()).filter((o) => o);
    if (!this.postSelected?.id || !pergunta || opcoes.length < 2) {
      this.toastr.warning('Informe pergunta e ao menos 2 opções.');
      return;
    }
    this.enquetesService
      .criarEnquete({ body: { pergunta, postId: this.postSelected.id, opcoes } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (enquete) => {
          this.postSelected = { ...this.postSelected, enquete };
          this.mostrarFormEnquete = false;
          this.novaEnquetePergunta = '';
          this.novaEnqueteOpcoes = ['', ''];
        },
        error: () => {},
      });
  }

  excluirEnquete(enquete: any, event: Event): void {
    event.stopPropagation();
    if (!enquete?.id) {
      return;
    }
    this.enquetesService
      .excluirEnquete({ id: enquete.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          if (this.postSelected) {
            this.postSelected = { ...this.postSelected, enquete: null };
          }
        },
        error: () => {},
      });
  }
  get postPertenceAoUsuarioLogado(): boolean {
    return this.post?.autorId === this.tokenService.userId;
  }
  verPerfil() {
    this.router.navigate(['/perfil', this.post?.autorId]);
  }
  toggleLike(post: any, event: Event): void {
    event.stopPropagation();
    if (post.isAnimating) return;

    post.isAnimating = true;
    const wasLiked = post.isLiked;

    post.isLiked = !wasLiked;
    post.numeroCurtidas += wasLiked ? -1 : 1;

    const likeAction$ = wasLiked
      ? this.postsService.descurtirPost({ id: post.id })
      : this.postsService.curtirPost({ id: post.id }).pipe(map(() => undefined));

    likeAction$
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (post.isAnimating = false))
      )
      .subscribe({
        error: () => {
          post.isLiked = wasLiked;
          post.numeroCurtidas += wasLiked ? 1 : -1;
        },
      });
  }

  toggleFavorite(post: any, event: Event): void {
    event.stopPropagation();
    if (post.isFavAnimating) return;

    post.isFavAnimating = true;
    const wasFavorite = post.isFavorite;

    post.isFavorite = !wasFavorite;
    post.numeroFavoritos += wasFavorite ? -1 : 1;

    const favAction$ = wasFavorite
      ? this.postsService.removerPostSalvo({ id: post.id })
      : this.postsService.salvarPost({ id: post.id });

    favAction$
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (post.isFavAnimating = false))
      )
      .subscribe({
        error: () => {
          post.isFavorite = wasFavorite;
          post.numeroFavoritos += wasFavorite ? 1 : -1;
        },
      });
  }

  denunciarPost(event: Event): void {
    event.stopPropagation();
    const postId = this.postSelected?.id;
    if (!postId) {
      return;
    }
    this.moderacaoService
      .denunciar({
        body: { tipoAlvo: 'POST', alvoId: postId, motivo: 'Conteúdo inadequado' },
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.toastr.success('Denúncia registrada. Obrigado pelo aviso.'),
        error: () => {},
      });
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['postSelected']) {
      if (this.postSelected?.id) {
        this.openModal();
        // Analytics do criador (dedupeado no back por usuário+dia).
        this.postsService.registrarVisualizacao({ id: this.postSelected.id }).subscribe({
          error: () => {},
        });
      } else {
        this.closeModal();
      }
    }
  }
  openModal(): void {
    this.isModalActive = true;
    document.body.style.overflow = 'hidden';
    setTimeout(() => {
      const overlay = document.getElementById('postDetailOverlay');
      if (overlay) overlay.classList.add('active');
    }, 10);
  }
  editPost(id: number): void {
    this.router.navigate(['/editar-post', id]);
  }

  constructor(private route: ActivatedRoute, private router: Router) {}
  @HostListener('document:keydown.escape', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    if (this.isModalActive) {
      this.closeModal();
    }
  }
  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  closePost(): void {
    window.history.back();
  }

  getTags(text: string): string[] {
    return text
      .split('#')
      .slice(1)
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
  }
  getFotoPerfil(user: any): string {
    if (user?.fotoPerfil && user.fotoPerfil.trim() !== '') {
      return user.fotoPerfil;
    }
    return '/icons/profile-placeholder.svg';
  }
  getImagemPost(imagem: string | null | undefined): string {
    if (!imagem || imagem.trim() === '') {
      return '/icons/post-placeholder.svg';
    }
    if (imagem.includes('post-placeholder.svg')) {
      return imagem;
    }
    return imagem;
  }
  handleImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/post-placeholder.svg';
    imgElement.onerror = null;
  }
  private isClosing = false;

  onOverlayClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.closeModal();
    }
  }
  startCloseAnimation(): void {
    this.isClosing = true;
    const overlay = document.getElementById('postDetailOverlay');
    const container = document.querySelector('.post-detail-container');

    if (overlay && container) {
      overlay.classList.remove('active');
      container.classList.remove('active');

      setTimeout(() => {
        this.closeModal();
        this.isClosing = false;
      }, 300);
    }
  }
  closeModal(): void {
    const overlay = document.getElementById('postDetailOverlay');
    if (overlay) overlay.classList.remove('active');

    setTimeout(() => {
      this.isModalActive = false;
      this.postSelected = null;
      this.postSelectedChange.emit(null);
      document.body.style.overflow = '';
    }, 300);
  }
}
