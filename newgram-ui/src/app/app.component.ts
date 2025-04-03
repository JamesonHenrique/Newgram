import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterModule, RouterOutlet } from '@angular/router';
import { AsideComponent } from './core/aside/aside.component';
import { HeaderComponent } from './core/header/header.component';
import { CommonModule } from '@angular/common';
import {
  HTTP_INTERCEPTORS,
  HttpClient,
  HttpClientModule,
} from '@angular/common/http';

import { filter } from 'rxjs';
import { TokenService } from './services/token/token.service';
import { HttpTokenInterceptor } from './services/interceptor/algamoney-ui/src/app/services/interceptor/http-token.interceptor';
import { ApiModule } from './services/api.module';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AsideComponent, HeaderComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  providers: [

    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpTokenInterceptor,
      multi: true,
    },

  ],
})
export class AppComponent implements OnInit {
  title = 'newgram';
  isAuthPage = false;

  constructor(private router: Router) {}

  ngOnInit() {
    this.checkAuthPage(this.router.url);

    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.checkAuthPage(event.url);
      });
  }

  private checkAuthPage(url: string): void {
    this.isAuthPage = url.includes('/login') || url.includes('/register');
  }
}
