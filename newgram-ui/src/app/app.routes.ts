import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { SearchProfileComponent } from './search-profile/search-profile.component';
import { ExploreComponent } from './explore/explore.component';
import { FavoriteComponent } from './favorite/favorite.component';

import { AuthGuard } from './services/guard/auth.guard';
import { UsuarioResolver } from './services/resolver/usuario.resolver';
import { LoginRedirectGuard } from './services/guard/login-redirect.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'feed'
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
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: '**',
    redirectTo: 'feed'
  }
];
