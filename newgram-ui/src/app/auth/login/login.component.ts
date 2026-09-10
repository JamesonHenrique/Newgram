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
        this.tokenService.token = response.token as string;

        this.router.navigate(['feed']);
      },
      error: (error) => {
        this.handleLoginError(error);
      },
    });

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
