import { Pageable } from './../services/models/pageable';
import { PostsService } from './../services/services/posts.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { Title } from '@angular/platform-browser';
import { FormatNumberPipe } from '../format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { FormsModule } from '@angular/forms';
import { Observable, Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError, finalize, tap, map } from 'rxjs/operators';

@Component({
  selector: 'app-explore',
  imports: [
    CommonModule,
    PostDetailsComponent,
    FormatNumberPipe,
    DateFormatPipe,
    FormsModule,
  ],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.css',
})
export class ExploreComponent {
  private destroy$ = new Subject<void>();
  private loading = false;

  celebrityPosts: any[] = [];
  viralPosts: any[] = [];
  followedPeoplePosts: any[] = [];
  postsRecomendados: any[] = [];
  postsPorLegenda: any[] = [];

  postSelected: any | null = null;
  selectedIndex: number | null = null;
  selectedPostType: string = '';
  termo: string = '';

  pageable: Pageable = {
    page: 0,
    size: 4,
    sort: [''],
  };
  numberOfElements = 0;
  totalPages = 0;
  totalElements = 0;
  popularTags = [
    'Humor',
    'Esportes',
    'VidaSaudável',
    'Música',
    'Carnaval',
    'Show',
    'Família',
    'Domingão',
    'Viagem',
    'Turismo',
    'RioGrandeDoSul',
    'Evento',
    'TV',
    'Entretenimento',
  ];

  constructor(private title: Title, private postsService: PostsService) {
    this.title.setTitle('Explore');
  }

  ngOnInit(): void {
    this.loadInitialPosts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get pesquisaOcorreu(): boolean {
    return this.termo.trim().length > 0;
  }

  get isLoading(): boolean {
    return this.loading;
  }

  private loadInitialPosts(): void {
    this.loading = true;

    forkJoin([
      this.listCelebritiesPost(),
      this.listViralPost(),
      this.listFollowedPeoplePost(),
      this.listPostRecomendados(),
    ])
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe();
  }

  openModal(post: any, index: number, postType: string): void {
    this.postSelected = post;
    this.selectedIndex = index;
    this.selectedPostType = postType;
  }

  openComments(post: any, event: MouseEvent) {}

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
  openPostDetails(post: any, event: Event): void {
    event.preventDefault();
    const target = event.target as HTMLElement;
    if (target.closest('.btn-icon, [class*="fa-"]')) {
      return;
    }
    this.postSelected = post;
  }

  getModalId(): string {
    return `modal-${this.selectedPostType}-${this.selectedIndex}`;
  }

  isUltimaPagina(): boolean {
    return (this.pageable.page || 0) >= this.totalPages - 1;
  }

  avancarPagina(): void {
    if (this.isUltimaPagina() || this.numberOfElements === 0) {
      this.pageable.page = 0;
    } else {
      this.pageable.page = (this.pageable.page || 0) + 1;
    }
  }
  carregarMais(tipoDeUsuario: string): void {
    this.avancarPagina();

    switch (tipoDeUsuario) {
      case 'followedPeoplePosts':
        this.listFollowedPeoplePost();
        break;
      case 'random':
        this.listPostRecomendados();
        break;

        break;
    }
  }
  listPostRecomendados(): Observable<void> {
    return this.postsService
      .listarPostsRecomendados({ pageable: this.pageable })
      .pipe(
        tap((response) => {
          this.postsRecomendados = (response.content || []).map(
            (post: any) => ({
              ...post,
              isLiked: post.curtidoPeloUsuario,
              isFavorite: post.salvoPeloUsuario,
              isAnimating: false,
              isFavAnimating: false,
            })
          );
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts recomendados:', error);
          return of(undefined);
        })
      );
  }

  listPostsByLegenda(): void {
    if (!this.termo.trim()) return;

    this.loading = true;
    this.postsService
      .buscarPostsPorLegenda({
        termo: this.termo,
        pageable: this.pageable,
      })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (response) =>
          (this.postsPorLegenda = (response.content || []).map((post: any) => ({
            ...post,
            isLiked: post.curtidoPeloUsuario,
            isFavorite: post.salvoPeloUsuario,
            isAnimating: false,
            isFavAnimating: false,
          }))),
        error: (error) =>
          console.error('Erro ao buscar posts por legenda:', error),
      });
  }

  private listCelebritiesPost(): Observable<void> {
    return this.postsService
      .listarPostsPopulares({ pageable: this.pageable })
      .pipe(
        tap((response) => {
          this.celebrityPosts = (response.content || []).map((post: any) => ({
            ...post,
            isLiked: post.curtidoPeloUsuario,
            isFavorite: post.salvoPeloUsuario,
            isAnimating: false,
            isFavAnimating: false,
          }));
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts de celebridades:', error);
          return of(undefined);
        })
      );
  }

  private listViralPost(): Observable<void> {
    return this.postsService
      .listarPostsTendencias({ pageable: this.pageable })
      .pipe(
        tap((response) => {
          this.viralPosts = (response.content || []).map((post: any) => ({
            ...post,
            isLiked: post.curtidoPeloUsuario,
            isFavorite: post.salvoPeloUsuario,
            isAnimating: false,
            isFavAnimating: false,
          }));
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts virais:', error);
          return of(undefined);
        })
      );
  }

  private listFollowedPeoplePost(): Observable<void> {
    return this.postsService
      .listarPostsPopularesSeguidores({ pageable: this.pageable })
      .pipe(
        tap((response) => {
          this.followedPeoplePosts = (response.content || []).map(
            (post: any) => ({
              ...post,
              isLiked: post.curtidoPeloUsuario,
              isFavorite: post.salvoPeloUsuario,
              isAnimating: false,
              isFavAnimating: false,
            })
          );
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts de pessoas seguidas:', error);
          return of(undefined);
        })
      );
  }

  onSearchTermChange(term: string): void {
    this.termo = term;
    if (term) {
      this.listPostsByLegenda();
    } else {
      this.postsPorLegenda = [];
    }
  }
  getFotoPerfil(user: any | null): string {
    if (user?.fotoPerfil?.trim()) {
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
}
