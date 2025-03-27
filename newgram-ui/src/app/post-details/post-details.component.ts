import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, inject, Input, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-post-details',
  imports: [CommonModule],
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
  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.post = this.postService.getPostById(+postId);
      document.body.style.overflow = 'hidden';
    }
  }

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
