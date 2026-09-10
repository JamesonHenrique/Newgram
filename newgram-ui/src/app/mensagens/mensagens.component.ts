import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Subject, interval, takeUntil } from 'rxjs';
import { ConversasService } from '../services/services';
import { ConversaResponseDto, MensagemResponseDto } from '../services/models';

@Component({
  selector: 'app-mensagens',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mensagens.component.html',
  styleUrl: './mensagens.component.css',
})
export class MensagensComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  conversas: ConversaResponseDto[] = [];
  conversaAtiva: ConversaResponseDto | null = null;
  mensagens: MensagemResponseDto[] = [];
  novoTexto = '';
  enviando = false;

  constructor(private title: Title, private conversasService: ConversasService) {
    this.title.setTitle('Mensagens');
  }

  ngOnInit(): void {
    this.carregarConversas();
    // Polling simples a cada 10s (sem WebSocket).
    interval(10000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.carregarConversas(false);
        if (this.conversaAtiva?.id) {
          this.carregarMensagens(this.conversaAtiva.id, false);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  carregarConversas(recarregarAtiva = true): void {
    this.conversasService
      .listarConversas({ pageable: { page: 0, size: 20, sort: [''] } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.conversas = (page.content as ConversaResponseDto[] | undefined) || [];
          if (recarregarAtiva && this.conversaAtiva?.id) {
            const atualizada = this.conversas.find((c) => c.id === this.conversaAtiva?.id);
            if (atualizada) {
              this.conversaAtiva = atualizada;
            }
          }
        },
        error: () => {},
      });
  }

  abrirConversa(conversa: ConversaResponseDto): void {
    this.conversaAtiva = conversa;
    this.mensagens = [];
    if (conversa.id) {
      this.carregarMensagens(conversa.id);
    }
  }

  carregarMensagens(conversaId: number, limpar = true): void {
    if (limpar) {
      this.mensagens = [];
    }
    this.conversasService
      .listarMensagens({ id: conversaId, pageable: { page: 0, size: 50, sort: [''] } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.mensagens = (page.content as MensagemResponseDto[] | undefined) || [];
        },
        error: () => {},
      });
  }

  enviar(): void {
    const texto = this.novoTexto.trim();
    if (!texto || !this.conversaAtiva?.id || this.enviando) {
      return;
    }
    this.enviando = true;
    this.conversasService
      .enviarMensagem({ id: this.conversaAtiva.id, body: { texto } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (mensagem) => {
          this.mensagens = [...this.mensagens, mensagem];
          this.novoTexto = '';
          this.enviando = false;
          this.carregarConversas();
        },
        error: () => {
          this.enviando = false;
        },
      });
  }
}
