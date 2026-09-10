import { TokenService } from './../services/token/token.service';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { DomSanitizer, SafeUrl, Title } from '@angular/platform-browser';
import { HashtagsService, PostsService } from '../services/services';
import { catchError, of, switchMap } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Pageable } from '../services/models';

@Component({
  selector: 'app-create-post',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.css',
})
export class CreatePostComponent {
  constructor(
    private title: Title,
    private sanitizer: DomSanitizer,
    private postService: PostsService,
    private router: Router,
    private tokenService: TokenService,
    private route: ActivatedRoute,
    private hashtagService: HashtagsService
  ) {
    if (this.postId) {
      this.title.setTitle('Editar post');
    } else {
      this.title.setTitle('Criar post');
    }
  }

  @ViewChild('imageUpload') imageUpload!: ElementRef<HTMLInputElement>;
  @ViewChild('imagePreview') imagePreview!: ElementRef<HTMLImageElement>;
  @ViewChild('uploadPlaceholder')
  uploadPlaceholder!: ElementRef<HTMLDivElement>;
  selectedFoto: any;
  postForm!: FormGroup;
  selectedTags: string[] = [];
  postId: number | null = null;
  popularTags:any = [];
  page: Pageable = {
    page: 0,
    size: 7,
    sort: ['']
  };
  ngOnInit(): void {
    this.postId = this.route.snapshot.params['id'];

    this.postForm = new FormGroup({
      legenda: new FormControl('', Validators.required),
      localizacao: new FormControl('', Validators.required),
      hashtags: new FormControl([], Validators.required),
      visibilidade: new FormControl('PUBLICO', Validators.required),
    });
    if (this.postId) {
      this.loadPostData(this.postId);
    }
    this.setupDragAndDrop();
    this.setupTagInput();
    this.findPopularTags();
  }

  loadPostData(postId: number): void {
    this.postService.buscarPorId({
      id: postId,
    }).subscribe({
      next: (post) => {
        this.postForm.patchValue({
          legenda: post.legenda,
          localizacao: post.localizacao,
          visibilidade: post.visibilidade
        });


        if (post.hashtags) {
          this.selectedTags = post.hashtags.map(tag => tag.trim());
          this.updateTagsInput();
          this.renderSelectedTags();
        }


      },
      error: (err) => {
        console.error('Erro ao carregar post:', err);
        this.router.navigate(['/erro']);
      }
    });
  }
  findPopularTags(): void {
    this.hashtagService.listarHashtagsPopulares({
      pageable: this.page
    }).subscribe({
      next: (tags) => {
        this.popularTags = tags.content || [];
      },
      error: (err) => {
        console.error('Erro ao buscar tags populares:', err);
      }
    });
  }
  addPopularTag(tagName: string): void {
    const normalizedTag = this.normalizeTag(tagName);

    const tagExists = this.selectedTags.some(
      (tag) => this.normalizeTag(tag) === normalizedTag
    );

    if (!tagExists) {
      this.selectedTags.push(`#${normalizedTag}`);
      this.updateTagsInput();
      this.renderSelectedTags();
    }
  }

