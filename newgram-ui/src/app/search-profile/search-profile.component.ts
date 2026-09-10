import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { FormatNumberPipe } from '../services/pipes/format-number.pipe';
import { SeguidoresService, UsuariosService } from '../services/services';
import { Pageable } from '../services/models';
import { FormsModule } from '@angular/forms';
import { Subject, Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, map, catchError, takeUntil, tap } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-search-profile',
  imports: [CommonModule, FormatNumberPipe, FormsModule],
  templateUrl: './search-profile.component.html',
  styleUrl: './search-profile.component.css',
})
export class SearchProfileComponent {
  private destroy$ = new Subject<void>();

  searchProfiles = '';
  famousUsers: any = [];
  connectionUsers: any = [];
  randomUsers: any = [];
  filteredRandomUsers: any = [];
  cachedUsers:any = [];
  pageable: Pageable = {
    page: 0,
    size: 4,
    sort: ['']
  };

  numberOfElements = 0;
  totalPages = 0;
  totalElements = 0;

  searchSubject = new Subject<string>();

  constructor(
    private title: Title,
    private usuariosService: UsuariosService,
    private router: Router,
    private seguidorService: SeguidoresService
  ) {
    this.title.setTitle('Buscar pessoas');
  }

  ngOnInit(): void {
    this.initSearchObservable();
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get pesquisando(): boolean {
    return this.searchProfiles.trim() !== '';
  }

  private initSearchObservable(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((term: string) => this.handleSearch(term)),
      takeUntil(this.destroy$)
    ).subscribe();
  }

  private loadInitialData(): void {
    this.loadInitialCache();
    this.findAllConnectionUsers();
    this.findAllUsuariosFamosos();
    this.findAllUsuarios();
  }

  private loadInitialCache(): void {
    this.usuariosService.buscarUsuarios({
      termo: '',
      pageable: { page: 0, size: 100, sort: [''] }
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => this.cachedUsers = response.content || [],
      error: (err) => console.error('Erro ao carregar cache:', err)
    });
  }
  toggleFollow(user: any, event: Event) {
    event.stopPropagation();
    if (user.seguindoUsuario) {
      this.deixarDeSeguir(user, event);
    } else {
      this.seguir(user, event);
    }
  }

  deixarDeSeguir(user: any, event: Event) {
    event.stopPropagation();

    this.seguidorService
      .deixarDeSeguir({ usuarioId: user.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          user.seguindoUsuario = false;
        },
        error: (error) => {
          console.error('Erro ao deixar de seguir:', error);
        },
      });
  }

  seguir(user: any, event: Event) {
    event.stopPropagation();
    this.seguidorService
      .seguirUsuario({ usuarioId: user.id })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          user.seguindoUsuario = true;

        },
        error: (error) => {
          console.error('Erro ao seguir:', error);
        },
      });
  }
  onSearchInput(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.searchSubject.next(term);
  }

  private handleSearch(term: string): Observable<void> {
    this.searchProfiles = term;

    const cachedResults = this.searchInCache(term);
    this.updateDisplayedUsers(cachedResults);

    return this.buscarTodosUsuariosPorTermo(term).pipe(
      tap((serverResults) => {
        if (term === this.searchProfiles) {
          this.updateDisplayedUsers(serverResults);
          this.updateCache(serverResults);
        }
      }),
      map(() => undefined),
      catchError(() => of(undefined))
    );
  }

  private searchInCache(term: string){
    if (!term.trim()) return this.cachedUsers;

    const lowerTerm = term.toLowerCase();
    return this.cachedUsers.filter((user: any)  =>
      user.nome.toLowerCase().includes(lowerTerm) ||
      user.username.toLowerCase().includes(lowerTerm)
    );
  }

  private updateDisplayedUsers(users:any): void {
    this.randomUsers = users;
    this.filteredRandomUsers = this.paginateUsers(users);
    this.totalElements = users.length;
    this.numberOfElements = this.filteredRandomUsers.length;
    this.totalPages = Math.ceil(users.length / (this.pageable.size || 1));
  }

  private updateCache(newUsers: any): void {
    newUsers.forEach((newUser: any) => {
      if (!this.cachedUsers.some((u: any) => u.id === newUser.id)) {
        this.cachedUsers.push(newUser);
      }
    });
  }

  findAllUsuarios(): void {
    if (this.pesquisando) {
      this.buscarTodosUsuariosPorTermo(this.searchProfiles.trim()).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (users) => this.updateDisplayedUsers(users),
        error: (err) => console.error('Erro ao buscar usuários:', err)
      });
    } else {
      this.usuariosService.buscarUsuarios({
        termo: '',
        pageable: this.pageable
      }).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (response) => {
          this.randomUsers = response.content || [];
          this.numberOfElements = response.numberOfElements || 0;
          this.totalPages = response.totalPages || 0;
          this.totalElements = response.totalElements || 0;
        },
        error: (err) => console.error('Erro ao buscar usuários:', err)
      });
    }
  }

  private buscarTodosUsuariosPorTermo(termo: string) {
    return this.usuariosService.buscarUsuarios({
      termo,
      pageable: { page: 0, size: 1000, sort: [''] }
    }).pipe(
      map(response => response.content || []),
      catchError(() => of([]))
    );
  }

  findAllConnectionUsers(): void {
    this.usuariosService.listarUsuariosPorAmigosEmComum({
      pageable: this.pageable
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => this.connectionUsers = response.content || [],
      error: (err) => console.error('Erro ao buscar conexões:', err)
    });
  }

  findAllUsuariosFamosos(): void {
    this.usuariosService.listarUsuariosMaisFamosos({
      pageable: this.pageable
    }).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => this.famousUsers = response.content || [],
      error: (err) => console.error('Erro ao buscar usuários famosos:', err)
    });
  }

  isUltimaPagina(): boolean {
    return (this.pageable.page || 0) >= this.totalPages - 1;
  }

  avancarPagina(): void {
    if (this.isUltimaPagina() || this.numberOfElements === 0) {
      this.pageable.page = 0;
    } else {
      this.pageable.page = (this.pageable.page || 0) + 1;
    }
  }

  verPerfil(username: string): void {
    this.router.navigate(['/perfil', username]);
  }

  carregarMais(tipoDeUsuario: string): void {
    this.avancarPagina();

    switch (tipoDeUsuario) {
      case 'connection':
        this.findAllConnectionUsers();
        break;
      case 'random':
        this.findAllUsuarios();
        break;
      case 'famous':
        this.findAllUsuariosFamosos();
        break;
    }
  }

  private paginateUsers(users: any) {
    const start = (this.pageable.page || 0) * (this.pageable.size || 1);
    const end = start + (this.pageable.size || 1);
    return users.slice(start, end);
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

  handleFotoPerfilError( event: Event): void {
    const imgElement = event.target as HTMLImageElement;
    imgElement.src = '/icons/profile-placeholder.svg';
    imgElement.onerror = null;
  }


}
