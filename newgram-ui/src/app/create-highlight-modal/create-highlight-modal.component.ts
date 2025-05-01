import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { DestaquesService, StoriesService } from '../services/services';
import { TokenService } from '../services/token/token.service';
import { DestaqueCreateDto } from '../services/models';

@Component({
  selector: 'app-create-highlight-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-highlight-modal.component.html',
  styleUrls: ['./create-highlight-modal.component.css'],
})
export class CreateHighlightModalComponent {
  private destroy$ = new Subject<void>();

  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() createHighlight = new EventEmitter<{
    name: string;
    selectedStories: string[];
    coverImage?: File | string;
  }>();

  nome = '';
  selectedStories: string[] = [];
  coverImage?: File | { id: string; url: string } | null = null;
  coverPreviewUrl: string | null = null;
  showCoverOptions = false;
  destaqueCreateDto: DestaqueCreateDto = {
    nome: '',
    storiesIds: [],
    capaDeDestaque: undefined,
  };
  availableStories: any = [];

  constructor(
    private storiesService: StoriesService,
    private tokenService: TokenService,
    private destaqueService: DestaquesService
  ) {}

  ngOnInit(): void {
    this.findAllStoriesByUserId();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }


  async urlToFile(
    url: string,
    filename: string,
    mimeType: string
  ): Promise<File> {
    const response = await fetch(url);
    const blob = await response.blob();
    return new File([blob], filename, { type: mimeType });
  }
  async criarDestaque() {
    let coverFile: File | undefined;

    if (
      this.coverImage &&
      typeof this.coverImage === 'object' &&
      'url' in this.coverImage
    ) {
      try {
        const urlParts = this.coverImage.url.split('/');
        const filename =
          urlParts[urlParts.length - 1].split('?')[0] || 'cover.jpg';
        coverFile = await this.urlToFile(
          this.coverImage.url,
          filename,
          'image/jpeg'
        );
      } catch (error) {
        console.error('Erro ao converter URL para File:', error);
      }
    } else if (this.coverImage instanceof File) {
      coverFile = this.coverImage;
    }

    this.destaqueCreateDto = {
      nome: this.nome,
      storiesIds: this.selectedStories.map((id) => Number(id)),
      capaDeDestaque: coverFile,
    };

    this.destaqueService
      .criarDestaque({
        body: this.destaqueCreateDto,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (destaque) => {
          this.createHighlight.emit({
            name: this.nome,
            selectedStories: this.selectedStories,
            coverImage: coverFile || this.selectedStories[0],
          });
          this.closeModal();
        },
        error: (error) => {
          console.error('Erro ao criar destaque:', error);
          if (error.status === 400) {
            console.log('Erros de validação:', error.error.errors);
          }
        },
      });
  }

  findAllStoriesByUserId() {
    this.storiesService
      .listarStoriesDoUsuario({ autorId: this.tokenService.userId })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (stories) => {
          this.availableStories = stories;
        },
        error: (error) => {
          console.error('Erro ao buscar stories:', error);
        },
      });
  }
  getCoverImageUrl(): string | null {
    if (this.coverPreviewUrl) {
      return this.coverPreviewUrl;
    }
    if (!this.coverImage || this.coverImage === null) {
      return '/icons/post-placeholder.svg';
    }

    if (this.selectedStories.length > 0) {
      const firstStory = this.availableStories.find(
        (s: any) => s.id === this.selectedStories[0]
      );
      return firstStory?.storieImagemUrl || null;
    }

    return null;
  }
  toggleStorySelection(storyId: string) {
    if (this.isStorySelected(storyId)) {
      this.selectedStories = this.selectedStories.filter(
        (id) => id !== storyId
      );

      if (
        this.isStoryCover(this.coverImage) &&
        this.coverImage.id === storyId
      ) {
        this.coverImage = null;
        this.coverPreviewUrl = null;
      }
    } else {
      this.selectedStories = [...this.selectedStories, storyId];
    }
  }
  private isStoryCover(cover: any): cover is { id: string; url: string } {
    return (
      cover && typeof cover === 'object' && 'id' in cover && 'url' in cover
    );
  }
  isStorySelected(storyId: string): boolean {
    return this.selectedStories.includes(storyId);
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.coverImage = input.files[0];
      this.generateCoverPreview(this.coverImage);
      this.showCoverOptions = false;
    }
  }

  setCoverFromStory(storyId: string) {
    const story = this.availableStories.find((s: any) => s.id === storyId);
    if (story) {
      this.coverImage = {
        id: storyId,
        url: story.storieImagemUrl,
      };
      this.coverPreviewUrl = story.storieImagemUrl;
      this.showCoverOptions = false;
    }
  }
  private generateCoverPreview(file: File) {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.coverPreviewUrl = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  toggleCoverOptions() {
    this.showCoverOptions = !this.showCoverOptions;
  }

  closeModal() {
    this.nome = '';
    this.selectedStories = [];
    this.coverImage = null;
    this.coverPreviewUrl = null;
    this.showCoverOptions = false;
    this.close.emit();
  }
}
