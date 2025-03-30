import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-home',
  imports: [CommonModule, PostDetailsComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  constructor(private title:  Title) {}
  ngOnInit(): void {
    this.title.setTitle('Feed');
  }
  postSelected:any = []
  selectedIndex:any = []

  posts = [
    {
      id: 1,
      avatar: 'https://www.cnnbrasil.com.br/wp-content/uploads/sites/12/2025/01/santos-neymar_ce226e-e1738361128243.jpg?w=1200&h=1200&crop=1',
      author: 'Neymar Jr',
      time: '2 horas atrás',
      location: 'Riyadh, Saudi Arabia',
      text: 'Preparando para o próximo jogo no Santos! ⚽🔥 #neymar #santos #futebol',
      image: 'https://www.365scores.com/pt-br/news/magazine/wp-content/uploads/2025/02/Neymar-Santos-scaled.jpg',
      tags: ['neymar', 'santos', 'futebol', 'treino'],
      likes: 125000,
      comments: 8700,
      isLiked: false,
      isAnimating: false,
      isFavorite: false
    },
    {
      id: 2,
      avatar: 'https://i.pinimg.com/originals/94/29/3d/94293dc5f07cb0b2825e8a7d16e164ed.jpg',
      author: 'Cristiano Ronaldo',
      time: '5 horas atrás',
      location: 'Al Nassr, Saudi Arabia',
      text: 'Vitória importante hoje! Obrigado a todos os torcedores pelo apoio. 💪🏼 #cr7 #alnassr #champions',
      image: 'https://fly.metroimg.com/upload/q_85,w_700/https://uploads.metroimg.com/wp-content/uploads/2024/08/26113635/cristiano-ronaldo-futebol-futuro-aposentadoria.jpg',
      tags: ['cr7', 'alnassr', 'champions', 'vitoria'],
      likes: 3200000,
      comments: 125000,
      isLiked: false,
      isAnimating: false,
      isFavorite: false
    },
  ];
topCreators = [
  {
    id: 1,
    avatar: 'https://s2-oglobo.glbimg.com/LaW6NoqTlik3XAzENbU6WZrVLaI=/0x0:651x562/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_da025474c0c44edd99332dddb09cabe8/internal_photos/bs/2024/V/x/z1C221T4i0bNRAYNDNAA/whatsapp-image-2024-09-26-at-17.13.21.jpeg',
    name: 'Whindersson Nunes',
    username: 'whindersson'
  },
  {
    id: 2,
    avatar: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Maisa_Silva_at_Lady_Night_in_2019.jpg/800px-Maisa_Silva_at_Lady_Night_in_2019.jpg',
    name: 'Maisa Silva',
    username: 'maisa'
  },
  {
    id: 3,
    avatar: 'https://i.pinimg.com/736x/08/7c/8d/087c8dfd2b4a0908d976fdc43bdf749f.jpg',
    name: 'Vinicius junior',
    username: 'vini'
  },
  {
    id: 4,
    avatar: 'https://s2-oglobo.glbimg.com/SwdOjRFjeW2iIePq5vL_ErKE370=/0x0:1080x1080/888x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_da025474c0c44edd99332dddb09cabe8/internal_photos/bs/2023/o/q/ISbpHERsimB6DNvacj2g/snapinsta.app-369080977-18382653874036131-1419709696409653797-n-1080.jpg',
    name: 'Renato Cariani',
    username: 'renatocariani'
  }
]

otherUsers = [
  {
    id: 1,
    avatar: 'https://br.web.img3.acsta.net/c_310_420/pictures/22/03/17/20/59/0915999.jpg',
    name: 'Danilo Gentili',
    username: 'danilo'
  },

  {
    id: 2,
    avatar: 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRV61NGGfrNzInHErojPbfVKVdx_KvNiT_bmg&s',
    name: 'Felca',
    username: 'felca'
  },
  {
    id: 3,
    avatar: 'https://blogdohiellevy.com.br/wp-content/uploads/2024/08/WhatsApp-Image-2024-08-27-at-13.22.20-768x1024.jpeg',
    name: 'Julio Balestrin',
    username: 'julio'
  },

]
  selectedPost: any = null;
  showDetail = false;

  openPostDetails(post: any, event: Event) {
    this.postSelected = post;
  }

  toggleLike(post: any, event: Event) {
    event.stopPropagation();
    post.isAnimating = true;
    setTimeout(() => {
      post.isAnimating = false;
      post.isLiked = !post.isLiked;
      post.likes += post.isLiked ? 1 : -1;
    }, 300);
  }
  toggleFavorite(post: any, event: Event) {
    event.stopPropagation();
    post.isFavorite = !post.isFavorite;
  }
  formatNumber(count:any) {
    if (count >= 10000000) {
      return `${Math.floor(count / 1000000)}M`; // Ex: 28.700.000 → "28M"
    } else if (count >= 1000000) {
      return `${(count / 1000000).toFixed(1)}M`; // Ex: 1.500.000 → "1.5M"
    } else if (count >= 1000) {
      return `${Math.floor(count / 1000)}K`; // Ex: 150.000 → "150K"
    } else {
      return count.toString(); // Menos de 1.000
    }
  }
}
