import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { TokenService } from '../services/token/token.service';
import { StoriesService } from '../services/services';
import { StorieCreateDto } from '../services/models';

@Component({
  selector: 'app-create-story-modal',
  imports: [CommonModule],
  templateUrl: './create-story-modal.component.html',
  styleUrl: './create-story-modal.component.css'
})
export class CreateStoryModalComponent {
  constructor(private tokenService: TokenService, private storyService: StoriesService){}
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Output() createStory = new EventEmitter<File[]>();

  selectedFiles: File[] = [];
  isDraggingOver = false;
  storyCreateDto: StorieCreateDto = {
    imagem: this.selectedFiles,
    destacar: false
  }
  @ViewChild('fileInput') fileInput!: ElementRef;

  changePhoto() {
    this.fileInput.nativeElement.click();
    this.fileInput.nativeElement.value = '';
  }

  criarStorie(){
    this.storyCreateDto.imagem = this.selectedFiles;
    this.storyService.criarStorie({
     body: this.storyCreateDto
    }).subscribe({
      next: () => {
        this.close.emit();
        this.createStory.emit(this.selectedFiles);
        this.selectedFiles = [];
      },
      error: (error) => {
        console.error(error);
      }
    })

  }
  onFilesSelected(event: any) {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFiles = [event.target.files[0]];
      this.storyCreateDto.imagem = this.selectedFiles;
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggingOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggingOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDraggingOver = false;


    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectedFiles = [event.dataTransfer.files[0]];
      this.storyCreateDto.imagem = this.selectedFiles;
    }
  }

  addFiles(files: FileList) {
    if (files.length > 0 && files[0].type.match('image.*')) {
      this.selectedFiles = [files[0]];
      this.storyCreateDto.imagem = this.selectedFiles;
    }
  }

  removeFile(index: number) {
    this.selectedFiles.splice(index, 1);
  }

  getPreview(file: File): string {
    return URL.createObjectURL(file);
  }


  closeModal() {
    this.selectedFiles = [];
    this.close.emit();
  }
}
