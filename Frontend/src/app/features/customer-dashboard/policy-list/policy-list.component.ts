import { Component, OnInit } from '@angular/core';

import { Store } from '@ngrx/store';

import { ApiService } from 'src/app/core/services/api.service';

import { Router } from '@angular/router';

import { ToastService } from 'src/app/shared/services/toast.service';

import { delay } from 'rxjs';

@Component({
  selector: 'app-policy-list',

  templateUrl: './policy-list.component.html',
})
export class PolicyListComponent implements OnInit {
  showRenewModal = false;

  selectedPolicyToRenew: any = null;

  isRenewing = false;

  policies: any[] = [];

  customerId: number = 0;

  customerName: string = '';

  activePolicies: any[] = [];

  lapsedPolicies: any[] = [];

  expiredPolicies: any[] = [];

  cardNumber: string = '';

  cvv: string = '';

  cardNumberError: string = '';

  cvvError: string = '';

  successMessage: string = '';

  errorMessage: string | null = null;

  activePage: number = 1;

  lapsedPage: number = 1;

  expiredPage: number = 1;

  itemsPerPage: number = 5;

  filteredActivePolicies: any[] = [];

  filteredLapsedPolicies: any[] = [];

  filteredExpiredPolicies: any[] = [];

  isLoading = false;

  selectedPolicyType: string = 'ALL';

  policyTypes = [
    { label: 'All', value: 'ALL' },

    { label: 'Health', value: 'HEALTH_INSURANCE' },

    { label: 'Life', value: 'LIFE_INSURANCE' },

    { label: 'Vehicle', value: 'VEHICLE_INSURANCE' },

    { label: 'Home', value: 'HOME_INSURANCE' },
  ];

  constructor(
    private readonly api: ApiService,

    private readonly store: Store<{ auth: any }>,

    private readonly router: Router,

    private readonly toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.store.select('auth').subscribe((state) => {
      if (state?.userId && state?.role === 'CUSTOMER') {
        this.customerId = state.userId;

        this.customerName = state.name;
      }
    });

    this.fetchPolicies();
  }

  goToPurchase() {
    this.router.navigate(['/customer-dashboard/purchase']);
  }

