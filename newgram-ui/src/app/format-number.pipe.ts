import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'formatNumber' })
export class FormatNumberPipe implements PipeTransform {
  transform(value: any): string {
    if (value === undefined || value === null) return '0';

    const num = typeof value === 'string' ? parseInt(value, 10) : value;

    if (num >= 10000000) return `${Math.floor(num / 1000000)}M`;
    if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
    if (num >= 1000) return `${Math.floor(num / 1000)}K`;

    return num.toString();
  }
}