import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Title } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';
import { AutenticacaoService } from '../../services/services';

@Component({
  selector: 'app-redefinir-senha',
  templateUrl: './redefinir-senha.component.html',
  styleUrls: ['./redefinir-senha.component.css'],
  imports: [RouterLink, FormsModule, ReactiveFormsModule, CommonModule],
})
export class RedefinirSenhaComponent implements OnInit {
  private destroy$ = new Subject<void>();
  form!: FormGroup;
  isLoading = false;
  sucesso = false;
  errorMessage: string | null = null;
  token = '';

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacaoService,
    private route: ActivatedRoute,
    private router: Router,
    private title: Title,
  ) {
    this.title.setTitle('Redefinir senha');
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
    this.form = this.fb.group({
      novaSenha: ['', [Validators.required, Validators.minLength(8)]],
      confirmacaoSenha: ['', [Validators.required]],
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  salvar(): void {
    if (this.form.invalid || !this.token) {
      return;
    }
    if (this.form.value.novaSenha !== this.form.value.confirmacaoSenha) {
      this.errorMessage = 'As senhas não conferem.';
      return;
    }
    this.isLoading = true;
    this.errorMessage = null;
    this.authService
      .redefinirSenha({
        body: {
          token: this.token,
          novaSenha: this.form.value.novaSenha,
          confirmacaoSenha: this.form.value.confirmacaoSenha,
        },
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isLoading = false;
          this.sucesso = true;
          setTimeout(() => this.router.navigate(['/login']), 2500);
        },
        error: () => {
          this.isLoading = false;
          this.errorMessage = 'Token inválido ou expirado. Solicite um novo link.';
        },
      });
  }
}
