import { createReducer, on } from '@ngrx/store';

import * as AuthActions from './auth.actions';

export interface AuthState {
  userId: number | null;

  role: 'AGENT' | 'CUSTOMER' | null;

  token: string | null;

  isAuthenticated: boolean;

  name: string | null;
}

export const initialState: AuthState = {
  userId: null,

  role: null,

  token: null,

  name: null,

  isAuthenticated: false,
};

export const authReducer = createReducer(
  initialState,

  on(AuthActions.loginSuccess, (state, { userId, role, token, name }) => ({
    ...state,

    userId: userId,

    role: role,

    token: token,

    name: name,

    isAuthenticated: true,
  })),

  on(AuthActions.logout, () => initialState),
);