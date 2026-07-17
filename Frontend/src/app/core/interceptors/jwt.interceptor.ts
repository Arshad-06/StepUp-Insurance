import { Injectable } from '@angular/core';

import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';

import { catchError, Observable, throwError } from 'rxjs';

import { Store } from '@ngrx/store';

import { Router } from '@angular/router';

import { ToastService } from 'src/app/shared/services/toast.service';

import * as AuthActions from '../../store/auth.actions';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(
    private readonly store: Store,
    private readonly router: Router,
    private readonly toastService: ToastService,
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler,
  ): Observable<HttpEvent<unknown>> {
    // Grab token from local storage

    const sessionData = localStorage.getItem('newgen_auth_session');

    if (sessionData) {
      const { token } = JSON.parse(sessionData);

      if (token) {
        // Clone request and add Authorization header

        request = request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        });
      }
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          localStorage.removeItem('newgen_auth_session');

          this.toastService.showError('Your Session Has Expired!');

          this.store.dispatch(AuthActions.logout());

          this.router.navigate(['/auth/login']);
        } else if (error.status === 403) {
          this.toastService.showError('Access Denied!');
        }

        return throwError(() => error);
      }),
    );
  }
}