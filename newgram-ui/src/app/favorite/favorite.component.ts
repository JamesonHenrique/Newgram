import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PostsService } from '../services/services';
import { Pageable } from '../services/models';
import { PostDetailsComponent } from '../post-details/post-details.component';

@Component({
  selector: 'app-favorite',
  imports: [CommonModule, PostDetailsComponent],
  templateUrl: './favorite.component.html',
  styleUrl: './favorite.component.css'
})
export class FavoriteComponent {
  constructor(private title: Title, private postsService: PostsService) {
    this.title.setTitle('Favoritos');
  }
  savedPosts: any[] = [];
    pageable: Pageable = {
      page: 0,
      size: 10,
      sort: ['string'],
    };
  savedPostss = [
    {
      id: 1,
      author: 'Whindersson Nunes',
      username: '@whindersson',
      avatar: 'https://s2-oglobo.glbimg.com/LaW6NoqTlik3XAzENbU6WZrVLaI=/0x0:651x562/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_da025474c0c44edd99332dddb09cabe8/internal_photos/bs/2024/V/x/z1C221T4i0bNRAYNDNAA/whatsapp-image-2024-09-26-at-17.13.21.jpeg',
      image: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ2gIwo0mtbJnUYwJ9dqsWivE7MuBklwV8FVeII-lQrEfcfAnQk0nt10pN3RxbCrzWXnMM&usqp=CAU',
      text: 'Treino pesado hoje! Quem mais tá na luta? 💪 #Fitness',
      location: 'São Paulo',
      tags: ['Humor', 'Esportes', 'VidaSaudável'],
      likes: 1250000,
      comments: 32400,
      shares: 87200,
      time: '2h'
    },
    {
      id: 2,
      author: 'Paulo Muzy',
      username: '@paulomuzy',
      avatar: 'https://www.dialethoseventos.com.br/assets-custom/img/palestrantes/paulo-muzy-12072023-164852.jpg',
      image: 'https://i.ytimg.com/vi/6r1mxVMfY1o/maxresdefault.jpg',
      text: 'Dica de nutrição pós-treino que mudou meus resultados!',
      location: 'São Rio de Janeiro',
      tags: ['Fitness', 'Nutrição', 'Saúde'],
      likes: 890000,
      comments: 15400,
      shares: 32100,
      time: '5h'
    },
    {
      id: 3,
      author: 'Gustavo Lima',
      username: '@gusttavo_lima',
      avatar: 'https://www.cnnbrasil.com.br/wp-content/uploads/sites/12/2024/09/imagem-29-1.jpg?w=1200&h=1200&crop=1',
      image: 'https://uploads.maisgoias.com.br/2024/05/168ce51f-que-calor-7.jpg',
      text: 'Turnê "Buteco do Gusttavo" esgotada em 3 cidades! Obrigado, fãs! 🎶',
      location: 'Pernambuco',
      tags: ['Sertanejo', 'Show', 'Música'],
      likes: 2100000,
      comments: 45000,
      shares: 92000,
      time: '4h'
    }
  ];
  postSelected: any;
  selectedIndex: any;
  ngOnInit(): void {
    this.listSavedPosts();
  }
  openModal(post: any, index: number) {
    this.postSelected = post;
    this.selectedIndex = index;


  }
  openPostDetails(post: any, event: Event) {
    this.postSelected = post;
  }

  listSavedPosts() {
    this.postsService.listarPostsSalvos(
      {
        pageable: this.pageable
      }
    ).subscribe((response) => {
      this.savedPosts = response.content || [];
    });
  }
}
