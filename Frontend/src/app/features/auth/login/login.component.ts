import { Component } from '@angular/core';

import * as AuthActions from '../../../store/auth.actions';

import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';

import { ApiService } from 'src/app/core/services/api.service';

import { Store } from '@ngrx/store';

import { Router } from '@angular/router';

import { AuthService } from 'src/app/core/services/auth.service';

import { EmailValidators } from 'src/app/core/validators/email.validators';

import { ToastService } from 'src/app/shared/services/toast.service';

@Component({
  selector: 'app-login',

  templateUrl: './login.component.html',
})
export class LoginComponent {
  loginForm: FormGroup;

  isLoading = false;

  isOtpSent = false;

  isEmailVerified = false;

  showOtpModal = false;

  errorMessage: string | null = null;

  successMessage: string = '';

  isSendingOtp = false;

  isRoleMenuOpen: boolean = false;

  isResetMode: boolean = false;

  securedResetToken: string | null = null;

  newPasswordCtrl = new FormControl('', [
    Validators.required,
    Validators.pattern('(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*+=!]).{8,}$'),
  ]);

  confirmPasswordCtrl = new FormControl('', [Validators.required]);

  constructor(
    private readonly fb: FormBuilder,

    private readonly api: ApiService,

    private readonly store: Store,

    private readonly router: Router,

    private readonly authService: AuthService,

    private readonly toastService: ToastService,
  ) {
    this.loginForm = this.fb.group({
      email: [
        '',
        [Validators.required, Validators.email],
        [EmailValidators.checkEmail(this.api, true)],
      ],

      password: [{ value: '', disabled: true }, Validators.required],

      role: ['', Validators.required],
    });

    // Re-do validation if user changes role after getting error

    this.loginForm.get('role')?.valueChanges.subscribe(() => {
      this.loginForm.get('email')?.updateValueAndValidity();
    });
  }

  sendOtp() {
    const email = this.loginForm.get('email')?.value;

    if (email) {
      this.isSendingOtp = true;

      this.api.generateOtp(email).subscribe({
        next: () => {
          this.isSendingOtp = false;

          this.isOtpSent = true;

          this.showOtpModal = true;
        },

        error: (err) => {
          this.isSendingOtp = false;

          console.error(err);
        },
      });
    }
  }

  close() {
    this.isOtpSent = false;

    this.isSendingOtp = false;

    this.showOtpModal = false;

    this.loginForm.reset();
  }

  verifyOtp(otp: string) {
    const email = this.loginForm.get('email')?.value;

    this.api.verifyOtp({ email, otp }).subscribe({
      next: (res: any) => {
        this.errorMessage = null;

        this.successMessage = 'OTP Verified Successfully!';

        this.toastService.showSuccess(this.successMessage);

        this.isEmailVerified = true;

        this.showOtpModal = false;

        this.securedResetToken = res;

        this.loginForm.get('password')?.enable();
      },

      error: () => {
        this.errorMessage = 'Invalid OTP Entered!';

        console.error(this.errorMessage);

        this.toastService.showError(this.errorMessage);
      },
    });
  }

  isResetInvalid(): boolean {
    return (
      this.newPasswordCtrl.invalid ||
      this.confirmPasswordCtrl.invalid ||
      this.newPasswordCtrl.value !== this.confirmPasswordCtrl.value
    );
  }

  toggleResetMode() {
    this.isResetMode = !this.isResetMode;

    if (!this.isResetMode) {
      this.newPasswordCtrl.reset();

      this.confirmPasswordCtrl.reset();
    }
  }

  onResetPasswordSubmit() {
    this.isLoading = true;

    if (this.isResetInvalid()) return;

    const payload = {
      email: this.loginForm.get('email')?.value,

      resetToken: this.securedResetToken as string,

      role: this.loginForm.get('role')?.value,

      newPassword: this.newPasswordCtrl.value as string,
    };

    this.api.resetPassword(payload).subscribe({
      next: (res: any) => {
        this.isLoading = false;

        this.toastService.showSuccess('Password Updated Successfully!');

        this.toggleResetMode();

        this.loginForm.get('password')?.reset();

        this.securedResetToken = null;
      },

      error: (err: any) => {
        this.isLoading = false;

        this.toastService.showError('Password Update Failed! Please Try Again');
      },
    });
  }

  onSubmit() {
    this.isLoading = true;

    if (this.loginForm.valid && this.isEmailVerified) {
      const { email, password, role } = this.loginForm.value;

      const loginPayload = { email, password, role };

      const authObserver = {
        next: (response: any) => {
          //Extract User ID From API Response

          // const parsedUserId = parseInt(response.replace(/\D/g, '')) || 0;

          this.isLoading = false;

          this.errorMessage = null;

          if (response.role === 'AGENT') {
            this.successMessage = 'Agent Logged In Successfully!';
          } else {
            this.successMessage = 'Customer Logged In Successfully!';
          }

          this.toastService.showSuccess(this.successMessage);

          // Save Session

          this.authService.saveSession(
            response.userId,
            response.role,
            response.token,
            response.name,
          );

          // Save State

          this.store.dispatch(
            AuthActions.loginSuccess({
              userId: response.userId,

              role: response.role,

              token: response.token,

              name: response.name,
            }),
          );

          if (response.role === 'AGENT') {
            this.router.navigate(['/agent-dashboard']);
          } else {
            this.router.navigate(['/customer-dashboard']);
          }
        },

        error: (err: any) => {
          this.isLoading = false;

          this.errorMessage = 'Login Failed. Please Try Again!';

          this.toastService.showError(this.errorMessage);

          this.loginForm.get('password')?.reset();
        },
      };

      if (role === 'AGENT') {
        this.api.loginAgent(loginPayload).subscribe(authObserver);
      } else {
        this.api.loginCustomer(loginPayload).subscribe(authObserver);
      }
    }
  }
}