import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { map, take } from 'rxjs';

import { AuthState } from 'src/app/store/auth.reducer';

export const authGuard: CanActivateFn = (route, state) => {
  const store = inject(Store<{ auth: AuthState }>);

  const router = inject(Router);

  const expectedRole = route.data['role'];

  return store
    .select((s) => s.auth)
    .pipe(
      take(1),

      map((authState) => {
        // Check if user is logged in

        if (!authState.isAuthenticated) {
          router.navigate(['/auth/login']);

          return false;
        }

        // Check if user has correct role

        if (expectedRole && authState.role !== expectedRole) {
          router.navigate(['/auth/login']);

          return false;
        }

        return true;
      }),
    );
};