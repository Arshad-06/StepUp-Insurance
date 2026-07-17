import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function nomineeCannotBeCustomerValidator(
  customerName: string,
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const nomineeName = control.value;

    if (!customerName || !nomineeName) {
      return null; // Missing Values
    }

    // Convert both to lowercase and check

    if (
      customerName.trim().toLowerCase() === nomineeName.trim().toLowerCase()
    ) {
      // Error

      return { nameMatchError: true };
    }

    // No Error

    return null;
  };
}