import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';
import { PostsService } from '../services/services';
import { PostDetailsComponent } from '../post-details/post-details.component';

@Component({
  selector: 'app-reels',
  standalone: true,
  imports: [CommonModule, PostDetailsComponent],
  templateUrl: './reels.component.html',
  styleUrl: './reels.component.css',
})
export class ReelsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  reels: any[] = [];
  postSelected: any | null = null;
  pagina = 0;
  temMais = true;
  carregando = false;

  constructor(private title: Title, private postsService: PostsService) {
    this.title.setTitle('Reels');
  }

  ngOnInit(): void {
    this.carregar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  carregar(): void {
    if (this.carregando || !this.temMais) {
      return;
    }
    this.carregando = true;
    this.postsService
      .listarReels({ pageable: { page: this.pagina, size: 5, sort: [''] } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          const itens = (page.content as any[] | undefined) || [];
          this.reels = [...this.reels, ...itens];
          const last = (page as any).last as boolean | undefined;
          this.temMais = last === undefined ? itens.length === 5 : !last;
          this.pagina += 1;
          this.carregando = false;
        },
        error: () => {
          this.carregando = false;
        },
      });
  }

  abrir(post: any): void {
    this.postSelected = post;
  }
}
