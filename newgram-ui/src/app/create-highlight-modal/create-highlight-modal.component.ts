import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
@Component({
  selector: 'app-create-highlight-modal',
  imports: [FormsModule, CommonModule],
  templateUrl: './create-highlight-modal.component.html',
  styleUrl: './create-highlight-modal.component.css'
})
export class CreateHighlightModalComponent {
  @Input() isOpen = false;
  @Input() userStories: any[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() highlightCreated = new EventEmitter<any>();

  currentStep = 1;
  selectedStories: number[] = [];
  selectedCover: string | ArrayBuffer | null = null;
  highlightName = '';

  get availableStories() {
    return this.userStories.filter(story => !story.inHighlight);
  }

  toggleStory(storyId: number) {
    if (this.selectedStories.includes(storyId)) {
      this.selectedStories = this.selectedStories.filter(id => id !== storyId);
    } else {
      if (this.selectedStories.length < 5) {
        this.selectedStories.push(storyId);
      }
    }
    this.updateCreateButtonState();
  }

  handleFileUpload(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedCover = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }

  nextStep() {
    if (this.currentStep === 1 && this.validateStep1()) {
      this.currentStep = 2;
    }
  }

  prevStep() {
    this.currentStep = 1;
  }

  validateStep1() {
    return this.highlightName.trim() !== '' && this.selectedCover !== null;
  }

  createHighlight() {
    const newHighlight = {
      name: this.highlightName,
      cover: this.selectedCover,
      stories: this.selectedStories
    };

    this.highlightCreated.emit(newHighlight);
    this.resetModal();
  }

  private resetModal() {
    this.currentStep = 1;
    this.highlightName = '';
    this.selectedCover = null;
    this.selectedStories = [];
  }

  private updateCreateButtonState() {
    // Lógica adicional se necessário
  }

  closeModal() {
    this.close.emit();
    this.resetModal();
  }
}