  private normalizeTag(tag: string): string {
    return tag.trim().toLowerCase().replace(/^#+/, '');
  }

  removeTag(tagToRemove: string): void {
    const normalizedToRemove = this.normalizeTag(tagToRemove);
    this.selectedTags = this.selectedTags.filter(
      (tag) => this.normalizeTag(tag) !== normalizedToRemove
    );
    this.updateTagsInput();
    this.renderSelectedTags();
  }
  private updateTagsInput(): void {
    this.postForm.get('hashtags')?.setValue(this.selectedTags);
  }

  private renderSelectedTags(): void {
    const container = document.getElementById('tagsContainer');
    if (container) {
      container.innerHTML = '';
      this.selectedTags.forEach((tag) => {
        const cleanedTag = tag.startsWith('#') ? tag.substring(1) : tag;
        const tagElement = document.createElement('span');
        tagElement.className = `inline-flex items-center px-3 py-1 rounded-full text-sm font-medium
                              bg-primary-500 text-white
                              transition-all hover:bg-primary-600
                              animate-fade-in`;

        tagElement.innerHTML = `
          #${cleanedTag}
          <button class="ml-2 focus:outline-none hover:scale-125 transition-transform">
            <i class="fas fa-times text-xs"></i>
          </button>
        `;

        tagElement.querySelector('button')?.addEventListener('click', (e) => {
          e.stopPropagation();
          this.removeTag(tag);
        });

        container.appendChild(tagElement);
      });
    }
  }
  private setupTagInput(): void {
    const tagInput = document.getElementById('tagInput');

    if (tagInput) {
      tagInput.addEventListener('keydown', (event: KeyboardEvent) => {
        if (event.key === 'Enter') {
          event.preventDefault();
          const input = event.target as HTMLInputElement;
          const inputValue = input.value.trim();

          if (inputValue) {
            const tags = inputValue
              .split(',')
              .map((tag) => tag.trim())
              .filter((tag) => tag.length > 0);

            tags.forEach((tag) => {
              const normalizedTag = tag.startsWith('#')
                ? tag.substring(1)
                : tag;
              const finalTag = `#${normalizedTag}`;

              if (!this.selectedTags.includes(finalTag)) {
                this.selectedTags.push(finalTag);
              }
            });

            this.updateTagsInput();
            this.renderSelectedTags();
            input.value = '';
          }
        }
      });
    }
  }

  previewSelectedImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      if (file.size > 10 * 1024 * 1024) {
        alert(
          'O arquivo é muito grande. Por favor, selecione uma imagem menor que 10MB.'
        );
        return;
      }

      if (!file.type.match('image.*')) {
        alert('Por favor, selecione apenas imagens (JPG, PNG ou GIF).');
        return;
      }
      this.selectedFoto = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imagePreview.nativeElement.src = e.target?.result as string;
        this.uploadPlaceholder.nativeElement.style.display = 'none';
        this.imagePreview.nativeElement.style.display = 'block';
      };
      reader.readAsDataURL(file);
    }
  }

  savePost() {
    const postRequest = { ...this.postForm.value };
    this.postService
      .criarPost({ body: postRequest })
      .pipe(
        switchMap((response: any) => {
          if (this.selectedFoto) {
            const formData = new FormData();
            formData.append('file', this.selectedFoto);
            return this.postService
              .uploadImagemPost({
                postId: response.id,
                body: formData,
              })
              .pipe(
                catchError((error) => {
                  console.error('Erro ao enviar foto:', error);
                  return of(response);
                })
              );
          }
          return of(response);
        })
      )
      .subscribe({
        next: (response) => {
          this.postForm.reset();
          this.selectedTags = [];
          this.selectedFoto = null;
          this.imagePreview.nativeElement.style.display = 'none';
          this.uploadPlaceholder.nativeElement.style.display = 'block';
          const userId = this.tokenService.userId;
          this.router.navigate([`/perfis/${userId}`]);
        },
        error: (error) => {
          console.error('Erro ao criar post:', error);
        },
      });
  }
  onSubmit() {
    this.savePost();
  }
  triggerFileInput(): void {
    this.imageUpload.nativeElement.click();
  }

  setupDragAndDrop(): void {
    const container = document.getElementById('imageUploadContainer');

    if (container) {
      container.addEventListener('dragover', (e) => {
        e.preventDefault();
        container.classList.add('dragover');
      });

      container.addEventListener('dragleave', () => {
        container.classList.remove('dragover');
      });

      container.addEventListener('drop', (e) => {
        e.preventDefault();
        container.classList.remove('dragover');

        if (e.dataTransfer?.files && e.dataTransfer.files[0]) {
          this.imageUpload.nativeElement.files = e.dataTransfer.files;
          const event = new Event('change');
          this.imageUpload.nativeElement.dispatchEvent(event);
        }
      });
    }
  }
}
