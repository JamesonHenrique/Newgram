import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'dateFormat',
})
export class DateFormatPipe implements PipeTransform {
  transform(dataString: string | Date): string {
    if (!dataString) return '';

    const data = new Date(dataString);
    const agora = new Date();
    const diffSegundos = Math.floor((agora.getTime() - data.getTime()) / 1000);

    // Calcula as diferenças
    const minutos = Math.floor(diffSegundos / 60);
    const horas = Math.floor(minutos / 60);
    const dias = Math.floor(horas / 24);
    const meses = Math.floor(dias / 30);
    const anos = Math.floor(meses / 12);

    // Verifica se tem hora/minuto na data original
    const temHora = data.getHours() !== 0 || data.getMinutes() !== 0;

    if (anos > 0) {
      return `${anos}a`;
    } else if (meses > 0) {
      return `${meses}m`;
    } else if (dias > 0) {
      if (dias === 1) {
        return 'Há 1 dia';
      } else {
        return temHora ? `${dias}d` : `Há ${dias} dias`;
      }
    } else if (horas > 0) {
      return `${horas}h`;
    } else if (minutos > 0) {
      return `${minutos}min`;
    } else {
      return 'Agora';
    }
  }
}