import { CommonModule } from '@angular/common';
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
import { FormatNumberPipe } from '../format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { Pageable, UsuarioSummaryDto } from '../services/models';
import { ComentariosService, UsuariosService } from '../services/services';
import { TokenService } from '../services/token/token.service';
import { CommentsComponent } from '../comments/comments.component';

@Component({
  selector: 'app-post-details',
  imports: [CommonModule, FormatNumberPipe, DateFormatPipe, CommentsComponent],
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

  isModalActive = false;
  post: any;
  postService: any;
  teste=false

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['postSelected']) {
      if (this.postSelected?.id) {
        console.log(this.postSelected)
        this.openModal();
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
      return 'data:image/jpg;base64,' + user.fotoPerfil;
    }
    return '/icons/profile-placeholder.svg';
  }
  getImagemPost(imagemBase64: string | null | undefined): string {
    if (imagemBase64 && imagemBase64.trim() !== '') {
      return 'data:image/jpg;base64,' + imagemBase64;
    }
    return '/icons/post-placeholder.svg';
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
      }, 300); // Tempo igual à duração da transição CSS
    }
  }
  closeModal(): void {
    const overlay = document.getElementById('postDetailOverlay');
    if (overlay) overlay.classList.remove('active');

    // Espera a animação terminar antes de limpar o post
    setTimeout(() => {
      this.isModalActive = false;
      this.postSelected = null;
      this.postSelectedChange.emit(null);
      document.body.style.overflow = '';
    }, 300); // Tempo igual à duração da transição CSS
  }

}
