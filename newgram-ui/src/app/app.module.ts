import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppComponent } from './app.component';
import { RouterOutlet } from '@angular/router';

@NgModule({
  declarations: [AppComponent],
  imports: [CommonModule, RouterOutlet],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
