import { Component, EventEmitter, Input, Output } from '@angular/core';
import { catchError, finalize, of, tap } from 'rxjs';
import { AutenticacaoService, UsuariosService } from '../services/services';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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
  contaPrivada = false;
  errorMessage: string | null = null;
  errorMsg: Array<string> = [];
  sucessoMsg: string | null = null;

  segredoTwoFactor: string | null = null;
  uriTwoFactor: string | null = null;
  backupCodesTwoFactor: string[] = [];
  codigoTwoFactor = '';
  twoFactorAtivo = false;

  sessoes: Array<{ jti?: string; dataCriacao?: string; expiracao?: string }> = [];

  chavePix = '';
  tipoConta: 'PESSOAL' | 'CRIADOR' | 'NEGOCIOS' = 'PESSOAL';

  confirmarExclusao = false;
  constructor(
    private fb: FormBuilder,
    private usuariosService: UsuariosService,
    private autenticacaoService: AutenticacaoService,
    private tokenService: TokenService,
    private router: Router
  ) {}
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
    if (this.userProfile && this.editForm) { 
      this.editForm.setValue({
        nome: this.userProfile?.nome,
        bio: this.userProfile?.bio,
        username: this.userProfile?.username
      });
      this.contaPrivada = !!this.userProfile?.privado;
      this.chavePix = this.userProfile?.chavePix || '';
      this.tipoConta = this.userProfile?.tipoConta || 'PESSOAL';
      this.newProfileImage = null;
      this.selectedFile = null;
    }
  }

  alternarPrivacidade(): void {
    const novoValor = !this.contaPrivada;
    this.usuariosService
      .atualizarPrivado({ id: this.userProfile.id, privado: novoValor })
      .pipe(
        tap((atualizado) => {
          this.contaPrivada = !!atualizado?.privado;
          if (this.userProfile) {
            this.userProfile = { ...this.userProfile, privado: this.contaPrivada };
          }
        }),
        catchError((error) => {
          this.errorMessage = 'Erro ao alterar privacidade. Tente novamente.';
          console.error('Erro ao alterar privacidade:', error);
          return of(null);
        })
      )
      .subscribe();
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

  iniciarAtivacaoTwoFactor(): void {
    this.errorMessage = null;
    this.autenticacaoService.ativarTwoFactor().subscribe({
      next: (res) => {
        this.segredoTwoFactor = res?.['segredo'] ?? null;
        this.uriTwoFactor = res?.['uri'] ?? null;
        const backup = res?.['backupCodes'] ?? '';
        this.backupCodesTwoFactor = backup ? backup.split(',') : [];
      },
      error: () => (this.errorMessage = 'Não foi possível iniciar o 2FA.'),
    });
  }

  carregarSessoes(): void {
    this.autenticacaoService.listarSessoes().subscribe({
      next: (sessoes) => (this.sessoes = sessoes || []),
      error: () => (this.sessoes = []),
    });
  }

  revogarSessao(jti: string | undefined): void {
    if (!jti) {
      return;
    }
    this.autenticacaoService.revogarSessao({ jti }).subscribe({
      next: () => {
        this.sessoes = this.sessoes.filter((s) => s.jti !== jti);
      },
      error: () => (this.errorMessage = 'Não foi possível revogar a sessão.'),
    });
  }

  revogarTodasSessoes(): void {
    this.autenticacaoService.revogarTodasSessoes().subscribe({
      next: () => {
        this.sessoes = [];
        this.sucessoMsg = 'Outras sessões encerradas.';
      },
      error: () => (this.errorMessage = 'Não foi possível encerrar as sessões.'),
    });
  }

  confirmarAtivacaoTwoFactor(): void {
    const codigo = this.codigoTwoFactor.trim();
    if (!codigo) {
      return;
    }
    this.autenticacaoService.confirmarTwoFactor({ body: { codigo } }).subscribe({
      next: () => {
        this.twoFactorAtivo = true;
        this.segredoTwoFactor = null;
        this.codigoTwoFactor = '';
        this.sucessoMsg = 'Autenticação em dois fatores ativada.';
      },
      error: () => (this.errorMessage = 'Código inválido.'),
    });
  }

  desativarTwoFactor(): void {
    this.autenticacaoService.desativarTwoFactor().subscribe({
      next: () => {
        this.twoFactorAtivo = false;
        this.sucessoMsg = 'Autenticação em dois fatores desativada.';
      },
      error: () => (this.errorMessage = 'Não foi possível desativar o 2FA.'),
    });
  }

  solicitarVerificacao(): void {
    this.usuariosService.solicitarVerificacao().subscribe({
      next: () => (this.sucessoMsg = 'Solicitação de verificação enviada.'),
      error: () => (this.errorMessage = 'Não foi possível solicitar. Talvez já enviada.'),
    });
  }

  salvarPixETipoConta(): void {
    this.usuariosService
      .atualizarChavePix({ body: { chavePix: this.chavePix || undefined } })
      .subscribe({
        next: () => {
          this.usuariosService
            .atualizarTipoConta({ tipoConta: this.tipoConta })
            .subscribe({
              next: () => {
                this.sucessoMsg = 'Pix e tipo de conta atualizados.';
                this.save.emit();
              },
              error: () => (this.errorMessage = 'Não foi possível salvar.'),
            });
        },
        error: () => (this.errorMessage = 'Não foi possível salvar a chave Pix.'),
      });
  }

  exportarDados(): void {
    this.usuariosService.exportarDados().subscribe({
      next: (dados) => {
        const blob = new Blob([JSON.stringify(dados, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'meus-dados-newgram.json';
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => (this.errorMessage = 'Não foi possível exportar os dados.'),
    });
  }

  excluirConta(): void {
    if (!this.confirmarExclusao) {
      this.confirmarExclusao = true;
      return;
    }
    this.usuariosService.excluirConta().subscribe({
      next: () => {
        this.tokenService.logout();
        this.router.navigate(['/login']);
      },
      error: () => (this.errorMessage = 'Não foi possível excluir a conta.'),
    });
  }
}
