import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-search-profile',
  imports: [CommonModule,],
  templateUrl: './search-profile.component.html',
  styleUrl: './search-profile.component.css',
})
export class SearchProfileComponent {
  constructor(private title:Title) {
    this.title.setTitle('Buscar pessoas');
  }
  connectionUsers = [
    {
      id: 1,
      name: 'Whindersson Nunes',
      username: 'whindersson',
      mutual: 12,
      avatar:
        'https://s2-oglobo.glbimg.com/LaW6NoqTlik3XAzENbU6WZrVLaI=/0x0:651x562/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_da025474c0c44edd99332dddb09cabe8/internal_photos/bs/2024/V/x/z1C221T4i0bNRAYNDNAA/whatsapp-image-2024-09-26-at-17.13.21.jpeg',
    },
    {
      id: 2,
      name: 'Maicon Kuster',
      username: 'maiconkuster',
      mutual: 8,
      avatar:
        'https://akamai.sscdn.co/uploadfile/letras/fotos/4/1/1/e/411e64de8de9630bf087468c3f17d08e.jpg',
    },
    {
      id: 3,
      name: 'Gabriel Barbosa',
      username: 'gabigol',
      mutual: 5,
      avatar:
        'https://encrypted-tbn0.gstatic.com/licensed-image?q=tbn:ANd9GcSUno0Sgdu4h9mmT59UkXXSGWQP_NKELMhq2EKXagpC21jo-0-lGCcgFKZ-kLfrA9k81G1eTt8FycMtLOE',
    },
    {
      id: 4,
      name: 'Luísa Sonza',
      username: 'luisasonza',
      mutual: 7,
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT8EYqnKM8fN7DI9IBdXM8M2xy8UvUU8w3QwA&s',
    },
    {
      id: 5,
      name: 'Luccas Neto',
      username: 'luccasneto',
      mutual: 3,
      avatar:
        'https://yt3.googleusercontent.com/ytc/AIdro_lG6cShOWXOgqhg4GscLlZavM40kxUr86RQLJpePMjJufY=s900-c-k-c0x00ffffff-no-rj',
    },
  ];
  randomUsers = [
    {
      id: 1,
      name: 'Ivete Sangalo',
      username: 'ivetesangalo',
      avatar:
        'https://cdn-images.dzcdn.net/images/artist/fb27c1806a4b63a9633da56f57ca5fd0/1900x1900-000000-80-0-0.jpg',
      posts: 1243,
      following: 289,
      followers: 28700000,
      bio: 'Cantora, empresária e rainha do Axé. 🎤✨ #VivaViver',
    },
    {
      id: 2,
      name: 'Xamã',
      username: 'xamaoficial',
      avatar:
        'https://novabrasilfm.com.br/app/uploads/2024/10/xama-768x691.webp',
      posts: 532,
      following: 156,
      followers: 12400000,
      bio: 'Rapper e compositor. 🎶 "Malvadão" é meu cartão de visitas. 🏆',
    },
    {
      id: 3,
      name: 'Gkay',
      username: 'gessicakayane',
      avatar:
        'https://s2-gshow.glbimg.com/r4_diUBCNZklNxL8AqrdiHT6rzU=/0x0:1080x1349/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_e84042ef78cb4708aeebdf1c68c6cbd6/internal_photos/bs/2022/6/d/321ZIATlmX9fgHTKDF0w/gkay.jpg',
      posts: 2876,
      following: 842,
      followers: 35200000,
      bio: 'Humorista, digital influencer e dona do #GKayFam. 💖',
    },
    {
      id: 4,
      name: 'Lázaro Ramos',
      username: 'lazaroramos',
      avatar:
        'https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/L%C3%A1zaro_Ramos_01.jpg/640px-L%C3%A1zaro_Ramos_01.jpg',
      posts: 876,
      following: 124,
      followers: 7800000,
      bio: 'Ator, diretor e escritor. Pai da Lis e do João. 📚🎭',
    },
    {
      id: 5,
      name: 'Tais Araújo',
      username: 'taisdeverdade',
      avatar:
        'https://br.web.img3.acsta.net/c_310_420/pictures/19/09/18/21/50/3636346.jpg',
      posts: 2105,
      following: 431,
      followers: 15600000,
      bio: 'Atriz, apresentadora.',
    },

  ];
  suggestionsUsers = [
    {
      id: 1,
      name: 'Matueê',
      username: 'matue',
      avatar:
        'https://s2-g1.glbimg.com/edM1HJtDGbHdDXZvhIJfUGiyWrA=/0x0:1080x1350/924x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_59edd422c0c84a879bd37670ae4f538a/internal_photos/bs/2020/9/G/qfvEJ5Qdiq18A4BKQGJQ/matue4.jpg',
    },
    {
      id: 2,
      name: 'Renato Aragão',
      username: 'renatoaragao',
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSkHNQUx6HLZ2in83UGLhV4YghCHQelpwVyDQ&s',
    },
    {
      id: 3,
      name: 'Gregório Duvivier',
      username: 'gregoriocomg',
      avatar:
        'https://upload.wikimedia.org/wikipedia/commons/thumb/9/93/Greg%C3%B3rio_Duvivier_2016.JPG/1200px-Greg%C3%B3rio_Duvivier_2016.JPG',
    },
    {
      id: 4,
      name: 'Sophia Abrahão',
      username: 'sophiaabrahao',
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQb1vBtz836m5epTj1IhiHRF9GW1NngzoUFHQ&s',
    },
    {
      id: 5,
      name: 'Emicida',
      username: 'emicida',
      avatar:
        'https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Emicida_Festival_Sensacional_2020_%28cropped%29.jpg/800px-Emicida_Festival_Sensacional_2020_%28cropped%29.jpg',
    },
  ];
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
