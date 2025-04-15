import { AbstractControl, ValidatorFn } from '@angular/forms';

export function confirmPasswordValidator(passwordControlName: string): ValidatorFn {
  return (control: AbstractControl): { [key: string]: any } | null => {
    const passwordControl = control.parent?.get(passwordControlName);

    if (!passwordControl || !control.value) {
      return null;
    }

    if (passwordControl.value !== control.value) {
      return {
        passwordMismatch: {
          expected: passwordControl.value,
          actual: control.value
        }
      };
    }

    return null;
  };
}