import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { DomSanitizer, Title } from '@angular/platform-browser';
import { FormatNumberPipe } from '../format-number.pipe';
import { PostsService, UsuariosService } from '../services/services';
import { Pageable } from '../services/models';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { TokenService } from '../services/token/token.service';
import { Observable, Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError, finalize, tap, map } from 'rxjs/operators';
@Component({
  selector: 'app-home',
  imports: [
    CommonModule,
    PostDetailsComponent,
    FormatNumberPipe,
    DateFormatPipe,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private destroy$ = new Subject<void>();
  private loading = false;

  posts: any[] = [];
  topCreators: any[] = [];
  usuarioLogado: any | null = null;

  postSelected: any | null = null;
  showDetail = false;

  pageable: Pageable = {
    page: 0,
    size: 10,
    sort: [''],
  };

  pageableCreators: Pageable = {
    page: 0,
    size: 4,
    sort: [''],
  };
  otherUsers = [
    {
      id: 1,
      avatar:
        'https://br.web.img3.acsta.net/c_310_420/pictures/22/03/17/20/59/0915999.jpg',
      name: 'Danilo Gentili',
      username: 'danilo',
    },

    {
      id: 2,
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRV61NGGfrNzInHErojPbfVKVdx_KvNiT_bmg&s',
      name: 'Felca',
      username: 'felca',
    },
    {
      id: 3,
      avatar:
        'https://blogdohiellevy.com.br/wp-content/uploads/2024/08/WhatsApp-Image-2024-08-27-at-13.22.20-768x1024.jpeg',
      name: 'Julio Balestrin',
      username: 'julio',
    },
  ];
  topics = [
    'fotografia',
    'esportes',
    'tecnologia',
    'musica',
    'beleza',
    'moda',
    'gastronomia',
  ].filter((item, index, self) => self.indexOf(item) === index); // Remove duplicatas

  constructor(
    private title: Title,
    private postsService: PostsService,
    private usuariosService: UsuariosService,
    private tokenService: TokenService,
    private sanitizer: DomSanitizer,
    private changeDetector: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.title.setTitle('Feed');
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadInitialData(): void {
    this.loading = true;

    forkJoin([
      this.listFeed(),
      this.findAllTopCriadores(),
      this.findUsuarioLogado(),
    ])
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe();
  }

  getFotoPerfil(user: any): string {
    if (!user.fotoPerfil || user.fotoPerfil.trim() === '') {
      return '/icons/profile-placeholder.svg';
    }
    if (user.fotoPerfil.includes('post-placeholder.svg')) {
      return user.fotoPerfil;
    }
    return user.fotoPerfil;
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
  handleFotoPerfilError( event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';

    imgElement.onerror = null;
  }

  handleImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/post-placeholder.svg';


    imgElement.onerror = null;
  }

  private findUsuarioLogado(): Observable<void> {
    if (!this.tokenService.userId) return of(undefined);

    return this.usuariosService
      .buscarUsuarioPorId({ id: this.tokenService.userId })
      .pipe(
        tap((res) => (this.usuarioLogado = res)),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao buscar usuário logado:', error);
          return of(undefined);
        })
      );
  }

  private findAllTopCriadores(): Observable<void> {
    return this.usuariosService
      .listarUsuariosMaisFamosos({ pageable: this.pageableCreators })
      .pipe(
        tap((response) => (this.topCreators = response.content || [])),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao buscar top criadores:', error);
          return of(undefined);
        })
      );
  }

  private listFeed(): Observable<void> {
    return this.postsService.listarFeed({ pageable: this.pageable }).pipe(
      tap((response) => {
        this.posts = (response.content || []).map((post: any) => ({
          ...post,
          isLiked: post.curtidoPeloUsuario,
          isFavorite: post.salvoPeloUsuario,
          isAnimating: false,
          isFavAnimating: false,
        }));
      }),
      map(() => undefined),
      catchError((error) => {
        console.error('Erro ao carregar feed:', error);
        return of(undefined);
      })
    );
  }

  openPostDetails(post: any, event: MouseEvent): void {
    event.preventDefault();
    this.postSelected = post;
    this.showDetail = true;
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
      : this.postsService.curtirPost({ id: post.id });

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

  trackByPostId(index: number, post: any): number {
    return post.id;
  }

  trackByCreatorId(index: number, creator: any): number {
    return creator.id || 0;
  }
}
