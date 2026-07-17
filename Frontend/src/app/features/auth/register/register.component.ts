import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { ApiService } from 'src/app/core/services/api.service';

import { AuthService } from 'src/app/core/services/auth.service';

import { agentExistsValidator } from 'src/app/core/validators/agent.validators';

import { EmailValidators } from 'src/app/core/validators/email.validators';

import { ToastService } from 'src/app/shared/services/toast.service';

import * as AuthActions from 'src/app/store/auth.actions';

@Component({
  selector: 'app-register',

  templateUrl: './register.component.html',
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;

  isCustomer: boolean = false;

  isOtpSent = false;

  showOtpModal = false;

  isEmailVerified = false;

  isSubmitting = false;

  isSendingOtp = false;

  errorMessage: string | null = null;

  successMessage: string = '';

  formHeading: string = '';

  constructor(
    private readonly fb: FormBuilder,

    private readonly api: ApiService,

    private readonly route: ActivatedRoute,

    private readonly store: Store,

    private readonly router: Router,

    private readonly authService: AuthService,

    private readonly toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe((data) => {
      this.isCustomer = data['role'] === 'CUSTOMER';

      this.formHeading = this.isCustomer
        ? 'Sign Up As Customer'
        : 'Sign Up As Agent';
    });

    this.registerForm = this.fb.group({
      name: [
        { value: '', disabled: true },
        [
          Validators.required,
          Validators.pattern('^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$'),
        ],
      ],

      email: [
        '',
        [Validators.required, Validators.email],
        [EmailValidators.checkEmail(this.api, false)],
      ],

      contact: [
        { value: '', disabled: true },
        [Validators.required, Validators.pattern('^[6-9][0-9]{9}')],
      ],

      role: [{ value: '', disabled: true }],

      agentId: [{ value: '', disabled: true }],

      password: [
        { value: '', disabled: true },
        [
          Validators.required,
          Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$'),
        ],
      ],
    });

    if (this.isCustomer) {
      this.registerForm.get('role')?.setValue('CUSTOMER');

      this.registerForm.get('agentId')?.setValidators([Validators.required]);

      this.registerForm
        .get('agentId')
        ?.setAsyncValidators([agentExistsValidator(this.api)]);

      this.registerForm.get('agentId')?.updateValueAndValidity();
    } else {
      this.registerForm.get('role')?.setValue('AGENT');
    }
  }

  sendOtp() {
    const email = this.registerForm.get('email')?.value;

    this.isSendingOtp = true;

    console.log(email);

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

  close() {
    this.isOtpSent = false;

    this.isSendingOtp = false;

    this.showOtpModal = false;

    this.registerForm.reset();

    if (this.isCustomer) {
      this.registerForm.get('role')?.setValue('CUSTOMER');
    } else {
      this.registerForm.get('role')?.setValue('AGENT');
    }
  }

  verifyOtp(otp: string) {
    const email = this.registerForm.get('email')?.value;

    this.api.verifyOtp({ email, otp }).subscribe({
      next: () => {
        this.isEmailVerified = true;

        this.showOtpModal = false;

        this.registerForm.get('password')?.enable();

        this.registerForm.get('contact')?.enable();

        if (this.isCustomer) {
          this.registerForm.get('agentId')?.enable();
        }

        this.registerForm.get('name')?.enable();

        this.successMessage = 'OTP Verified Successfully!';

        this.toastService.showSuccess(this.successMessage);
      },

      error: () => {
        this.errorMessage = 'Invalid OTP Entered!';

        console.error(this.errorMessage);

        this.toastService.showError(this.errorMessage);
      },
    });
  }

  restrictToNumbers(event: KeyboardEvent) {
    const charCode = event.key.codePointAt(0) as number;

    // Allow only digits

    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
    }
  }

  resendOtp() {
    this.isSendingOtp = false;

    this.errorMessage = '';

    this.sendOtp();
  }

  onSubmit() {
    if (this.registerForm.valid && this.isEmailVerified) {
      this.isSubmitting = true;

      const data = this.registerForm.getRawValue();

      if (this.isCustomer) {
        this.api.registerCustomer(data).subscribe({
          next: (res: any) => {
            this.isSubmitting = false;

            if (res.userId) {
              this.store.dispatch(
                AuthActions.loginSuccess({
                  userId: res.userId,

                  role: res.role,

                  token: res.token,

                  name: res.name,
                }),
              );

              this.authService.saveSession(
                res.userId,
                res.role,
                res.token,
                res.name,
              );

              this.successMessage = 'Customer Registered Successfully!';

              this.toastService.showSuccess(this.successMessage);

              this.router.navigate(['/customer-dashboard']);
            }
          },

          error: () => {
            this.isSubmitting = false;

            this.errorMessage = 'Registration Failed. Please Try Again!';

            this.toastService.showError(this.errorMessage);

            this.isEmailVerified = false;

            this.isOtpSent = false;

            this.registerForm.reset();

            this.registerForm.get('role')?.setValue('CUSTOMER');
          },
        });
      } else {
        this.api.registerAgent(data).subscribe({
          next: (res: any) => {
            this.isSubmitting = false;

            this.errorMessage = null;

            this.successMessage = 'Agent Registered Successfully';

            this.toastService.showSuccess(this.successMessage);

            if (res.userId) {
              this.store.dispatch(
                AuthActions.loginSuccess({
                  userId: res.userId,

                  role: res.role,

                  token: res.token,

                  name: res.name,
                }),
              );

              this.authService.saveSession(
                res.userId,
                res.role,
                res.token,
                res.name,
              );

              // Route To Correct Page

              this.router.navigate(['/agent-dashboard']);
            }
          },

          error: () => {
            this.isSubmitting = false;

            this.errorMessage = 'Registration Failed. Please Try Again!';

            this.toastService.showError(this.errorMessage);

            this.isEmailVerified = false;

            this.isOtpSent = false;

            this.registerForm.reset();

            this.registerForm.get('role')?.setValue('AGENT');
          },
        });
      }
    }
  }
}