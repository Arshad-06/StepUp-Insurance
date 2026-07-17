import { inject } from '@angular/core';

import { CanActivateFn, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { map, take } from 'rxjs';

import { AuthState } from 'src/app/store/auth.reducer';

export const guestGuard: CanActivateFn = () => {
  const store = inject(Store<{ auth: AuthState }>);

  const router = inject(Router);

  return store
    .select((s) => s.auth)
    .pipe(
      take(1),

      map((authState) => {
        // If user is already logged in, block access to auth pages

        if (authState.isAuthenticated) {
          // Route them back to where they belong based on their role

          if (authState.role === 'AGENT') {
            router.navigate(['/agent-dashboard']);
          } else if (authState.role === 'CUSTOMER') {
            router.navigate(['/customer-dashboard']);
          } else {
            router.navigate(['/']);
          }

          return false; // Blocks access
        }

        // If not logged in, allow access

        return true;
      }),
    );
};