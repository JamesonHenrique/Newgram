import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AsideComponent } from './core/aside/aside.component';
import { HeaderComponent } from './core/header/header.component';
import { PostDetailsComponent } from './post-details/post-details.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AsideComponent, HeaderComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'newgram-ui';
}
