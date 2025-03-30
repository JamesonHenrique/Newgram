import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-create-post',
  imports: [CommonModule, FormsModule,ReactiveFormsModule],
  templateUrl: './create-post.component.html',
  styleUrl: './create-post.component.css',
})
export class CreatePostComponent {
  constructor(private title:Title) {
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
      image: new FormControl('', Validators.required),
      caption: new FormControl('', Validators.required),
      location: new FormControl('', Validators.required),
      tags: new FormControl('', Validators.required),
    });
  }
}
