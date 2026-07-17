import { Component, OnInit } from '@angular/core';

import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import { Router } from '@angular/router';

import { Store } from '@ngrx/store';

import { ApiService } from 'src/app/core/services/api.service';

import { AuthState } from 'src/app/store/auth.reducer';

import { take } from 'rxjs';

import { noPastDateValidator } from '../../../core/validators/date.validators';

import { nomineeCannotBeCustomerValidator } from '../../../core/validators/nominee.validators';

import { ToastService } from 'src/app/shared/services/toast.service';

@Component({
  selector: 'app-purchase-policy',

  templateUrl: './purchase-policy.component.html',
})
export class PurchasePolicyComponent implements OnInit {
  purchaseForm!: FormGroup;

  customerId: number | null = null;

  isSubmitting = false;

  isPolicyTypeMenuOpen = false;

  isPolicyTermMenuOpen = false;

  successMessage = '';

  isDatePickerOpen = false;

  errorMessage = '';

  customerName: string = '';

  today: string = new Date().toISOString().split('T')[0];

  successToastMessage = '';

  constructor(
    private readonly fb: FormBuilder,

    private readonly api: ApiService,

    private readonly store: Store<{ auth: AuthState }>,

    private readonly router: Router,

    private readonly toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.store
      .select('auth')
      .pipe(take(1))
      .subscribe((state) => {
        this.customerId = state.userId;

        this.customerName = state.name ?? '';

        this.initForm();
      });
  }

  initForm() {
    this.purchaseForm = this.fb.group({
      policyType: ['', Validators.required],

      policyStartDate: ['', [Validators.required, noPastDateValidator]],

      policyTerm: [
        '',
        [Validators.required, Validators.min(5), Validators.max(30)],
      ],

      nominee: [
        '',
        [
          Validators.required,
          Validators.pattern('^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$'),
          nomineeCannotBeCustomerValidator(this.customerName),
        ],
      ],

      premiumAmount: [100, [Validators.required, Validators.min(100)]],
    });
  }

  onSubmit(): void {
    if (this.purchaseForm.invalid || !this.customerId) return;

    this.isSubmitting = true;

    this.errorMessage = '';

    this.successMessage = '';

    const { policyType, policyStartDate, policyTerm, nominee, premiumAmount } =
      this.purchaseForm.value;

    const purchaseDto = {
      policyType,
      policyStartDate,
      policyTerm,
      nominee,
      premiumAmount,
    };

    this.api.purchasePolicy(purchaseDto).subscribe({
      next: () => {
        this.isSubmitting = false;

        this.successMessage =
          'Policy purchased successfully! Redirecting to dashboard...';

        this.successToastMessage = 'Policy Purchased Successfully!';

        this.toastService.showSuccess(this.successToastMessage);

        this.router.navigate(['/customer-dashboard']);
      },

      error: () => {
        this.isSubmitting = false;

        console.log(purchaseDto);

        this.errorMessage = 'Purchase Failed! Please Try Again';

        this.toastService.showError(this.errorMessage);

        console.log(this.errorMessage);
      },
    });
  }

  onPolicyTypeBlur() {
    this.isPolicyTypeMenuOpen = false;

    if (!this.purchaseForm.get('policyType')?.value) {
      this.purchaseForm.get('policyType')?.markAsTouched();
    }
  }

  onPolicyTermBlur() {
    this.isPolicyTermMenuOpen = false;

    if (!this.purchaseForm.get('policyTerm')?.value) {
      this.purchaseForm.get('policyTerm')?.markAsTouched();
    }
  }

  togglePolicyTypeMenu() {
    if (this.isPolicyTypeMenuOpen) {
      if (!this.purchaseForm.get('policyType')?.value) {
        this.purchaseForm.get('policyType')?.markAsTouched();
      }
    }

    this.isPolicyTypeMenuOpen = !this.isPolicyTypeMenuOpen;
  }

  onDateBlur() {
    this.isDatePickerOpen = false;

    this.purchaseForm.get('policyStartDate')?.markAsTouched();
  }

  togglePolicyTermMenu() {
    if (this.isPolicyTermMenuOpen) {
      if (!this.purchaseForm.get('policyTerm')?.value) {
        this.purchaseForm.get('policyTerm')?.markAsTouched();
      }
    }

    this.isPolicyTermMenuOpen = !this.isPolicyTermMenuOpen;
  }
}