  restrictToNumbers(event: KeyboardEvent) {
    const charCode = event.key.codePointAt(0) as number;

    // Allow only digits

    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
    }
  }

  groupPolicies() {
    const today = new Date();

    today.setHours(0, 0, 0, 0);

    const sortedPolicies = [...this.policies].sort(
      (a, b) =>
        new Date(a.lastPremiumPaymentDate).getTime() -
        new Date(b.lastPremiumPaymentDate).getTime(),
    );

    this.expiredPolicies = sortedPolicies.filter((policy) => {
      const endDate = new Date(policy.policyEndDate);

      endDate.setHours(0, 0, 0, 0);

      return endDate.getTime() <= today.getTime();
    });

    this.activePolicies = sortedPolicies.filter((policy) => {
      const endDate = new Date(policy.policyEndDate);

      endDate.setHours(0, 0, 0, 0);

      return endDate > today && policy.policyStatus?.toUpperCase() === 'ACTIVE';
    });

    this.lapsedPolicies = sortedPolicies.filter((policy) => {
      const endDate = new Date(policy.policyEndDate);

      endDate.setHours(0, 0, 0, 0);

      return endDate > today && policy.policyStatus?.toUpperCase() === 'LAPSED';
    });

    this.applyFilter();
  }

  fetchPolicies() {
    this.isLoading = true;

    this.api
      .getCustomerPolicies()
      .pipe(delay(300))
      .subscribe({
        next: (data) => {
          console.log('policies from api:', data);
          this.policies = data;
          this.groupPolicies();

          this.isLoading = false;
        },

        error: () => {
          this.policies = [];

          this.isLoading = false;
        },
      });
  }

  applyFilter(): void {
    if (this.selectedPolicyType === 'ALL') {
      this.filteredActivePolicies = [...this.activePolicies];

      this.filteredLapsedPolicies = [...this.lapsedPolicies];

      this.filteredExpiredPolicies = [...this.expiredPolicies];

      return;
    }

    this.filteredActivePolicies = this.activePolicies.filter(
      (p) => p.policyType === this.selectedPolicyType,
    );

    this.filteredLapsedPolicies = this.lapsedPolicies.filter(
      (p) => p.policyType === this.selectedPolicyType,
    );

    this.filteredExpiredPolicies = this.expiredPolicies.filter(
      (p) => p.policyType === this.selectedPolicyType,
    );
  }

  get paginatedActivePolicies() {
    const start = (this.activePage - 1) * this.itemsPerPage;

    const end = start + this.itemsPerPage;

    return this.filteredActivePolicies.slice(start, end);
  }

  get paginatedLapsedPolicies() {
    const start = (this.lapsedPage - 1) * this.itemsPerPage;

    const end = start + this.itemsPerPage;

    return this.filteredLapsedPolicies.slice(start, end);
  }

  get paginatedExpiredPolicies() {
    const start = (this.expiredPage - 1) * this.itemsPerPage;

    const end = start + this.itemsPerPage;

    return this.filteredExpiredPolicies.slice(start, end);
  }

  get totalActivePages() {
    return Math.ceil(this.filteredActivePolicies.length / this.itemsPerPage);
  }

  get totalLapsedPages() {
    return Math.ceil(this.filteredLapsedPolicies.length / this.itemsPerPage);
  }

  get totalExpiredPages() {
    return Math.ceil(this.filteredExpiredPolicies.length / this.itemsPerPage);
  }

  onPolicyTypeChange(type: string): void {
    this.selectedPolicyType = type;

    this.applyFilter();
  }

  openRenewModal(policy: any) {
    this.selectedPolicyToRenew = policy;

    this.showRenewModal = true;
  }

  closeRenewModal() {
    this.showRenewModal = false;

    this.selectedPolicyToRenew = null;

    this.cardNumber = '';

    this.cvv = '';

    this.cardNumberError = '';

    this.cvvError = '';
  }

  canRenew(policy: any): boolean {
    if (!policy.lastPremiumPaymentDate) {
      return true;
    }

    const lastPayment = new Date(policy.lastPremiumPaymentDate);

    const today = new Date();

    const isPastYear = lastPayment.getFullYear() < today.getFullYear();

    const isPastMonth =
      lastPayment.getFullYear() === today.getFullYear() &&
      lastPayment.getMonth() < today.getMonth();

    if (isPastYear || isPastMonth) {
      return true;
    }

    return false;
  }

  isRenewFormValid(): boolean {
    return (
      this.cardNumber?.length === 16 &&
      this.cvv?.length === 3 &&
      !this.cardNumberError &&
      !this.cvvError
    );
  }

  validateCardNumber() {
    if (this.cardNumber && this.cardNumber.length !== 16) {
      this.cardNumberError = 'Card number must be 16 digits';
    } else {
      this.cardNumberError = '';
    }
  }

  validateCvv() {
    if (this.cvv && this.cvv.length !== 3) {
      this.cvvError = 'CVV must be 3 digits';
    } else {
      this.cvvError = '';
    }
  }

  get hasPolicies(): boolean {
    return (
      (this.activePolicies.length ?? 0) > 0 ||
      (this.lapsedPolicies?.length ?? 0) > 0 ||
      (this.expiredPolicies?.length ?? 0) > 0
    );
  }

  confirmRenewal() {
    if (!this.selectedPolicyToRenew) return;

    this.isRenewing = true;

    this.cardNumberError = '';

    this.cvvError = '';

    let hasError = false;

    if (!this.cardNumber) {
      this.cardNumberError = 'Please enter card number';

      hasError = true;
    } else if (this.cardNumber.length !== 16) {
      this.cardNumberError = 'Card number must be 16 digits';

      hasError = true;
    }

    if (!this.cvv) {
      this.cvvError = 'Please enter CVV';

      hasError = true;
    } else if (this.cvv.length !== 3) {
      this.cvvError = 'CVV must be 3 digits';

      hasError = true;
    }

    if (hasError) {
      return;
    }

    const renewPayload = {
      policyId: this.selectedPolicyToRenew.policyId,

      policyStatus: this.selectedPolicyToRenew.policyStatus,

      cardNumber: this.cardNumber,

      cvv: this.cvv,
    };

    this.api.renewPolicy(renewPayload).subscribe({
      next: (msg) => {
        this.isRenewing = false;

        this.errorMessage = '';

        this.successMessage = 'Policy Renewed Successfully!';

        this.toastService.showSuccess(this.successMessage);

        this.closeRenewModal();

        this.fetchPolicies();
      },

      error: (err) => {
        this.isRenewing = false;

        this.errorMessage = 'Payment Failed! Please Try Again';

        this.toastService.showError(this.errorMessage);

        this.closeRenewModal();

        this.fetchPolicies();
      },
    });
  }

  get hasFilteredPolicies(): boolean {
    return (
      this.filteredActivePolicies.length > 0 ||
      this.filteredLapsedPolicies.length > 0 ||
      this.filteredExpiredPolicies.length > 0
    );
  }
}