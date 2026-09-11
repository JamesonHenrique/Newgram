import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { ProfileComponent } from './profile/profile.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { SearchProfileComponent } from './search-profile/search-profile.component';
import { ExploreComponent } from './explore/explore.component';
import { FavoriteComponent } from './favorite/favorite.component';

import { AuthGuard } from './services/guard/auth.guard';
import { AdminGuard } from './services/guard/admin.guard';
import { UsuarioResolver } from './services/resolver/usuario.resolver';
import { LoginRedirectGuard } from './services/guard/login-redirect.guard';

export const routes: Routes = [
  {
    path: 'login',
    pathMatch: 'full',
    redirectTo: 'login'
  },
  {
    path: 'feed',
    component: HomeComponent,
    canActivate: [AuthGuard],
    resolve: { usuario: UsuarioResolver }
  },
  {
    path: 'perfil/:username',
    component: ProfileComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'criar-post',
    component: CreatePostComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'editar-post/:id',
    component: CreatePostComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'pessoas',
    component: SearchProfileComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'explorar',
    component: ExploreComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'salvos',
    component: FavoriteComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'mensagens',
    loadComponent: () => import('./mensagens/mensagens.component').then((m) => m.MensagensComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'reels',
    loadComponent: () => import('./reels/reels.component').then((m) => m.ReelsComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'moderacao',
    loadComponent: () => import('./moderacao/moderacao.component').then((m) => m.ModeracaoComponent),
    canActivate: [AuthGuard, AdminGuard]
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    loadComponent: () => import('./auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'recuperar-senha',
    loadComponent: () =>
      import('./auth/recuperar-senha/recuperar-senha.component').then((m) => m.RecuperarSenhaComponent),
  },
  {
    path: 'redefinir-senha',
    loadComponent: () =>
      import('./auth/redefinir-senha/redefinir-senha.component').then((m) => m.RedefinirSenhaComponent),
  },
  {
    path: 'verificar-email',
    loadComponent: () =>
      import('./auth/verificar-email/verificar-email.component').then((m) => m.VerificarEmailComponent),
  },
  {
    path: '**',
    redirectTo: 'feed'
  }
];
