import { Pageable } from './../services/models/pageable';
import { PostsService } from './../services/services/posts.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { Title } from '@angular/platform-browser';
import { FormatNumberPipe } from '../format-number.pipe';
import { DateFormatPipe } from "../services/pipes/date-format-pipe";
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-explore',
  imports: [CommonModule, PostDetailsComponent, FormatNumberPipe, DateFormatPipe, FormsModule],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.css'
})
export class ExploreComponent {
  constructor(private title:Title, private postsService: PostsService) {
    this.title.setTitle('Explore');
  }
  postSelected: any = null;
  selectedIndex: any = null;
  selectedPost: any = null;
  selectedPostType: string = '';
  pageable: Pageable = {
    page: 0,
    size: 3,
    sort: ['string'],
  };
  celebrityPosts: any[] = [];
  viralPosts: any[] = [];
  followedPeoplePosts: any[] = [];

  viralPostss = [
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

celebrityPostss = [
  {
    id: 1,
    author: 'Ivete Sangalo',
    username: '@ivetesangalo',
    avatar: 'https://cdn-images.dzcdn.net/images/artist/fb27c1806a4b63a9633da56f57ca5fd0/1900x1900-000000-80-0-0.jpg',
    image: 'https://i.ytimg.com/vi/13MqkelD09E/maxresdefault.jpg',
    text: 'Preparem-se para o Carnaval 2024! #Axé #Bahia',
    location: 'Bahia',
    tags: ['Música', 'Carnaval', 'Show'],
    likes: 3200000,
    comments: 42800,
    shares: 156000,
    time: '1d'
  },
  {
    id: 2,
    author: 'Rodrigo Faro',
    username: '@rodrigofaro',
    avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRF7C6VEkzSCfwkduTPL5CYmII-kAIKJvIX2Q&s',
    image: 'https://ofuxico.com.br/wp-content/uploads/2024/12/rodrigo-faro-1.jpg',
    text: 'Domingo em família é tudo de bom! ❤️',
    location: 'Rio de Janeiro',
    tags: ['Família', 'Domingão'],
    likes: 1870000,
    comments: 38200,
    shares: 45600,
    time: '1d'
  },
  {
    id: 3,
    author: 'Xororó',
    username: '@xororooficial',
    avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTnXJyDrgbacogHWuyraAHsspig6BtHFEWcRQ&s',
    image: 'https://imagens.ne10.uol.com.br/veiculos/_midias/jpg/2022/05/31/615x300/1_chitaozinhoexororo-21133146.jpg',
    text: 'Sertanejo a noite toda! 🎶',
    location: 'Rio Grande do Sul',
    tags: ['Música', 'Sertanejo', 'Tradição'],
    likes: 950000,
    comments: 18000,
    shares: 25000,
    time: '1d'
  }
];

followedPeoplePostss = [
  {
    id: 1,
    author: 'Sabrina Sato',
    username: '@sabrinasato',
    avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQqrHq1mkNqRoi8iRLAHNddoOnse6PDpu7RIw&s',
    image: 'https://s2-marieclaire.glbimg.com/yzsSkSQNIi1_ZqVIaJsEKTvGQYc=/0x0:4088x2300/888x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_51f0194726ca4cae994c33379977582d/internal_photos/bs/2025/y/W/uBn54gTHCUKIexhEAe3Q/befunky-collage-58-.jpg',
    text: 'Vistas incríveis de Gramado! Quem já veio?',
    location: 'Rio Grande do Norte',
    tags: [' #Viagem', ' #Turismo', ' #RioGrandeDoSul'],
    likes: 456000,
    comments: 12800,
    shares: 9800,
    time: '3h'
  },
  {
    id: 2,
    author: 'Marcos Mion',
    username: '@marcosmion',
    avatar: 'https://upload.wikimedia.org/wikipedia/commons/b/b1/Marcos_Mion_2021.jpg',
    image: 'https://anotabahia.com/wp-content/uploads/2025/01/anotabahia-marcos-mion-fala-sobre-relacao-do-filme-mma-meu-melhor-amigo-com-sua-paternidade-whatsapp-image-2025-01-09-at-20.00.57-1200x900.jpeg',
    text: 'Noite incrível no Prêmio Multishow!',
    location: 'Espirito Santo',
    tags: [' #Evento', ' #TV', ' #Entretenimento'],
    likes: 678000,
    comments: 21500,
    shares: 12400,
    time: '8h'
  },
  {
    id: 3,
    author: 'Sandy',
    username: '@sandy',
    avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTVxidCuln_ATp1mgP8usIEK0A6xhJopGo4pg&s',
    image: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRdqKgjmu6iM2Eglf3CCiB2vLnNmlvWiuBWSQ&s',
    text: 'Novo single saindo semana que vem! Preparem-se 🎤',
    location: 'Minas Gerais',
    tags: [' #Música', ' #Lançamento', ' #MPB'],
    likes: 870000,
    comments: 22000,
    shares: 31000,
    time: '8h'
  }
];
popularTags = [
  'Humor',
  'Esportes',
  'VidaSaudável',
  'Música',
  'Carnaval',
  'Show',
  'Família',
  'Domingão',
  'Viagem',
  'Turismo',
  'RioGrandeDoSul',
  'Evento',
  'TV',
  'Entretenimento'
]
termo: string = '';
postsPorLegenda: any[] = [];
postsRecomendados: any[] = [];
get pesquisaOcorreu(): boolean {
  return this.termo.length > 0;
}
ngOnInit(): void {
  this.listCelebritiesPost();
  this.listViralPost();
  this.listFollowedPeoplePost();
  this.listPostRecomendados();

}
openModal(post: any, index: number) {
  this.postSelected = post;
  this.selectedIndex = index;


}
openPostDetails(post: any, event: Event) {
  this.postSelected = post;
}

getModalId(): string {
  return `modal-${this.selectedPostType}-${this.selectedIndex}`;
}
listPostRecomendados() {
  this.postsService.listarPostsRecomendados(
    {
      pageable: this.pageable
    }
  ).subscribe((response) => {
    this.postsRecomendados = response.content || [];
  });
}
listPostsByLegenda() {
  this.postsService.buscarPostsPorLegenda(
    {
      termo: this.termo,
      pageable: this.pageable
    }
  ).subscribe((response) => {
    this.postsPorLegenda = response.content || [];
  });
}
listCelebritiesPost() {
  this.postsService.listarPostsPopulares(
    {
      pageable: this.pageable
    }
  ).subscribe((response) => {
    this.celebrityPosts = response.content || [];
  });
}
listViralPost() {
  this.postsService.listarPostsTendencias(
    {
      pageable: this.pageable
    }
  ).subscribe((response) => {
    this.viralPosts = response.content || [];
  });
}
listFollowedPeoplePost() {
  this.postsService.listarPostsPopularesSeguidores(
    {
      pageable: this.pageable
    }
  ).subscribe((response) => {
    this.followedPeoplePosts = response.content || [];
  });
}
}
