import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';
import { AutenticacaoService } from '../../services/services';

@Component({
  selector: 'app-verificar-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="auth-container">
      <h1 class="h3-bold">Verificação de e-mail</h1>
      <p class="small-medium text-light-3" *ngIf="estado === 'carregando'">Confirmando...</p>
      <p class="small-medium text-green-500" *ngIf="estado === 'ok'">
        E-mail verificado com sucesso.
        <a routerLink="/login" class="text-primary-500">Entrar</a>
      </p>
      <p class="small-medium text-red-500" *ngIf="estado === 'erro'">
        Link inválido ou expirado. Peça um novo link no seu perfil.
      </p>
    </div>
  `,
})
export class VerificarEmailComponent implements OnInit {
  private destroy$ = new Subject<void>();
  estado: 'carregando' | 'ok' | 'erro' = 'carregando';

  constructor(
    private route: ActivatedRoute,
    private authService: AutenticacaoService,
    private title: Title
  ) {
    this.title.setTitle('Verificar e-mail');
  }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token') || '';
    if (!token) {
      this.estado = 'erro';
      return;
    }
    this.authService
      .verificarEmail({ token })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => (this.estado = 'ok'),
        error: () => (this.estado = 'erro'),
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
