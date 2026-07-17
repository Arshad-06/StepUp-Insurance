import { Component } from '@angular/core';

import { Store } from '@ngrx/store';

import { Observable } from 'rxjs';

import { AuthState } from 'src/app/store/auth.reducer';

@Component({
  selector: 'app-landing-page',

  templateUrl: './landing-page.component.html',
})
export class LandingPageComponent {
  isAuthenticated$: Observable<boolean>;

  role$: Observable<'AGENT' | 'CUSTOMER' | null>;

  constructor(private readonly store: Store<{ auth: AuthState }>) {
    this.isAuthenticated$ = this.store.select(
      (state) => state.auth.isAuthenticated,
    );

    this.role$ = this.store.select((state) => state.auth.role);
  }
}