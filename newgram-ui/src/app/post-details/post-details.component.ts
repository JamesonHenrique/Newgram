import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, inject, Input, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormatNumberPipe } from '../format-number.pipe';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';

@Component({
  selector: 'app-post-details',
  imports: [CommonModule, FormatNumberPipe, DateFormatPipe],
  templateUrl: './post-details.component.html',
  styleUrl: './post-details.component.css',
})
export class PostDetailsComponent {
  @Input() index: number = 1;
  @Input() postSelected: any;
  @Input() modalId: string = 'modal-1';

 @Input() item: any;


  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  post: any;
  postService: any;
  editPost(id: number): void {
    this.router.navigate(['/editar-post', id]);
  }
  formatNumber(count:any) {
    if (count >= 10000000) {
      return `${Math.floor(count / 1000000)}M`;
    } else if (count >= 1000000) {
      return `${(count / 1000000).toFixed(1)}M`;
    } else if (count >= 1000) {
      return `${Math.floor(count / 1000)}K`;
    } else {
      return count.toString();
    }
  }
  constructor(private route: ActivatedRoute, private router: Router) {}



  ngOnDestroy(): void {
    document.body.style.overflow = '';
  }

  closePost(): void {
    window.history.back();
  }

  getTags(text: string): string[] {
    return text
      .split('#')
      .slice(1)
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
  }
}
