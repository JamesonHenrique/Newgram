import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { ProfileComponent } from './profile/profile.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { SearchProfileComponent } from './search-profile/search-profile.component';
import { ExploreComponent } from './explore/explore.component';
import { FavoriteComponent } from './favorite/favorite.component';

export const routes: Routes = [
  { path: 'feed', component:HomeComponent },
  { path: 'perfis', component:ProfileComponent },
  { path: 'criar-post', component:CreatePostComponent },
  { path: 'pessoas', component:SearchProfileComponent },
  { path: 'explorar', component:ExploreComponent },
  { path: 'salvos', component:FavoriteComponent },
  { path: 'login', component:LoginComponent },
  { path: 'register', component:RegisterComponent },
  { path: '**', redirectTo: 'feed' },

];
