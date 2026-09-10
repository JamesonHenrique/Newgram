import { map } from 'rxjs/operators';
import { TokenService } from './../services/token/token.service';
import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  ViewChild,
} from '@angular/core';
import { Title } from '@angular/platform-browser';
import { PostDetailsComponent } from '../post-details/post-details.component';
import { CommonModule } from '@angular/common';
import { FormatNumberPipe } from '../services/pipes/format-number.pipe';
import {
  ConversasService,
  DestaquesService,
  ModeracaoService,
  PostsService,
  SeguidoresService,
  StoriesService,
  UsuariosService,
} from '../services/services';
import { ActivatedRoute, Router } from '@angular/router';
import {
  catchError,
  Subject,
  switchMap,
  takeUntil,
  tap,
  throwError,
  finalize,
  forkJoin,
  of,
} from 'rxjs';
import { StoryModalComponent } from '../story-modal/story-modal.component';
import { CreateHighlightModalComponent } from '../create-highlight-modal/create-highlight-modal.component';
import { CreateStoryModalComponent } from '../create-story-modal/create-story-modal.component';
import { DateFormatPipe } from '../services/pipes/date-format-pipe';
import { EditProfileModalComponent } from '../edit-profile-modal/edit-profile-modal.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    PostDetailsComponent,
    CommonModule,
    FormatNumberPipe,
    CreateHighlightModalComponent,
    StoryModalComponent,
    CreateStoryModalComponent,
    EditProfileModalComponent,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent {
  private destroy$ = new Subject<void>();
  loading = false;
  error: string | null = null;

  userProfile: any = null;
  username: string = '';

  solicitacoes: any[] = [];
  totalSolicitacoes = 0;

  posts: any[] = [];
  postSelected: any = null;
  selectedIndex: number | null = null;

  storiesAtivos: any[] = [];
  currentStory: any = null;
  viewedStoriesIds: Set<number> = new Set<number>();
  lastStoriesCount: number = 0;

  destaque: any[] = [];

  isStoryModalOpen = false;
  isCreateHighlightModalOpen = false;
  isCreateStoryModalOpen = false;
  viewed = false;
  isEditProfileModalOpen = false;
  storiesExists = true;

  constructor(
    private title: Title,
    private usuariosService: UsuariosService,
    private route: ActivatedRoute,
    private postsService: PostsService,
    private tokenService: TokenService,
    private seguidorService: SeguidoresService,
    private destaqueService: DestaquesService,
    private storiesService: StoriesService,
    private moderacaoService: ModeracaoService,
    private conversasService: ConversasService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
  @ViewChild('highlightsContainer') highlightsContainer!: ElementRef;

  scrollHighlights(direction: 'left' | 'right') {
    const container = this.highlightsContainer.nativeElement;
    const scrollAmount = 200;
    if (direction === 'left') {
      container.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    } else {
      container.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  }

  shouldShowNavButtons(): boolean {
    if (!this.highlightsContainer) return false;
    const container = this.highlightsContainer.nativeElement;
    return container.scrollWidth > container.clientWidth;
  }
  ngOnInit(): void {
    this.title.setTitle('Perfil');
    this.loadProfileAndPosts();
    this.loadViewedStoriesFromStorage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadProfileAndPosts(): void {
    this.loading = true;
    this.error = null;

    this.route.params
      .pipe(
        tap((params) => (this.username = params['username'] || '')),
        switchMap(() => this.carregarPerfil()),
        switchMap(() =>
          forkJoin([
            this.findAllPostsById(),
            this.findAllDestaquesByUsername(),
            this.findAllStoriesAtivosByUserId(),
            this.carregarSolicitacoes(),
          ])
        ),
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        error: (err) => {
          console.error('Erro ao carregar perfil e posts:', err);
          this.error = 'Erro ao carregar perfil. Tente novamente mais tarde.';
        },
      });
  }

  /** Solicitações pendentes só fazem sentido no próprio perfil. */
  private carregarSolicitacoes() {
    if (!this.userProfile?.id || this.userProfile.id !== this.tokenService.userId) {
      this.solicitacoes = [];
      this.totalSolicitacoes = 0;
      return of([]);
    }
    return this.seguidorService
      .listarSolicitacoes({ pageable: { page: 0, size: 20, sort: [''] } })
      .pipe(
        map((page) => (page.content as any[] | undefined) || []),
        tap((lista) => {
          this.solicitacoes = lista;
          this.totalSolicitacoes = lista.length;
        }),
        catchError(() => {
          this.solicitacoes = [];
          return of([]);
        })
      );
  }

  aceitarSolicitacao(solicitacao: any, event: Event): void {
    event.stopPropagation();
    this.seguidorService
      .aceitarSolicitacao({ id: solicitacao.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.solicitacoes = this.solicitacoes.filter((s) => s.id !== solicitacao.id);
          this.totalSolicitacoes = this.solicitacoes.length;
        },
        error: (error) => console.error('Erro ao aceitar solicitação:', error),
      });
  }

  rejeitarSolicitacao(solicitacao: any, event: Event): void {
    event.stopPropagation();
    this.seguidorService
      .rejeitarSolicitacao({ id: solicitacao.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.solicitacoes = this.solicitacoes.filter((s) => s.id !== solicitacao.id);
          this.totalSolicitacoes = this.solicitacoes.length;
        },
        error: (error) => console.error('Erro ao rejeitar solicitação:', error),
      });
  }

  private carregarPerfil() {
    if (!this.username) {
      this.error = 'Username não definido';
      return throwError(() => new Error('Username não definido'));
    }

    return this.usuariosService
      .buscarUsuarioPorUsername({ username: this.username })
      .pipe(
        tap((usuario) => (this.userProfile = usuario)),
        tap((usuario) => this.carregarEstadoSeguimento(usuario)),
        catchError((err) => {
          console.error('Erro ao carregar perfil:', err);
          this.error = 'Usuário não encontrado';
          return throwError(() => err);
        })
      );
  }

  /** Preenche solicitacaoPendente para exibir "Solicitado" em conta privada. */
  private carregarEstadoSeguimento(usuario: any): void {
    if (!usuario?.id || usuario.id === this.tokenService.userId || usuario.seguindoUsuario) {
      return;
    }
    this.seguidorService
      .verificarSeguimento({ usuarioId: usuario.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (estado) => {
          if (this.userProfile) {
            this.userProfile = {
              ...this.userProfile,
              seguindoUsuario: estado.seguindo,
              solicitacaoPendente: estado.solicitacaoPendente,
            };
          }
        },
        error: () => {},
      });
    this.moderacaoService
      .verificarBloqueio({ usuarioId: usuario.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (estado) => {
          if (this.userProfile) {
            this.userProfile = { ...this.userProfile, bloqueado: !!estado.bloqueado };
          }
        },
        error: () => {},
      });
  }

  toggleBloqueio(event: Event): void {
    event.stopPropagation();
    const user = this.userProfile;
    if (!user?.id) {
      return;
    }
    const acao$ = user.bloqueado
      ? this.moderacaoService.desbloquear({ usuarioId: user.id })
      : this.moderacaoService.bloquear({ usuarioId: user.id });
    acao$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.userProfile = {
          ...user,
          bloqueado: !user.bloqueado,
          seguindoUsuario: false,
          solicitacaoPendente: false,
        };
      },
      error: (error) => console.error('Erro ao alternar bloqueio:', error),
    });
  }

  conversar(event: Event): void {
    event.stopPropagation();
    const user = this.userProfile;
    if (!user?.id) {
      return;
    }
    this.conversasService
      .iniciarConversa({ usuarioId: user.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.router.navigate(['/mensagens']),
        error: (error) => console.error('Erro ao abrir conversa:', error),
      });
  }

  private findAllPostsById() {
    if (!this.userProfile?.id) {
      this.posts = [];
      return of([]);
    }

    return this.postsService
      .listarPostsDoUsuario({
        usuarioId: this.userProfile.id,
        pageable: { page: 0, size: 1000, sort: [''] },
      })
      .pipe(
        map((posts) => posts.content || []),
        tap((posts) => {
          this.posts = posts;
        }),
        catchError((err) => {
          console.error('Erro ao buscar posts:', err);
          this.posts = [];
          this.error = 'Erro ao carregar posts';
          return of([]);
        })
      );
  }
  private findAllDestaquesByUsername() {
    return this.destaqueService
      .listarDestaquesPorUsername({
        username: this.username,
        pageable: { page: 0, size: 50, sort: [''] },
      })
      .pipe(
        tap((page) => (this.destaque = (page.content as any[] | undefined) || [])),
        catchError((err) => {
          console.error('Erro ao buscar destaques:', err);
          return throwError(() => err);
        })
      );
  }

  private findAllStoriesAtivosByUserId() {
    if (!this.userProfile?.id)
      return throwError(() => new Error('ID do usuário não disponível'));

    return this.storiesService
      .listarStoriesDoUsuario({
        autorId: this.userProfile.id,
        pageable: { page: 0, size: 50, sort: [''] },
      })
      .pipe(
        tap((page) => {
          const stories = (page.content as any[] | undefined) || [];
          if (stories.length !== this.lastStoriesCount) {
            this.viewed = false;
            this.lastStoriesCount = stories.length;
          }
          this.storiesAtivos = stories;
          this.updateViewedStatus();
          this.saveViewedStoriesToStorage();
        }),
        catchError((err) => {
          console.error('Erro ao buscar stories:', err);
          return throwError(() => err);
        })
      );
  }

  openStory(storyData: any) {
    if (!storyData) return;

    this.currentStory = storyData;
    this.isStoryModalOpen = true;
    document.body.style.overflow = 'hidden';

    if (!storyData.stories) {
      if (Array.isArray(storyData)) {
        storyData.forEach((story: any) => this.viewedStoriesIds.add(story.id));
      } else if (storyData.id) {
        this.viewedStoriesIds.add(storyData.id);
      }
      this.updateViewedStatus();
    }
  }

  closeStoryModal() {
    this.isStoryModalOpen = false;
    document.body.style.overflow = '';
  }

  getStoryImages(storyData: any): string[] {
    if (!storyData) return [];

    if (storyData?.stories) {
      return storyData.stories
        .map((story: any) => story.storieImagemUrl)
        .filter(Boolean);
    }

    if (Array.isArray(storyData)) {
      return storyData
        .map((story: any) => story.storieImagemUrl)
        .filter(Boolean);
    }

    if (storyData?.storieImagemUrl) {
      return [storyData.storieImagemUrl];
    }

    return [];
  }

  private loadViewedStoriesFromStorage() {
    const saved = localStorage.getItem('viewedStories');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        this.viewedStoriesIds = new Set(parsed.ids);
        this.lastStoriesCount = parsed.count || 0;
      } catch (e) {
        console.error('Erro ao ler viewedStories do localStorage', e);
      }
    }
  }

  private saveViewedStoriesToStorage() {
    localStorage.setItem(
      'viewedStories',
      JSON.stringify({
        ids: Array.from(this.viewedStoriesIds),
        count: this.lastStoriesCount,
      })
    );
  }

  updateViewedStatus() {
    this.viewed =
      this.storiesAtivos.length > 0 &&
      this.storiesAtivos.every((story: any) =>
        this.viewedStoriesIds.has(story.id)
      );
  }

  get isPerfilDoUsuarioLogado(): boolean {
    return (
      !!this.userProfile && this.userProfile.id === this.tokenService.userId
    );
  }

  /** Conta privada de outro usuário sem vínculo: mostra cadeado em vez dos posts. */
  get isContaPrivadaFechada(): boolean {
    return (
      !!this.userProfile &&
      !!this.userProfile.privado &&
      !this.isPerfilDoUsuarioLogado &&
      !this.userProfile.seguindoUsuario
    );
  }

  get nomeDoUsuario(): string {
    return this.userProfile?.nome || '';
  }

  getFotoPerfil(user: any): string {
    if (!user?.fotoPerfil || user.fotoPerfil.trim() === '') {
      return '/icons/profile-placeholder.svg';
    }
    return user.fotoPerfil;
  }

  getImagemPost(imagem: string | null | undefined): string {
    if (!imagem || imagem.trim() === '') {
      return '/icons/post-placeholder.svg';
    }
    return imagem;
  }
  getDestaqueFotoDeCapa(destaque: any): string {
    if (!destaque || destaque.trim() === '') {
      return '/icons/post-placeholder.svg';
    }

    if (destaque.includes('post-placeholder.svg')) {
      return destaque;
    }

    return destaque;
  }
  handleFotoPerfilError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';
    imgElement.onerror = null;
  }

  handleImageError(event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/post-placeholder.svg';
    imgElement.onerror = null;
  }

  ifVerified(): boolean {
    return this.userProfile?.numeroSeguidores > 100000;
  }

  toggleFollow(user: any, event: Event) {
    event.stopPropagation();
    if (user.seguindoUsuario || user.solicitacaoPendente) {
      this.deixarDeSeguir(user, event);
    } else {
      this.seguir(user, event);
    }
  }

  private seguir(user: any, event: Event) {
    event.stopPropagation();
    this.seguidorService
      .seguirUsuario({ usuarioId: user.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // Conta privada: vira solicitação pendente em vez de follow imediato.
          if (user.privado) {
            user.solicitacaoPendente = true;
          } else {
            user.seguindoUsuario = true;
          }
        },
        error: (error) => console.error('Erro ao seguir:', error),
      });
  }

  private deixarDeSeguir(user: any, event: Event) {
    event.stopPropagation();
    this.seguidorService
      .deixarDeSeguir({ usuarioId: user.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          user.seguindoUsuario = false;
          user.solicitacaoPendente = false;
        },
        error: (error) => console.error('Erro ao deixar de seguir:', error),
      });
  }

  openCreateHighlightModal() {
    this.isCreateHighlightModalOpen = true;
  }

  closeCreateHighlightModal() {
    this.isCreateHighlightModalOpen = false;
  }
  openEditProfileModal() {
    this.isEditProfileModalOpen = true;
  }

  onProfileUpdated(updatedUser: any) {
    this.userProfile = { ...this.userProfile, ...updatedUser };
    this.carregarPerfil().subscribe();
  }
  handleCreateHighlight(event: { name: string; selectedStories: string[] }) {
    console.log('Criando destaque:', event);
    this.findAllDestaquesByUsername().subscribe(() => {
      this.cdr.detectChanges();
    });
  }
  openCreateStoryModal() {
    this.isCreateStoryModalOpen = true;
  }

  closeCreateStoryModal() {
    this.isCreateStoryModalOpen = false;
  }

  handleCreateStory(files: File[]) {
    if (!files.length) return;

    this.closeCreateStoryModal();
  }

  openModal(post: any, index: number) {
    this.postSelected = post;
    this.selectedIndex = index;
  }

  openPostDetails(post: any, event: Event) {
    event.stopPropagation();
    this.postSelected = post;
  }
}
