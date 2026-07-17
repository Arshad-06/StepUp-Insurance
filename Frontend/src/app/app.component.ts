import { Component, OnInit } from '@angular/core';

import { AuthService } from './core/services/auth.service';

import { ToastMessage, ToastService } from './shared/services/toast.service';

@Component({
  selector: 'app-root',

  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  title = 'newgen';

  Name = 'Group-21';

  toast: ToastMessage | null = null;

  private timeoutId: any;

  constructor(
    private readonly authService: AuthService,
    private readonly toastService: ToastService,
  ) {}

  ngOnInit() {
    // Restore Session

    this.authService.restoreSession();

    this.toastService.toastState$.subscribe((toastMessage) => {
      this.toast = toastMessage;

      if (this.timeoutId) {
        clearTimeout(this.timeoutId);
      }

      if (this.toast) {
        this.timeoutId = setTimeout(() => {
          this.toast = null;
        }, 3500);
      }
    });
  }
}
