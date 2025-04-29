import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ImageCacheService {
  private imageCache = new Map<string, BehaviorSubject<string>>();

  getImage(url: string): Observable<string> {
    // Se a imagem já está no cache, retorna do cache
    if (this.imageCache.has(url)) {
      return this.imageCache.get(url)!.asObservable();
    }

    // Se não está no cache, cria um novo BehaviorSubject
    const imageSubject = new BehaviorSubject<string>(url);
    this.imageCache.set(url, imageSubject);

    // Pré-carrega a imagem
    this.preloadImage(url).subscribe({
      next: () => imageSubject.next(url),
      error: () => {
        // Em caso de erro, usa o placeholder
        const placeholder = url.includes('profile') ?
          '/icons/profile-placeholder.svg' :
          '/icons/post-placeholder.svg';
        imageSubject.next(placeholder);
      }
    });

    return imageSubject.asObservable();
  }

  private preloadImage(url: string): Observable<boolean> {
    return new Observable(observer => {
      const img = new Image();
      img.src = url;

      img.onload = () => {
        observer.next(true);
        observer.complete();
      };

      img.onerror = () => {
        observer.error();
        observer.complete();
      };
    });
  }
}