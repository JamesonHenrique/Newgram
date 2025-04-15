export interface PasswordValidationResult {
  valid: boolean;
  errors?: {
    hasLength?: boolean;
    hasUppercase?: boolean;
    hasLowercase?: boolean;
    hasNumber?: boolean;
    hasSpecial?: boolean;
  };
}
