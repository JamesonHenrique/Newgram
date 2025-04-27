import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-story-modal',
  imports: [CommonModule],
  templateUrl: './story-modal.component.html',
  styleUrl: './story-modal.component.css'
})
export class StoryModalComponent {
  @Input() isOpen = false;
  @Input() storyTitle = '';
  @Input() storyAvatar = '';
  @Input() storyImages: string[] = [];

  @Output() close = new EventEmitter<void>();

  currentImageIndex = 0;
  progressValue = 0;
  private progressInterval: any;
  private timeout: any;
  private readonly STORY_DURATION = 5000; // 5 seconds per image

  ngOnInit() {
    this.startProgress();
  }

  ngOnDestroy() {
    this.clearTimers();
  }

  startProgress() {
    this.progressValue = 0;
    this.clearTimers();

    this.progressInterval = setInterval(() => {
      this.progressValue += 1;
      if (this.progressValue >= 100) {
        this.nextImage();
      }
    }, this.STORY_DURATION / 100);
  }

  nextImage() {
    if (this.currentImageIndex < this.storyImages.length - 1) {
      this.currentImageIndex++;
      this.startProgress();
    } else {
      this.closeModal();
    }
  }

  prevImage() {
    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
      this.startProgress();
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
    if (this.progressInterval) {
      clearInterval(this.progressInterval);
    }
    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  }
}
