import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../../services/token/token.service';
import { AutenticacaoService } from '../../services/services';

@Component({
  selector: 'app-login',
  imports: [RouterLink, FormsModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  authForm!: FormGroup;
  errorMsg: Array<string> = [];

  constructor(
    private title: Title,
    private router: Router,
    private authService: AutenticacaoService,
    private tokenService: TokenService,
    private toastr: ToastrService,
    private fb: FormBuilder
  ) {
    this.title.setTitle('Login');
  }

  ngOnInit(): void {
    this.authForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', Validators.required],
    });
  }

  login() {
    this.errorMsg = [];

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

        this.router.navigate(['dashboard']);
      },
      error: (error) => {
        if (error.status === 401) {
          this.errorMsg.push(
            'Credenciais inválidas. Verifique seu e-mail e senha.'
          );
        } else if (error.error && error.error.validationErrors) {
          this.errorMsg = error.error.validationErrors;
        } else {
          this.errorMsg.push(
            'Ocorreu um erro inesperado. Tente novamente mais tarde.'
          );
        }

        this.errorMsg.forEach((msg) => {
          this.toastr.error(msg, 'Erro ao tentar logar');
        });
      },
    });

  }

  onSubmit() {
    this.login();
  }
}
