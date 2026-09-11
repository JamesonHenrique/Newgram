import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { ConversasService, EnquetesService } from '../services/services';

@Component({
  selector: 'app-story-modal',
  imports: [CommonModule, FormsModule],
  templateUrl: './story-modal.component.html',
  styleUrl: './story-modal.component.css'
})
export class StoryModalComponent implements OnChanges, OnDestroy {
  @Input() isOpen = false;
  @Input() storyTitle = '';
  @Input() storyAvatar = '';
  @Input() storyImages: string[] = [];
  @Input() storyIds: number[] = [];
  @Input() stories: any[] = [];
  @Input() autorId: number | null = null;
  @Input() viewerId: number | null = null;

  @Output() close = new EventEmitter<void>();

  private destroy$ = new Subject<void>();
  respostaTexto = '';
  enviandoResposta = false;
  reacoes = ['❤️', '😂', '😮', '😢', '👏', '🔥'];

  currentImageIndex = 0;
  progressValue = 0;
  private progressInterval: any;
  private readonly STORY_DURATION = 5000;
  private animationFrameId: number | null = null;
  private lastTimestamp: number = 0;
  private pauseStartTime = 0;
  private remainingTime: number = this.STORY_DURATION;

  constructor(
    private conversasService: ConversasService,
    private enquetesService: EnquetesService
  ) {}

  /** Resposta/reação só para stories de outro usuário com IDs conhecidos. */
  get podeInteragir(): boolean {
    return !!this.autorId && this.autorId !== this.viewerId;
  }

  /** Enquete do story exibido agora (se houver). */
  get enqueteAtual(): any | null {
    if (!this.stories?.length) {
      return null;
    }
    const item = this.stories[Math.min(this.currentImageIndex, this.stories.length - 1)];
    return item?.enquete ?? null;
  }

  votarEnqueteStory(opcao: any, event: Event): void {
    event.stopPropagation();
    const enquete = this.enqueteAtual;
    if (!enquete?.id || !opcao?.id || enquete.minhaOpcaoId || enquete.encerrada) {
      return;
    }
    this.enquetesService
      .votarEnquete({ id: enquete.id, body: { opcaoId: opcao.id } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (atualizada) => {
          const item = this.stories[Math.min(this.currentImageIndex, this.stories.length - 1)];
          if (item) {
            item.enquete = atualizada;
          }
        },
        error: () => {},
      });
  }

  private storyAtualId(): number | null {
    if (this.storyIds?.length) {
      return this.storyIds[Math.min(this.currentImageIndex, this.storyIds.length - 1)] ?? null;
    }
    return null;
  }

  enviarViaDm(texto: string): void {
    const conteudo = texto.trim();
    if (!conteudo || !this.autorId || this.enviandoResposta) {
      return;
    }
    this.enviandoResposta = true;
    const id = this.storyAtualId();
    const mensagem = id ? `${conteudo} (story #${id})` : conteudo;
    this.conversasService
      .iniciarConversa({ usuarioId: this.autorId })
      .pipe(
        switchMap((conversa) =>
          this.conversasService.enviarMensagem({ id: conversa.id as number, body: { texto: mensagem } })
        ),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => {
          this.respostaTexto = '';
          this.enviandoResposta = false;
        },
        error: () => {
          this.enviandoResposta = false;
        },
      });
  }

  enviarResposta(): void {
    this.enviarViaDm(this.respostaTexto);
  }

  enviarReacao(emoji: string): void {
    this.enviarViaDm(emoji);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen'] && changes['isOpen'].currentValue) {
      this.resetProgress();
      this.startProgress();
    } else if (changes['isOpen'] && !changes['isOpen'].currentValue) {
      this.clearTimers();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.clearTimers();
  }

  resetProgress() {
    this.currentImageIndex = 0;
    this.progressValue = 0;
    this.remainingTime = this.STORY_DURATION;
    this.clearTimers();
  }

  startProgress() {
    this.clearTimers();
    this.lastTimestamp = performance.now();
    this.animateProgress();
  }

  private animateProgress() {
    this.animationFrameId = requestAnimationFrame((timestamp) => {
      const delta = timestamp - this.lastTimestamp;
      this.lastTimestamp = timestamp;

      if (this.remainingTime > 0) {
        this.progressValue = 100 - (this.remainingTime / this.STORY_DURATION) * 100;
        this.remainingTime -= delta;
        this.animateProgress();
      } else {
        this.nextImage();
      }
    });
  }

  nextImage() {
    if (this.currentImageIndex < this.storyImages.length - 1) {
      this.currentImageIndex++;
      this.remainingTime = this.STORY_DURATION;
      this.startProgress();
    } else {
      this.closeModal();
    }
  }

  prevImage() {
    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
      this.remainingTime = this.STORY_DURATION;
      this.startProgress();
    }
  }

  pauseProgress() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
  }

  resumeProgress() {
    if (!this.animationFrameId) {
      this.lastTimestamp = performance.now();
      this.animateProgress();
    }
  }

  closeModal() {
    this.clearTimers();
    this.close.emit();
  }

  onOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('story-modal')) {
      this.closeModal();
    }
  }

  private clearTimers() {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
    if (this.progressInterval) {
      clearInterval(this.progressInterval);
      this.progressInterval = null;
    }
  }
}
