import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Subject, interval, takeUntil } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ConversasService } from '../services/services';
import { ConversaResponseDto, MensagemResponseDto } from '../services/models';
import { TokenService } from '../services/token/token.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-mensagens',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mensagens.component.html',
  styleUrl: './mensagens.component.css',
})
export class MensagensComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private stomp: Client | null = null;
  private inscricaoTopico: StompSubscription | null = null;

  conversas: ConversaResponseDto[] = [];
  conversaAtiva: ConversaResponseDto | null = null;
  mensagens: MensagemResponseDto[] = [];
  novoTexto = '';
  enviando = false;

  constructor(
    private title: Title,
    private conversasService: ConversasService,
    private tokenService: TokenService
  ) {
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
    this.desconectarTopico();
    this.stomp?.deactivate();
  }

  /** Tempo real via STOMP; polling de 10s continua como fallback. */
  private conectarTopico(conversaId: number): void {
    this.desconectarTopico();
    if (!this.stomp) {
      this.stomp = new Client({
        webSocketFactory: () => new SockJS(`${environment.apiUrl}/ws`),
        connectHeaders: { Authorization: `Bearer ${this.tokenService.token}` },
        reconnectDelay: 5000,
      });
      this.stomp.activate();
    }
    const assinar = () => {
      if (!this.stomp?.connected) {
        setTimeout(assinar, 1000);
        return;
      }
      this.inscricaoTopico = this.stomp.subscribe(`/topic/conversas.${conversaId}`, (msg) => {
        try {
          const mensagem = JSON.parse(msg.body) as MensagemResponseDto;
          if (!this.mensagens.some((m) => m.id === mensagem.id)) {
            this.mensagens = [...this.mensagens, mensagem];
          }
        } catch {
          this.carregarMensagens(conversaId, false);
        }
      });
    };
    assinar();
  }

  private desconectarTopico(): void {
    try {
      this.inscricaoTopico?.unsubscribe();
    } catch {
      // ignora: socket pode já estar fechado
    }
    this.inscricaoTopico = null;
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
      this.conectarTopico(conversa.id);
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
