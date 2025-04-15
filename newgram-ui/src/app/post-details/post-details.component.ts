import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  inject,
  Input,
  Output,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormatNumberPipe } from '../format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { UsuarioSummaryDto } from '../services/models';
import { UsuariosService } from '../services/services';
import { TokenService } from '../services/token/token.service';

@Component({
  selector: 'app-post-details',
  imports: [CommonModule, FormatNumberPipe, DateFormatPipe],
  templateUrl: './post-details.component.html',
  styleUrl: './post-details.component.css',
})
export class PostDetailsComponent {
  @Input() index: number = 1;
  @Input() postSelected: any;
  @Input() modalId: string = 'modal-1';

  @Input() item: any;

  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  post: any;
  postService: any;
  comentariosSelected: any[] = [];
  usuarioLogado: UsuarioSummaryDto = {} as UsuarioSummaryDto;
  usuariosService = inject(UsuariosService);
  tokenService = inject(TokenService);

  getFotoPerfil(): string {
    if (
      this.usuarioLogado?.fotoPerfil &&
      this.usuarioLogado.fotoPerfil.trim() !== ''
    ) {
      return 'data:image/jpg;base64,' + this.usuarioLogado.fotoPerfil;
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

  editPost(id: number): void {
    this.router.navigate(['/editar-post', id]);
  }
  formatNumber(count: any) {
    if (count >= 10000000) {
      return `${Math.floor(count / 1000000)}M`;
    } else if (count >= 1000000) {
      return `${(count / 1000000).toFixed(1)}M`;
    } else if (count >= 1000) {
      return `${Math.floor(count / 1000)}K`;
    } else {
      return count.toString();
    }
  }
  constructor(private route: ActivatedRoute, private router: Router) {
    this.findUsuarioLogado();
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
}
