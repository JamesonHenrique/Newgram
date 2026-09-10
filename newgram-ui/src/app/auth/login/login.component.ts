import { Title } from '@angular/platform-browser';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AutenticacaoService } from '../../services/services';
import { CommonModule } from '@angular/common';
import { TokenService } from '../../services/token/token.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [RouterLink, FormsModule, ReactiveFormsModule, CommonModule],
})
export class LoginComponent implements OnInit {
  authForm!: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  showPassword = false;
  etapaTwoFactor = false;
  codigoTwoFactor = '';

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacaoService,
    private router: Router,
    private title: Title,
    private tokenService: TokenService,
  ) {
    this.title.setTitle('Login');
  }

  ngOnInit(): void {
    this.initForm();
    this.checkRememberedUser();
  }

  private initForm(): void {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]],
      rememberMe: [false]
    });
  }

  private checkRememberedUser(): void {
    const rememberedEmail = localStorage.getItem('rememberedEmail');
    if (rememberedEmail) {
      this.authForm.patchValue({
        email: rememberedEmail,
        rememberMe: true
      });
    }
  }

  togglePasswordVisibility(field: 'senha'): void {
    if (field === 'senha') {
      this.showPassword = !this.showPassword;
      const input = document.getElementById('senha') as HTMLInputElement;
      if (input) {
        input.type = this.showPassword ? 'text' : 'password';
      }
    }
  }




  login() {


    if (this.authForm.invalid) {

      return;
    }

    const authRequest = this.authForm.value;
    if(localStorage.getItem('token')) {
      localStorage.removeItem('token');
    }

    this.authService.login({ body: authRequest }).subscribe({
      next: (response) => {
        // 2FA ativo: segunda etapa pede o código do app autenticador.
        if (response.twoFactorRequired) {
          this.etapaTwoFactor = true;
          this.isLoading = false;
          return;
        }
        this.entrarComTokens(response.token as string, response.refreshToken);
      },
      error: (error) => {
        this.isLoading = false;
        this.handleLoginError(error);
      },
    });

  }

  confirmarTwoFactor() {
    const codigo = this.codigoTwoFactor.trim();
    if (!codigo) {
      return;
    }
    this.isLoading = true;
    this.authService
      .verificarTwoFactor({ body: { email: this.authForm.value.email, codigo } })
      .subscribe({
        next: (response) => {
          this.entrarComTokens(response.token as string, response.refreshToken);
        },
        error: () => {
          this.isLoading = false;
          this.errorMessage = 'Código inválido. Tente novamente.';
        },
      });
  }

  private entrarComTokens(token: string, refreshToken?: string) {
    this.tokenService.token = token;
    if (refreshToken) {
      this.tokenService.refreshToken = refreshToken;
    }
    this.router.navigate(['feed']);
  }

  private handleLoginError(error: any): void {
    if (error.status === 401) {
      this.errorMessage = 'Email ou senha incorretos. Por favor, tente novamente.';
    } else if (error.status === 0) {
      this.errorMessage = 'Não foi possível conectar ao servidor. Verifique sua conexão com a internet.';
    } else {
      this.errorMessage = 'Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.';
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  get email() {
    return this.authForm.get('email');
  }

  get senha() {
    return this.authForm.get('senha');
  }

  get rememberMe() {
    return this.authForm.get('rememberMe');
  }
}
