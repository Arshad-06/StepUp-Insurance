import {
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';

import { ApiService } from '../services/api.service';

import { catchError, map, Observable, of, switchMap, timer } from 'rxjs';

export class EmailValidators {
  static checkEmail(api: ApiService, requireExists: boolean): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const parent = control.parent;

      if (!parent || !control.value) {
        return of(null);
      }

      const roleSelected = parent.get('role')?.value;

      if (!roleSelected) {
        return of(null);
      }

      const userType = roleSelected === 'AGENT' ? 'a' : 'c';

      return timer(500).pipe(
        switchMap(() => api.checkEmailExists(control.value, userType)),

        map((exists: boolean) => {
          if (requireExists && !exists) {
            return { emailNotFound: true };
          }

          if (!requireExists && exists) {
            return { emailTaken: true };
          }

          return null;
        }),

        catchError(() => of(null)),
      );
    };
  }
}