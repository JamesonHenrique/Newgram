import { AbstractControl, ValidatorFn } from '@angular/forms';

export function passwordValidator(): ValidatorFn {

    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (!value) {
        return null;
      }

      const hasLength = value.length >= 8;
      const hasUppercase = /[A-Z]/.test(value);
      const hasLowercase = /[a-z]/.test(value);
      const hasNumber = /[0-9]/.test(value);
      const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value);

      const passwordValid = hasLength && hasUppercase && hasLowercase && hasNumber && hasSpecial;

      return !passwordValid ? { passwordStrength: {
        hasLength,
        hasUppercase,
        hasLowercase,
        hasNumber,
        hasSpecial
      }} : null;
    };

}