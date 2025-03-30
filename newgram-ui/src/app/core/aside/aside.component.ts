import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-aside',
  imports: [RouterLink,RouterLinkActive,CommonModule],
  templateUrl: './aside.component.html',
  styleUrl: './aside.component.css'
})
export class AsideComponent {
  menuItems = [
    { path: '/feed', icon: 'fa-home', label: 'Página Inicial' },
    { path: '/explorar', icon: 'fa-compass', label: 'Explorar' },
    { path: '/pessoas', icon: 'fa-users', label: 'Pessoas' },
    { path: '/salvos', icon: 'fa-bookmark', label: 'Salvos' },
    { path: '/criar-post', icon: 'fa-plus-circle', label: 'Criar Post' }
  ];
  user = [
    {
      username: 'mrbeast',
      name: 'Mr. Beast',
      image: 'https://imageio.forbes.com/specials-images/imageserve/67167167f2fdc4bfaaf125f4/0x0.jpg?format=jpg&crop=1852,1856,x34,y173,safe&height=416&width=416&fit=bounds'
    }
  ]
  particles = Array.from({length: 8}, (_, i) => ({
    x: Math.random() * 120,
    y: Math.random() * 60,
    delay: i * 150
  }));
  updateSpotlight(event: MouseEvent, isActive: boolean) {
    const spotlight = document.querySelector('.active-route-spotlight');
    if (spotlight && isActive) {
      const target = event.currentTarget as HTMLElement;
      const rect = target.getBoundingClientRect();
      spotlight.setAttribute('style', `opacity: 0.3; transform: translateY(${rect.top + rect.height/2}px)`);
    }
  }
}
