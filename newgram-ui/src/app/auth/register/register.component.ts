import { Component, ElementRef, ViewChild } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from '../../services/token/token.service';
import { CommonModule } from '@angular/common';
import { passwordValidator } from './validator/password.validator';
import { confirmPasswordValidator } from './validator/confirm-password.validator';
import { AutenticacaoService, UsuariosService } from '../../services/services';
import { catchError, of, switchMap, tap } from 'rxjs';
import { UsuarioCreateDto } from '../../services/models';
import { passwordMatchValidator } from './validator/custom-validators';

@Component({
  selector: 'app-register',
  imports: [RouterLink, FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  authForm!: FormGroup;

  @ViewChild('avatarUpload') avatarUpload!: ElementRef<HTMLInputElement>;
  @ViewChild('avatarImage') avatarImage!: ElementRef<HTMLImageElement>;
  @ViewChild('avatarPlaceholder') avatarPlaceholder!: ElementRef<HTMLElement>;
  selectedFotoDePerfil: any;
  passwordStrength = 0;
  strengthText = '';
  strengthClass = '';
  errorMessage: string | null = null;
  constructor(
    private title: Title,
    private router: Router,
    private authService: AutenticacaoService,
    private fb: FormBuilder,

  ) {
    this.title.setTitle('Register');
  }
  ngOnInit(): void {
    this.authForm = this.fb.group(
      {
        nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
        email: ['', Validators.required],
        senha: ['', [Validators.required, passwordValidator()]],
        confirmacaoSenha: ['', [Validators.required]],
        username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
        bio: ['', [Validators.maxLength(150), Validators.required]],
      },
      { validators: passwordMatchValidator() }
    );
    this.setupPasswordValidation();
  }

  errorMsg: Array<string> = [];
  register() {
    if (this.authForm.invalid) {
      return;
    }

    this.errorMessage = null;
    this.errorMsg = [];

    const authRequest: UsuarioCreateDto = {
      ...this.authForm.value,
      fotoPerfil: this.selectedFotoDePerfil || undefined,
    };

    this.authService.registrar({ body: authRequest }).pipe(
      tap(() => {
        this.router.navigate(['/login']);
        localStorage.removeItem('token');
      }),

      catchError((error) => {
        if (error.error && typeof error.error === 'object') {
          if (error.status === 413) {
            this.errorMessage = error.error.message || 'O arquivo enviado é muito grande';
          }
          else if (error.error.message) {
            this.errorMessage = error.error.message;
          }
          else if (error.error.validationErrors) {
            this.errorMsg = error.error.validationErrors;
          }
        }
        else {
          this.errorMessage = 'Erro ao realizar o registro. Tente novamente.';
        }

        console.error('Erro no registro:', error);

        return of(null);
      })
    ).subscribe();
  }
  private setupPasswordValidation(): void {
    const passwordControl = this.authForm.get('senha');
    if (passwordControl) {
      passwordControl.valueChanges.subscribe((password) => {
        this.updatePasswordRequirements(password);
        this.updatePasswordStrength(password);
      });
    }
  }
  calculatePasswordStrength(password: string): number {
    if (!password) return 0;

    let strength = 0;
    if (password.length >= 8) strength += 20;
    if (/[A-Z]/.test(password)) strength += 20;
    if (/[a-z]/.test(password)) strength += 20;
    if (/[0-9]/.test(password)) strength += 20;
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength += 20;

    return strength;
  }
  updatePasswordRequirements(password: string): void {
    if (!password) {
      document.querySelectorAll('.requirement').forEach((req) => {
        req.classList.remove('requirement-met');
        req.classList.add('requirement-unmet');
        const icon = req.querySelector('i');
        if (icon) {
          icon.classList.remove('fa-check');
          icon.classList.add('fa-circle');
        }
      });
      return;
    }

    const requirements = {
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[!@#$%^&*(),.?":{}|<>]/.test(password),
    };

    this.updateRequirement('reqLength', requirements.length);
    this.updateRequirement('reqUppercase', requirements.uppercase);
    this.updateRequirement('reqLowercase', requirements.lowercase);
    this.updateRequirement('reqNumber', requirements.number);
    this.updateRequirement('reqSpecial', requirements.special);
  }

  private updateRequirement(elementId: string, isMet: boolean): void {
    const element = document.getElementById(elementId);
    if (element) {
      element.classList.toggle('requirement-met', isMet);
      element.classList.toggle('requirement-unmet', !isMet);

      const icon = element.querySelector('i');
      if (icon) {
        icon.classList.toggle('fa-check', isMet);
        icon.classList.toggle('fa-circle', !isMet);
      }
    }
  }
  showPassword = false;
  showConfirmPassword = false;
  updatePasswordStrength(password: string): void {
    const strengthMeter = document.getElementById('strengthMeter');
    const strengthText = document.getElementById('strengthText');

    if (!strengthMeter || !strengthText) return;

    if (!password) {
      strengthMeter.style.width = '0%';
      strengthText.textContent = 'Força da senha';
      strengthMeter.className = 'strength-meter-fill';
      return;
    }

    const hasLength = password.length >= 8;
    const hasUppercase = /[A-Z]/.test(password);
    const hasLowercase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    let strength = 0;
    if (hasLength) strength += 20;
    if (hasUppercase) strength += 20;
    if (hasLowercase) strength += 20;
    if (hasNumber) strength += 20;
    if (hasSpecial) strength += 20;

    strengthMeter.style.width = `${strength}%`;

    if (strength < 40) {
      strengthMeter.className = 'strength-meter-fill strength-weak';
      strengthText.textContent = 'Fraca';
      strengthText.className = 'strength-text text-weak';
    } else if (strength < 80) {
      strengthMeter.className = 'strength-meter-fill strength-medium';
      strengthText.textContent = 'Média';
      strengthText.className = 'strength-text text-medium';
    } else {
      strengthMeter.className = 'strength-meter-fill strength-strong';
      strengthText.textContent = 'Forte';
      strengthText.className = 'strength-text text-strong';
    }
  }

  togglePasswordVisibility(field: 'senha' | 'confirmacaoSenha'): void {
    if (field === 'senha') {
      this.showPassword = !this.showPassword;
      const input = document.getElementById('senha') as HTMLInputElement;
      if (input) {
        input.type = this.showPassword ? 'text' : 'password';
      }
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
      const input = document.getElementById(
        'confirmacaoSenha'
      ) as HTMLInputElement;
      if (input) {
        input.type = this.showConfirmPassword ? 'text' : 'password';
      }
    }
  }

  previewSelectedImage(event: any): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.selectedFotoDePerfil = event.target.files[0];
    if (file) {
      const reader = new FileReader();

      reader.onload = (e: ProgressEvent<FileReader>) => {
        const result = e.target?.result as string;

        this.avatarImage.nativeElement.src = result;
        this.avatarImage.nativeElement.style.display = 'block';

        this.avatarPlaceholder.nativeElement.style.display = 'none';
      };

      reader.readAsDataURL(file);
    }
  }

  triggerFileInput(): void {
    this.avatarUpload.nativeElement.click();
  }

  onSubmit() {
    this.register();
  }
  currentStep: number = 1;
  progressWidth: string = '33%';

  nextStep(): void {
    if (this.currentStep < 3) {
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
  hasError(controlName: string, errorType: string): boolean {
    const control = this.authForm.get(controlName);
    return control ? control.hasError(errorType) && (control.dirty || control.touched) : false;
  }
}
