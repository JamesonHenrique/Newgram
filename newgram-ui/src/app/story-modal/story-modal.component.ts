import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-story-modal',
  imports: [CommonModule],
  templateUrl: './story-modal.component.html',
  styleUrl: './story-modal.component.css'
})
export class StoryModalComponent implements OnChanges {
  @Input() isOpen = false;
  @Input() storyTitle = '';
  @Input() storyAvatar = '';
  @Input() storyImages: string[] = [];

  @Output() close = new EventEmitter<void>();

  currentImageIndex = 0;
  progressValue = 0;
  private progressInterval: any;
  private readonly STORY_DURATION = 5000; 
  private animationFrameId: number | null = null;
  private lastTimestamp: number = 0;
  private pauseStartTime: number = 0;
  private remainingTime: number = this.STORY_DURATION;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['isOpen'] && changes['isOpen'].currentValue) {
      this.resetProgress();
      this.startProgress();
    } else if (changes['isOpen'] && !changes['isOpen'].currentValue) {
      this.clearTimers();
    }
  }

  ngOnDestroy() {
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
