import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../../services/token/token.service';


@Component({
  selector: 'app-register',
  imports: [RouterLink,FormsModule, ReactiveFormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  authForm!: FormGroup;
  private authService: any
  constructor(
    private title: Title,
    private router: Router,
    private tokenService: TokenService,
    private toastr: ToastrService,
    private fb: FormBuilder
  ) {
    this.title.setTitle('Register');
  }
  ngOnInit(): void {
    this.authForm = this.fb.group(
      {
        nome: ['', Validators.required],
        email: ['', Validators.required],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        username: ['', Validators.required],
        avatar: ['', Validators.required],
        bio: ['', Validators.required],
      },
      { validators: this.senhasIguais }
    );


  }

  senhasIguais(control: AbstractControl): ValidationErrors | null {
    const senha = control.get('password')?.value;
    const confirmSenha = control.get('confirmPassword')?.value;
    return senha === confirmSenha ? null : { senhasDiferentes: true };
  }

  errorMsg: Array<string> = [];
  register() {
    this.errorMsg = [];
    const authRequest = { ...this.authForm.value };
    delete authRequest.confirmPassword;
    if(this.authForm.invalid) {
      this.toastr.warning(
        'Por favor, preencha todos os campos corretamente.',
        'Formulário inválido'
      );
      return;
    }
    this.authService
      .register({
        body: authRequest,
      })
      .subscribe({
        next: (response:any) => {
          this.tokenService.token = response.token as string;
          this.router.navigate(['login']);
          this.toastr.success('Conta criada com sucesso');
        },
        error: (error:any) => {
          this.errorMsg = error.error.validationErrors;

          this.toastr.error('Erro ao tentar registrar-se', error.error.error);

          this.errorMsg.push(error.error.error);
        },
      });
  }


  onSubmit() {
    this.register();
  }
  currentStep: number = 1;
  progressWidth: string = '33%';

  nextStep(): void {
    if (this.currentStep < 2) {
      this.currentStep++;
      this.updateProgressBar();
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
      this.updateProgressBar();
    }
  }

  private updateProgressBar(): void {
    this.progressWidth = `${(this.currentStep / 2) * 100}%`;
  }

  isStepActive(step: number): boolean {
    return this.currentStep === step;
  }
}
