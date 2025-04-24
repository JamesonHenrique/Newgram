import { TokenService } from './../services/token/token.service';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { CommonModule } from '@angular/common';
import { FormatNumberPipe } from '../format-number.pipe';
import { PostsService, UsuariosService } from '../services/services';
import { ActivatedRoute } from '@angular/router';
import {
  catchError,
  Subject,
  switchMap,
  takeUntil,
  tap,
  throwError,
} from 'rxjs';
@Component({
  selector: 'app-profile',
  imports: [PostDetailsComponent, CommonModule, FormatNumberPipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent {
  private destroy$ = new Subject<void>();
  constructor(
    private title: Title,
    private usuariosService: UsuariosService,
    private route: ActivatedRoute,
    private postsService: PostsService,
    private tokenService: TokenService
  ) {}
  ngOnInit(): void {
    this.title.setTitle('Perfil');
    this.loadProfileAndPosts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  postss = [
    {
      id: 1,
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjduzTUsXisG5zpKy2-Sjrm6Qstwzhk0x8Ew&s',
      author: 'Davy Jones - GameplayRJ',
      time: '2 horas atrás',
      location: 'Live no Twitch',
      text: 'Live hoje às 20h testando o novo patch de Elden Ring! Quem vai tá lá? 🎮🔥 #eldenring #gameplayrj #live',
      image: 'https://i.ytimg.com/vi/wulMWEhnkX4/maxresdefault.jpg',
      tags: ['#eldenring', '#gameplayrj', '#live', '#fps'],
      likes: 12500,
      comments: 870,
      isLiked: false,
      isAnimating: false,
      isFavorite: false,
    },
    {
      id: 2,
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjduzTUsXisG5zpKy2-Sjrm6Qstwzhk0x8Ew&s',
      author: 'Davy Jones - GameplayRJ',
      time: '1 dia atrás',
      location: 'Estúdio GameplayRJ',
      text: 'Review completo do novo God of War Ragnarök Valhalla! Nota 10/10 pra essa DLC GRÁTIS da Santa Monica 🪓🇳🇴 ',
      image: 'https://i.ytimg.com/vi/lK60hiaHSkE/sddefault.jpg',
      tags: [' #godofwar', ' #review', ' #ps5', ' #dlc'],
      likes: 28700,
      comments: 1540,
      isLiked: false,
      isAnimating: false,
      isFavorite: false,
    },
    {
      id: 3,
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjduzTUsXisG5zpKy2-Sjrm6Qstwzhk0x8Ew&s',
      author: 'Davy Jones - GameplayRJ',
      time: '3 dias atrás',
      location: 'Evento GameXP',
      text: 'Melhores momentos do evento GameXP 2024! O futuro dos jogos tá VINDO com força 💥 Confira o vlog completo no YouTube! #gamexp #evento #gamer',
      image:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTTj8vg5aIAgbAodU46pIrWQCKAi3d22JqMUw&s',
      tags: ['#gamexp', '#evento', '#vlog', '#gamer'],
      likes: 34200,
      comments: 2100,
      isLiked: false,
      isAnimating: false,
      isFavorite: false,
    },
  ];
  imageBase =
    'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSCEIme6O8TwaQL3UJOKZMng381Zjp1q3_pnA&s';
  userProfilee = [
    {
      name: 'Davy Jones - GameplayRJ',
      username: 'gameplayrj',
      bio: 'Criador de conteúdo GAMER 🎮 | Notícias, reviews e gameplay dos melhores jogos! 🕹️ | PC, PS5, Xbox e Nintendo | Parcerias: gameplayrj@email.com',
      avatar:
        'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjduzTUsXisG5zpKy2-Sjrm6Qstwzhk0x8Ew&s',
      followers: 150000,
      following: 250,
      followed: false,

      posts: 1240,
      highlights: [
        {
          title: 'Gameplays',
          image:
            'https://st2.depositphotos.com/4744673/8357/i/450/depositphotos_83575466-stock-photo-looking-through-window-airplane.jpg',
        },
        {
          title: 'Reviews',
          image:
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS4ixlqhwWUSt63c0RXEUFTCd1DVbp4hvxo8Q&s',
        },
        {
          title: 'Dicas',
          image:
            'https://i0.wp.com/blog.portaleducacao.com.br/wp-content/uploads/2022/07/Fotografia-e-a-sua-importancia-para-a-sociedade.jpg',
        },
        {
          title: 'Eventos',
          image:
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ_VcSIKb_yfIhKVBGf1mWpS6-lfmKe_h7mLw&s',
        },
        {
          title: 'Unboxing',
          image:
            'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4b/Everest_kalapatthar_crop.jpg/250px-Everest_kalapatthar_crop.jpg',
        },
        {
          title: 'Memes',
          image:
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJet3DAVZswBCCgf8qXaSNSscay5LBfmVMNg&s',
        },
      ],
    },
  ];
  postSelected: any = [];
  selectedIndex: any = [];
  following: any = this.userProfilee[0].followed;
  userProfile: any = [];
  username: string = '';
  posts: any = [];
  private loadProfileAndPosts(): void {
    this.route.params
      .pipe(
        tap((params) => (this.username = params['username'])),
        switchMap(() => this.carregarPerfil()),
        switchMap(() => this.findAllPostsById()),
        takeUntil(this.destroy$)
      )
      .subscribe({
        error: (err) => console.error('Erro ao carregar perfil e posts:', err),
      });
  }
  get isPerfilDoUsuarioLogado(): boolean {
    return (
      !!this.userProfile && this.userProfile.id === this.tokenService.userId
    );
  }

  private carregarPerfil() {
    if (!this.username) {
      return throwError(() => new Error('Username não definido'));
    }

    return this.usuariosService
      .buscarUsuarioPorUsername({ username: this.username })
      .pipe(
        tap((usuario) => {
          this.userProfile = usuario;
        }),
        catchError((err) => {
          console.error('Erro ao carregar perfil:', err);
          return throwError(() => err);
        })
      );
  }
  getFotoPerfil(user: any): string {
    if (user?.fotoPerfil && user.fotoPerfil.trim() !== '') {
      return 'data:image/jpg;base64,' + user.fotoPerfil;
    }
    return '/icons/profile-placeholder.svg';
  }
  getImagemPost(imagemBase64: string | null | undefined): string {
    if (imagemBase64 && imagemBase64.trim() !== '') {
      return 'data:image/jpg;base64,' + imagemBase64;
    }
    return '/icons/post-placeholder.svg';
  }
  private findAllPostsById() {
    if (!this.userProfile?.id) {
      return throwError(() => new Error('ID do usuário não disponível'));
    }

    return this.postsService
      .listarPostsDoUsuario({
        usuarioId: this.userProfile.id,
        pageable: {
          page: 0,
          size: 10,
          sort: [''],
        },
      })
      .pipe(
        tap((posts) => {
          this.posts = posts.content;
        }),
        catchError((err) => {
          console.error('Erro ao buscar posts:', err);
          return throwError(() => err);
        })
      );
  }

  follow() {
    this.following = !this.following;
  }
  ifVerified() {
    return this.userProfile.numeroSeguidores > 100000;
  }
  openModal(post: any, index: number) {
    this.postSelected = post;
    this.selectedIndex = index;
  }
  openPostDetails(post: any, event: Event) {
    this.postSelected = post;
  }
}
