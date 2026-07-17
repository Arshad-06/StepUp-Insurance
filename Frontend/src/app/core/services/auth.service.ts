import { Injectable } from '@angular/core';

import { Store } from '@ngrx/store';

import * as AuthActions from '../../store/auth.actions';

import { jwtDecode } from 'jwt-decode';

import { ToastService } from 'src/app/shared/services/toast.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly STORAGE_KEY = 'newgen_auth_session';

  isLoggingIn: boolean = false;

  constructor(
    private readonly store: Store,
    private readonly toastService: ToastService,
  ) {}

  // Save Session To Browser Storage

  saveSession(
    userId: number,
    role: 'AGENT' | 'CUSTOMER',
    token: string,
    name: string,
  ) {
    const session = { userId, role, token, name };

    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(session));
  }

  // Clear Session On Logout

  clearSession() {
    localStorage.removeItem(this.STORAGE_KEY);
  }

  // Restore Session When App Is Refreshed

  restoreSession() {
    const storedSession = localStorage.getItem(this.STORAGE_KEY);

    if (storedSession) {
      const { userId, role, token, name } = JSON.parse(storedSession);

      try {
        const decodedToken = jwtDecode(token);

        const currentTime = Math.floor(Date.now() / 1000);

        if (decodedToken.exp && decodedToken.exp < currentTime) {
          this.toastService.showError('Your Session Has Expired!');

          this.clearSession();

          return;
        }

        // Dispatch back to ngRx state

        this.store.dispatch(
          AuthActions.loginSuccess({ userId, role, token, name }),
        );
      } catch (error) {
        console.error('Session Invalid:', error);

        this.toastService.showError('Invalid Session. Logging Out Securely...');

        this.clearSession();
      }
    }
  }
}