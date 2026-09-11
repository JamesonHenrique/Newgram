import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';
import { ModeracaoService } from '../services/services';
import { DenunciaDto } from '../services/fn/moderacao';

type StatusFiltro = '' | 'ABERTA' | 'EM_ANALISE' | 'RESOLVIDA' | 'REJEITADA';

@Component({
  selector: 'app-moderacao',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './moderacao.component.html',
  styleUrl: './moderacao.component.css',
})
export class ModeracaoComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();

  filtro: StatusFiltro = 'ABERTA';
  denuncias: DenunciaDto[] = [];
  pagina = 0;
  temMais = true;
  carregando = false;
  semPermissao = false;

  constructor(private title: Title, private moderacaoService: ModeracaoService) {
    this.title.setTitle('Moderação');
  }

  ngOnInit(): void {
    this.carregar(true);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  trocarFiltro(filtro: StatusFiltro): void {
    this.filtro = filtro;
    this.carregar(true);
  }

  carregar(recomecar = false): void {
    if (this.carregando) {
      return;
    }
    if (recomecar) {
      this.pagina = 0;
      this.denuncias = [];
      this.temMais = true;
      this.semPermissao = false;
    }
    if (!this.temMais) {
      return;
    }
    this.carregando = true;
    this.moderacaoService
      .listarFila({
        status: (this.filtro || undefined) as 'ABERTA' | 'EM_ANALISE' | 'RESOLVIDA' | 'REJEITADA' | undefined,
        pageable: { page: this.pagina, size: 20, sort: [''] },
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          const itens = (page.content as DenunciaDto[] | undefined) || [];
          this.denuncias = [...this.denuncias, ...itens];
          const last = (page as any).last as boolean | undefined;
          this.temMais = last === undefined ? itens.length === 20 : !last;
          this.pagina += 1;
          this.carregando = false;
        },
        error: (err) => {
          this.carregando = false;
          this.semPermissao = err?.status === 403;
        },
      });
  }

  resolver(denuncia: DenunciaDto, status: 'EM_ANALISE' | 'RESOLVIDA' | 'REJEITADA', event: Event): void {
    event.stopPropagation();
    if (!denuncia.id) {
      return;
    }
    this.moderacaoService
      .resolverDenuncia({ id: denuncia.id, status })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (atualizada) => {
          this.denuncias = this.denuncias.map((d) => (d.id === atualizada.id ? atualizada : d));
        },
        error: () => {},
      });
  }
}
