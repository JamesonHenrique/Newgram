import { HashtagsService } from './../services/services/hashtags.service';
import { Pageable } from './../services/models/pageable';
import { PostsService } from './../services/services/posts.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { Title } from '@angular/platform-browser';
import { FormatNumberPipe } from '../services/pipes/format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { FormsModule } from '@angular/forms';
import { Observable, Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError, finalize, tap, map } from 'rxjs/operators';
import { HashtagSummaryDto } from '../services/models';

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
  popularTags:any = [];
  tagsByPostId: { [postId: number]: HashtagSummaryDto[] } = {};
  isLoading = false;

  constructor(private title: Title, private postsService: PostsService, private hashtagsService: HashtagsService) {
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

  findHashtagsByPostId(postId: number) {
    this.hashtagsService.listarHashtagsPorPostId({ postId: postId })
      .pipe(
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (response) => {
          this.tagsByPostId[postId] = response || [];
        },
        error: (error) => console.error('Erro ao buscar hashtags por post:', error),
      });
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
  openPostDetails(post: any, event: Event): void {
    event.preventDefault();
    const target = event.target as HTMLElement;
    if (target.closest('.btn-icon, [class*="fa-"]')) {
      return;
    }
    this.postSelected = post;
  }


  private processPosts(posts: any[], response: any): any[] {
    return (response.content || []).map((post: any) => {
      this.findHashtagsByPostId(post.id);
      return {
        ...post,
        isLiked: post.curtidoPeloUsuario,
        isFavorite: post.salvoPeloUsuario,
        isAnimating: false,
        isFavAnimating: false,
      };
    });
  }
  private listPostRecomendados(pageable?: Pageable): Observable<void> {
    const requestPageable = pageable || this.pageable;
    return this.postsService
      .listarPostsRecomendados({ pageable: requestPageable })
      .pipe(
        tap((response) => {
          const newPosts = this.processPosts([], response);
          this.postsRecomendados = [...this.postsRecomendados, ...newPosts];
          this.updatePagination(response);
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts recomendados:', error);
          return of(undefined);
        })
      );
  }
  private updatePagination(response: any): void {
    this.numberOfElements = response.numberOfElements || 0;
    this.totalPages = response.totalPages || 0;
    this.totalElements = response.totalElements || 0;
  }
  listPostsByLegenda(): void {
    if (!this.termo.trim()) return;

    this.loading = true;
    this.postsService
      .buscarPostsPorLegenda({
        termo: this.termo,
        pageable: {
          page: 0,
          size: 5,
          sort: [''],
        },
      })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (response) => {
          this.postsPorLegenda = this.processPosts(this.postsPorLegenda, response);
        },
        error: (error) => {
          console.error('Erro ao buscar posts por legenda:', error);
        }
      });
  }

  private listCelebritiesPost(): Observable<void> {
    return this.postsService
      .listarPostsPopulares({ pageable: this.pageable })
      .pipe(
        tap((response) => {
          this.celebrityPosts = this.processPosts(this.celebrityPosts, response);
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
          this.viralPosts = this.processPosts(this.viralPosts, response);
        }),
        map(() => undefined),
        catchError((error) => {
          console.error('Erro ao carregar posts virais:', error);
          return of(undefined);
        })
      );
  }

  private listFollowedPeoplePost(pageable?: Pageable): Observable<void> {
    const requestPageable = pageable || this.pageable;
    return this.postsService
      .listarPostsPopularesSeguidores({ pageable: requestPageable })
      .pipe(
        tap((response) => {
          const newPosts = this.processPosts([], response);
          this.followedPeoplePosts = [...this.followedPeoplePosts, ...newPosts];
          this.updatePagination(response);
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
