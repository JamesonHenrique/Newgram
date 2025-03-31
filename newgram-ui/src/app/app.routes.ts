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

export const routes: Routes = [
  { path: 'feed', component:HomeComponent },
  { path: 'perfis/:id', component:ProfileComponent },
  { path: 'criar-post', component:CreatePostComponent },
  { path: 'editar-post/:id', component:UpdatePostComponent },
  { path: 'pessoas', component:SearchProfileComponent },
  { path: 'explorar', component:ExploreComponent },
  { path: 'salvos', component:FavoriteComponent },
  { path: 'login', component:LoginComponent },
  { path: 'register', component:RegisterComponent },
  { path: '**', redirectTo: 'feed' },

];
