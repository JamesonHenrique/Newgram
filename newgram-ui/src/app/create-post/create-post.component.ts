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

@Component({
  selector: 'app-create-post',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.css',
})
export class CreatePostComponent {
  constructor(private title: Title, private sanitizer: DomSanitizer) {
    this.title.setTitle('Criar post');
  }
  postForm!: FormGroup;
  popularTags = [
    {
      name: '#fotografia',
    },
    {
      name: '#viagem',
    },
    {
      name: '#natureza',
    },
    {
      name: '#comida',
    },
    {
      name: '#arte',
    },
    {
      name: '#música',
    },
    {
      name: '#tecnologia',
    },
  ];


  ngOnInit(): void {
    this.postForm = new FormGroup({
      legenda: new FormControl('', Validators.required),
      localizacao: new FormControl('', Validators.required),
      hashtags: new FormControl('', Validators.required),
    });
    this.setupDragAndDrop();

  }
  @ViewChild('imageUpload') imageUpload!: ElementRef<HTMLInputElement>;
  @ViewChild('imagePreview') imagePreview!: ElementRef<HTMLImageElement>;
  @ViewChild('uploadPlaceholder') uploadPlaceholder!: ElementRef<HTMLDivElement>;

  selectedFoto: string | ArrayBuffer | null = '';


  triggerFileInput(): void {
    this.imageUpload.nativeElement.click();
  }


  previewSelectedImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];


      if (file.size > 10 * 1024 * 1024) {
        alert('O arquivo é muito grande. Por favor, selecione uma imagem menor que 10MB.');
        return;
      }


      if (!file.type.match('image.*')) {
        alert('Por favor, selecione apenas imagens (JPG, PNG ou GIF).');
        return;
      }

      const reader = new FileReader();

      reader.onload = (e) => {
        this.selectedFoto = e.target?.result as string;

        this.uploadPlaceholder.nativeElement.style.display = 'none';
        this.imagePreview.nativeElement.style.display = 'block';
      };

      reader.readAsDataURL(file);
    }
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
