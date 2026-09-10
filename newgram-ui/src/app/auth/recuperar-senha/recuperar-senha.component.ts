import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';
import { AutenticacaoService } from '../../services/services';

@Component({
  selector: 'app-recuperar-senha',
  templateUrl: './recuperar-senha.component.html',
  styleUrls: ['./recuperar-senha.component.css'],
  imports: [RouterLink, FormsModule, ReactiveFormsModule, CommonModule],
})
export class RecuperarSenhaComponent implements OnInit {
  private destroy$ = new Subject<void>();
  form!: FormGroup;
  isLoading = false;
  enviado = false;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacaoService,
    private title: Title,
  ) {
    this.title.setTitle('Recuperar senha');
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  enviar(): void {
    if (this.form.invalid) {
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.authService
      .recuperarSenha({ body: { email: this.form.value.email } })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isLoading = false;
          this.enviado = true;
        },
        error: () => {
          this.isLoading = false;
          this.errorMessage = 'Não foi possível enviar. Tente novamente.';
        },
      });
  }
}
