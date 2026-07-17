import { createAction, props } from '@ngrx/store';

export const loginSuccess = createAction(
  '[Auth] Login Success',

  props<{
    userId: number;
    role: 'AGENT' | 'CUSTOMER';
    token: string;
    name: string;
  }>(),
);

export const logout = createAction('[Auth] Logout');