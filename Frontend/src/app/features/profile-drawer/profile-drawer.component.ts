import { Component, OnInit, Output, EventEmitter } from '@angular/core';

import {
  AbstractControl,
  AsyncValidatorFn,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';

import { ApiService } from '../../core/services/api.service';

import { ToastService } from '../../shared/services/toast.service';

import { EmailValidators } from 'src/app/core/validators/email.validators';

import { Store } from '@ngrx/store';

import { AuthState } from 'src/app/store/auth.reducer';

import { of } from 'rxjs';

import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-profile-drawer',

  templateUrl: './profile-drawer.component.html',
})
export class ProfileDrawerComponent implements OnInit {
  @Output() closeDrawer = new EventEmitter<void>();

  profileForm!: FormGroup;

  profileData: any = null;

  isSendingOtp = false;

  isOtpSent = false;

  isLoading: boolean = true;

  isUpdating: boolean = false;

  isEditMode: boolean = false;

  showOtpModal = false;

  constructor(
    private readonly fb: FormBuilder,

    private readonly apiService: ApiService,

    private readonly toastr: ToastService,

    private readonly authService: AuthService,

    private readonly store: Store<{ auth: AuthState }>,
  ) {}

  ignoreCurrentEmailValidator(): AsyncValidatorFn {
    return (control: AbstractControl) => {
      if (this.profileData && control.value === this.profileData.email) {
        return of(null);
      }

      return EmailValidators.checkEmail(this.apiService, false)(control);
    };
  }

  ngOnInit(): void {
    // Initialize the form for the updatable fields

    this.profileForm = this.fb.group({
      name: [
        '',
        [
          Validators.required,
          Validators.pattern('^[A-Z][A-Za-z]+( [A-Z][A-Za-z]+){0,2}$'),
        ],
      ],

      email: ['', [Validators.email], [this.ignoreCurrentEmailValidator()]],

      contactInfo: [
        '',
        [Validators.required, Validators.pattern(String.raw`^[6789]\d{9}$`)],
      ],

      password: [
        '',
        Validators.pattern('(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*+=!]).{8,}$'),
      ],

      role: [{ value: '', disabled: true }],
    });

    this.fetchProfile();

    this.store
      .select((state) => state.auth.role)
      .subscribe((role) => {
        if (role) {
          this.profileForm.patchValue({ role: role });

          this.profileForm.get('email')?.updateValueAndValidity();
        }
      });
  }

  fetchProfile() {
    this.isLoading = true;

    this.apiService.getProfile().subscribe({
      next: (res: any) => {
        this.profileData = res;

        // Patch the updatable fields into the form

        this.profileForm.patchValue({
          name: this.profileData.name,

          email: this.profileData.email,

          contactInfo: this.profileData.contact,
        });

        this.isLoading = false;
      },

      error: (err) => {
        this.isLoading = false;

        this.toastr.showError('Failed To Load Profile!');

        this.close();
      },
    });
  }

  toggleEditMode() {
    this.isEditMode = !this.isEditMode;

    // If they cancel edit, revert form to original data

    if (!this.isEditMode && this.profileData) {
      this.profileForm.patchValue({
        name: this.profileData.name,

        email: this.profileData.email,

        contactInfo: this.profileData.contact,

        password: '',
      });
    }
  }

  // 1. Helper to extract ONLY the fields that differ from the original data

  getChangedFields(): any {
    const delta: any = {};

    const formValues = this.profileForm.value;

    if (formValues.name !== this.profileData.name) {
      delta.name = formValues.name;
    }

    if (formValues.email !== this.profileData.email) {
      delta.email = formValues.email;
    }

    // Mapping the form's 'contactInfo' back to the backend's 'contact' field

    if (formValues.contactInfo !== this.profileData.contact) {
      delta.contact = formValues.contactInfo;
    }

    // Password is only included if they actually typed something

    if (formValues.password) {
      delta.password = formValues.password;
    }

    return delta;
  }

  // 2. Dynamic getter to check if we have any actual changes

  get hasChanges(): boolean {
    if (!this.profileData) return false;

    return Object.keys(this.getChangedFields()).length > 0;
  }

  restrictToNumbers(event: KeyboardEvent) {
    const charCode = event.key.codePointAt(0) as number;

    // Allow only digits

    if (charCode < 48 || charCode > 57) {
      event.preventDefault();
    }
  }

  // 3. Submit Method

  onUpdate() {
    if (this.profileForm.invalid || !this.hasChanges) return;

    this.isUpdating = true;

    // Extract ONLY the changed fields

    const deltaPayload = this.getChangedFields();

    this.apiService.updateProfile(deltaPayload).subscribe({
      next: (res) => {
        this.isUpdating = false;

        this.toastr.showSuccess('Profile Updated Successfully!');

        this.profileData.name = res.name;

        this.profileData.email = res.email;

        this.profileData.contact = res.contact;

        this.profileForm.get('password')?.reset();

        if (this.profileData.role === 'AGENT') {
          this.authService.saveSession(
            res.agentId as number,
            'AGENT',
            res.token,
            res.name,
          );
        } else {
          this.authService.saveSession(
            res.customerId as number,
            'CUSTOMER',
            res.token,
            res.name,
          );
        }

        this.toggleEditMode();
      },

      error: (err) => {
        this.isUpdating = false;

        this.toastr.showError('Failed To Update Profile!');
      },
    });
  }

  sendOtp() {
    let email = this.profileForm.get('email')?.value;

    console.log(email);

    if (!email) {
      email = this.profileData.email;
    }

    this.isSendingOtp = true;

    this.apiService.generateOtp(email).subscribe({
      next: () => {
        this.isSendingOtp = false;

        this.isOtpSent = true;

        this.showOtpModal = true;
      },

      error: () => {
        this.isSendingOtp = false;

        alert('Unable to send OTP');
      },
    });
  }

  verifyOtp(otp: string) {
    let email = this.profileForm.get('email')?.value;

    if (!email) {
      email = this.profileData.email;
    }

    this.apiService.verifyOtp({ email, otp }).subscribe({
      next: () => {
        this.showOtpModal = false;

        this.toastr.showSuccess('OTP Verified Successfully!');

        this.onUpdate();
      },

      error: () => {
        this.toastr.showError('Invalid OTP Entered!');
      },
    });
  }

  close() {
    this.closeDrawer.emit();
  }

  closeOtp() {
    this.isOtpSent = false;

    this.isSendingOtp = false;

    this.showOtpModal = false;
  }
}