import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PostsService } from '../services/services';
import { Pageable } from '../services/models';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { PaginationComponent } from '../pagination/pagination.component';
import { of, Subject } from 'rxjs';
import { takeUntil, catchError, finalize } from 'rxjs/operators';
import { RouterLink } from '@angular/router';
@Component({
  selector: 'app-favorite',
  imports: [CommonModule, PostDetailsComponent, PaginationComponent, RouterLink],
  templateUrl: './favorite.component.html',
  styleUrl: './favorite.component.css',
})
export class FavoriteComponent {
  private destroy$ = new Subject<void>();
  private loading = false;

  savedPosts: any[] = [];
  postSelected: any | null = null;
  selectedIndex: number | null = null;

  pageable: Pageable = {
    page: 0,
    size: 4,
    sort: [''],
  };

  postsTotais = 0;
  paginaTotal = 0;

  constructor(private title: Title, private postsService: PostsService) {
    this.title.setTitle('Favoritos');
  }

  ngOnInit(): void {
    this.listSavedPosts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  desfavoritar(post: any, event: Event) {
    event.stopPropagation();
    this.postsService
      .removerPostSalvo({ id: post.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.savedPosts = this.savedPosts.filter((p) => p.id !== post.id);
          this.postsTotais -= 1;

          if (this.savedPosts.length === 0 && (this.pageable.page || 0) > 0) {
            this.pageable.page = (this.pageable.page || 0) - 1;
            this.listSavedPosts();
          }
        },
        error: (err) => console.error('Erro ao remover favorito:', err),
      });
  }
  get isLoading(): boolean {
    return this.loading;
  }

  carregarMais(): void {
    if (this.isLastPage()) return;

    this.pageable.page = (this.pageable.page || 0) + 1;
    this.listSavedPosts();
  }

  onPageChange(newPage: number): void {
    if (newPage >= 0 && newPage < this.paginaTotal) {
      this.pageable.page = newPage;
      this.listSavedPosts();
    }
  }

  isLastPage(): boolean {
    return (this.pageable.page || 0) >= this.paginaTotal - 1;
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

  getFotoPerfil(user: any): string {
    return user?.fotoPerfil?.trim()
      ? `${user.fotoPerfil}`
      : '/icons/profile-placeholder.svg';
  }

  openModal(post: any, index: number): void {
    this.postSelected = post;
    this.selectedIndex = index;
  }

  openPostDetails(post: any, event: Event): void {
    event.preventDefault();
    this.postSelected = post;
  }

  private listSavedPosts(): void {
    this.loading = true;

    this.postsService
      .listarPostsSalvos({ pageable: this.pageable })
      .pipe(
        takeUntil(this.destroy$),
        catchError((error) => {
          console.error('Erro ao carregar posts salvos:', error);
          return of({
            content: [],
            totalElements: 0,
            totalPages: 0,
          });
        }),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (response: any) => {
          this.savedPosts = response.content || [];
          this.postsTotais = response.totalElements || 0;
          this.paginaTotal = response.totalPages || 0;
        },
      });
  }

  removeFromFavorites(post: any, index: number): void {
    if (!post.id) return;

    this.postsService
      .removerPostSalvo({ id: post.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.savedPosts = this.savedPosts.filter((p) => p.id !== post.id);
          this.postsTotais -= 1;

          if (this.savedPosts.length === 0 && (this.pageable.page || 0) > 0) {
            this.pageable.page = (this.pageable.page || 0) - 1;
            this.listSavedPosts();
          }
        },
        error: (err) => console.error('Erro ao remover favorito:', err),
      });
  }

  trackByPostId(index: number, post: any): number {
    return post.id;
  }
}
