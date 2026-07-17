import { Component, OnInit } from '@angular/core';

import { Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { Observable } from 'rxjs';

import * as AuthActions from '../../../store/auth.actions';

import { AuthState } from '../../../store/auth.reducer';

import { AuthService } from '../../services/auth.service';

import { ToastService } from 'src/app/shared/services/toast.service';

@Component({
  selector: 'app-navbar',

  templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
  isDarkMode: boolean = false;

  isAuthenticated$: Observable<boolean>;

  isMobileMenuOpen: boolean = false;

  role$: Observable<'AGENT' | 'CUSTOMER' | null>;

  isProfileDrawerOpen: boolean = false;

  constructor(
    private readonly store: Store<{ auth: AuthState }>,

    private readonly router: Router,

    private readonly authService: AuthService,

    private readonly toastService: ToastService,
  ) {
    this.isAuthenticated$ = this.store.select(
      (state) => state.auth.isAuthenticated,
    );

    this.role$ = this.store.select((state) => state.auth.role);
  }

  ngOnInit(): void {
    this.initTheme();
  }

  get isAuthScreen(): boolean {
    return (
      this.router.url.includes('/auth') ||
      this.router.url.includes('/customer-dashboard/purchase')
    );
  }

  toggleProfileDrawer() {
    this.isProfileDrawerOpen = !this.isProfileDrawerOpen;
  }

  initTheme(): void {
    const storedTheme = localStorage.getItem('app-theme');

    if (storedTheme === 'dark') {
      this.applyTheme('dark');
    } else {
      this.applyTheme('light');
    }
  }

  toggleDarkMode(): void {
    this.isDarkMode = !this.isDarkMode;

    const targetedTheme = this.isDarkMode ? 'dark' : 'light';

    this.applyTheme(targetedTheme);
  }

  private applyTheme(theme: 'dark' | 'light'): void {
    const htmlTag = document.documentElement;

    if (theme === 'dark') {
      htmlTag.classList.add('dark');

      localStorage.setItem('app-theme', 'dark');

      this.isDarkMode = true;
    } else {
      htmlTag.classList.remove('dark');

      localStorage.setItem('app-theme', 'light');

      this.isDarkMode = false;
    }
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }

  logout() {
    this.authService.clearSession();

    this.toastService.showSuccess('You Have Been Securely Signed Out');

    this.store.dispatch(AuthActions.logout());

    this.closeMobileMenu();

    this.router.navigate(['/']);
  }
}