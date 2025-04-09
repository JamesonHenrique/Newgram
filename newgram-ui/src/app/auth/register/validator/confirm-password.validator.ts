import { AbstractControl, ValidatorFn } from '@angular/forms';

export function confirmPasswordValidator(passwordControlName: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (!control.parent) {
      return null;
    }

    const passwordControl = control.parent.get(passwordControlName);
    if (!passwordControl) {
      return null;
    }

    const password = passwordControl.value;
    const confirmPassword = control.value;

    return password === confirmPassword ? null : { passwordsNotMatch: true };
  };
}