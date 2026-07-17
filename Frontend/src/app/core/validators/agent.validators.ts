import {
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';

import { catchError, map, Observable, of, switchMap, timer } from 'rxjs';

import { ApiService } from 'src/app/core/services/api.service';

export function agentExistsValidator(apiService: ApiService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) {
      return of(null);
    }

    // Wait 500ms after user stops typing

    return timer(500).pipe(
      // Switch to API Call

      switchMap(() => apiService.checkAgentExists(control.value)),

      map((exists) => {
        return exists ? null : { agentNotFound: true };
      }),

      catchError(() => of(null)),
    );
  };
}