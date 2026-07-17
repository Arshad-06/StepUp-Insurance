import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const noPastDateValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  if (!control.value) {
    // If null, let required handle it

    return null;
  }

  const selectedDate = new Date(control.value);

  const today = new Date();

  // Reset time to midnight so user is still allowed to pick 'today'

  today.setHours(0, 0, 0, 0);

  // Compare Date

  if (selectedDate < today) {
    return { pastDate: true }; // pastDate error
  }

  // No Error -> Valid Date

  return null;
};
