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
import { StoryModalComponent } from "../story-modal/story-modal.component";

@Component({
  selector: 'app-profile',
  imports: [PostDetailsComponent, CommonModule, FormatNumberPipe, StoryModalComponent],
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
          images: [
            'https://st2.depositphotos.com/4744673/8357/i/450/depositphotos_83575466-stock-photo-looking-through-window-airplane.jpg',
            'https://images.unsplash.com/photo-1598550476439-6847785fcea6?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80',
            'https://images.unsplash.com/photo-1542751371-adc38448a05e?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
          ]
        },
        {
          title: 'Reviews',
          images: [
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS4ixlqhwWUSt63c0RXEUFTCd1DVbp4hvxo8Q&s',
            'https://images.unsplash.com/photo-1493711662062-fa541adb3fc8?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
          ]
        },
        {
          title: 'Dicas',
          images: [
            'https://images.unsplash.com/photo-1522542550221-31fd19575a2d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80',
            'https://images.unsplash.com/photo-1522542550221-31fd19575a2d?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
          ]
        },
        {
          title: 'Eventos',
          images: [
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ_VcSIKb_yfIhKVBGf1mWpS6-lfmKe_h7mLw&s',
            'https://images.unsplash.com/photo-1511578314322-379afb476865?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
          ]
        },
        {
          title: 'Memes',
          images: [
            'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJet3DAVZswBCCgf8qXaSNSscay5LBfmVMNg&s',
            'https://images.unsplash.com/photo-1605559424843-9e4c228bf1c2?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80'
          ]
        }
      ]
    },
  ];
  postSelected: any = [];
  selectedIndex: any = [];
  following: any = this.userProfilee[0].followed;
  userProfile: any = [];
  username: string = '';
  posts: any = [];
  isStoryModalOpen = false;
  currentStory: any = null;
  profileAvatar = 'https://media.istockphoto.com/id/610259354/pt/foto/jovem-mulher-usando-dslr-c%C3%A2mara.jpg?s=612x612&w=0&k=20&c=R1agbbanj4qKfZ6dFERdUovwchDOgIvtlJmEnLEO_XY=';
  viewed: boolean = false;
  storiesExists: boolean = true;
showCreateHighlightModal = false;

openCreateHighlightModal() {
  this.showCreateHighlightModal = true;
}

onHighlightCreated(newHighlight: any) {

  console.log('Novo highlight criado:', newHighlight);
  this.showCreateHighlightModal = false;

}
  toggleViewed() {
    this.viewed = !this.viewed;

  }

  checkStories(): void {
    this.storiesExists = this.userProfilee[0].highlights.length > 0;
  }
  openStory(highlight: any) {
    this.currentStory = highlight;
    this.isStoryModalOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeStoryModal() {
    this.isStoryModalOpen = false;
    document.body.style.overflow = '';
  }
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
  get nomeDoUsuario(): string {
    return this.userProfile?.nome || '';
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
    if (!user.fotoPerfil || user.fotoPerfil.trim() === '') {
      return '/icons/profile-placeholder.svg';
    }
    if (user.fotoPerfil.includes('post-placeholder.svg')) {
      return user.fotoPerfil;
    }
    return user.fotoPerfil;
  }
  getImagemPost(imagem: string | null | undefined): string {
    if (!imagem || imagem.trim() === '') {
      return '/icons/post-placeholder.svg';
    }

    if (imagem.includes('post-placeholder.svg')) {
      return imagem;
    }

    return imagem;
  }
  handleFotoPerfilError( event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';
    imgElement.onerror = null;
  }

  handleImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/post-placeholder.svg';
    imgElement.onerror = null;
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
