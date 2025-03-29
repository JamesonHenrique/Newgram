import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { AsideComponent } from './core/aside/aside.component';
import { HeaderComponent } from './core/header/header.component';
import { PostDetailsComponent } from './post-details/post-details.component';
import { filter } from 'rxjs';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AsideComponent, HeaderComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'newgram-ui';
  isAuthPage = false;

  constructor(private router: Router) {}

  ngOnInit() {
    // Verifica a rota inicial
    this.checkAuthPage(this.router.url);

    // Escuta mudanças de rota
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd)
      )
      .subscribe((event: NavigationEnd) => {
        this.checkAuthPage(event.url);
      });
  }

  private checkAuthPage(url: string): void {
    this.isAuthPage = url.includes('/login') || url.includes('/register');
    console.log('Current URL:', url, 'isAuthPage:', this.isAuthPage);
  }
}
