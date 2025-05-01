import { Component, EventEmitter, Input, Output } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import { UsuariosService } from '../services/services';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UsuarioUpdateDto } from '../services/models';
import { TokenService } from '../services/token/token.service';

@Component({
  selector: 'app-edit-profile-modal',
  standalone: true,
  imports: [FormsModule, CommonModule, ReactiveFormsModule],
  templateUrl: './edit-profile-modal.component.html',
  styleUrl: './edit-profile-modal.component.css'
})
export class EditProfileModalComponent {
  @Input() isOpen = false;
  @Input() userProfile: any;
  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<any>();

  newProfileImage: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;
  loading = false;
  editForm!: FormGroup;
  errorMessage: string | null = null;
  errorMsg: Array<string> = [];
  constructor(private fb: FormBuilder, private usuariosService: UsuariosService, private tokenService: TokenService) {}
  ngOnInit(): void {
    this.editForm = this.fb.group(
      {
        nome: ['', [Validators.required, Validators.minLength(3)]],
        bio: ['', [Validators.maxLength(150), Validators.required]],
        username: ['', [Validators.required, Validators.minLength(3)]],
      },
    );

  }


  updateProfile() {
    if (this.editForm.invalid) {
      return;
    }

    this.errorMessage = null;
    this.errorMsg = [];

    const editRequest: UsuarioUpdateDto = {
      ...this.editForm.value,
      fotoPerfil: this.selectedFile || undefined,
    };

    this.usuariosService.atualizarUsuario({ body: editRequest, id: this.userProfile.id }).pipe(
      tap(() => {
        this.save.emit();
        this.close.emit();
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
          this.errorMessage = 'Erro ao editar perfil. Tente novamente.';
        }

        console.error('Erro ao editar perfil:', error);

        return of(null);
      })
    ).subscribe();
  }

  ngOnChanges() {
    if (this.userProfile) {
      this.editForm.setValue({
        nome: this.userProfile?.nome,
        bio: this.userProfile?.bio,
        username: this.userProfile?.username
      });
      this.newProfileImage = null;
      this.selectedFile = null;
    }
  }

  handleImageUpload(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.newProfileImage = e.target?.result || null;
      };
      reader.readAsDataURL(file);
    }
  }

  closeModal() {
    this.close.emit();
  }
}
