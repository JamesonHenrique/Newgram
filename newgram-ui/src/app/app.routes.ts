import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { SearchProfileComponent } from './search-profile/search-profile.component';
import { ExploreComponent } from './explore/explore.component';
import { FavoriteComponent } from './favorite/favorite.component';
import { UpdatePostComponent } from './update-post/update-post.component';
import { AuthGuard } from './services/guard/auth.guard';

export const routes: Routes = [
  { path: 'feed', component:HomeComponent, canActivate: [AuthGuard]  },
  { path: 'perfis/:id', component:ProfileComponent, canActivate: [AuthGuard] },
  { path: 'criar-post', component:CreatePostComponent, canActivate: [AuthGuard] },
  { path: 'editar-post/:id', component:UpdatePostComponent, canActivate: [AuthGuard] },
  { path: 'pessoas', component:SearchProfileComponent, canActivate: [AuthGuard] },
  { path: 'explorar', component:ExploreComponent, canActivate: [AuthGuard] },
  { path: 'salvos', component:FavoriteComponent, canActivate: [AuthGuard] },
  { path: 'login', component:LoginComponent },
  { path: 'register', component:RegisterComponent },
  { path: '**', redirectTo: 'feed' },

];
