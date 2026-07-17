import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';

import { FormControl, Validators } from '@angular/forms';

import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-otp-modal',

  templateUrl: './otp-modal.component.html',
})
export class OtpModalComponent implements OnInit, OnDestroy {
  @Input() email: string = '';

  @Input() timerDuration: number = 90;

  @Output() verify = new EventEmitter<string>();

  @Output() resend = new EventEmitter<void>();

  @Output() closeModal = new EventEmitter<void>();

  otpControl = new FormControl('', [
    Validators.required,
    Validators.minLength(4),
    Validators.maxLength(4),
  ]);

  otpArray: string[] = ['', '', '', ''];

  otpTimer: number = 0;

  displayTime: string = '00:00';

  otpExpired: boolean = false;

  timer: any;

  constructor(private readonly toastService: ToastService) {}

  ngOnInit() {
    this.startOtpTimer();
  }

  ngOnDestroy() {
    this.clearTimer();
  }

  startOtpTimer() {
    this.clearTimer();

    this.otpExpired = false;

    this.otpTimer = this.timerDuration;

    this.updateTimeDisplay();

    this.timer = setInterval(() => {
      if (this.otpTimer <= 0) {
        this.clearTimer();

        this.otpExpired = true;

        this.toastService.showError('OTP Expired!');
      } else {
        this.otpTimer--;

        this.updateTimeDisplay();
      }
    }, 1000);
  }

  private updateTimeDisplay() {
    const minutes = Math.floor(this.otpTimer / 60);

    const seconds = Math.floor(this.otpTimer % 60);

    this.displayTime = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  clearTimer() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  onResendClick() {
    if (this.otpExpired) {
      this.otpArray = ['', '', '', ''];

      this.otpControl.reset();

      this.resend.emit();

      this.startOtpTimer();
    }
  }

  onVerifyClick() {
    if (this.otpControl.valid && !this.otpExpired) {
      this.verify.emit(this.otpControl.value as string);
    }
  }

  onCloseClick() {
    this.closeModal.emit();
  }

  restrictToNumbers(event: any) {
    const pattern = /\d/;

    const inputChar = String.fromCodePoint(event.charCode);

    if (!pattern.test(inputChar)) {
      event.preventDefault();
    }
  }

  onOtpInputChange(index: number, event: any): void {
    const val = event.target.value;

    if (val && index < 3) {
      document.getElementById(`otp_box_${index + 1}`)?.focus();
    }

    this.otpControl.setValue(this.otpArray.join(''));
  }

  onOtpBackspace(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace') {
      event.preventDefault();

      if (this.otpArray[index]) {
        this.otpArray[index] = '';
      } else if (index > 0) {
        const prevBox = document.getElementById(`otp_box_${index - 1}`);

        prevBox?.focus();

        this.otpArray[index - 1] = '';
      }

      this.otpControl.setValue(this.otpArray.join(''));
    }
  }

  onOtpFocus(index: number): void {
    const firstEmptyIndex = this.otpArray.findIndex((val) => !val);

    if (firstEmptyIndex !== -1 && firstEmptyIndex < index) {
      document.getElementById(`otp_box_${firstEmptyIndex}`)?.focus();
    }
  }

  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();

    const pastedData = event.clipboardData?.getData('text') || '';

    const cleanDigits = pastedData.replaceAll(/\D/g, '').slice(0, 4).split('');

    if (cleanDigits.length > 0) {
      for (let i = 0; i < 4; i++) {
        this.otpArray[i] = cleanDigits[i] || '';
      }

      this.otpControl.setValue(this.otpArray.join(''));

      const focusIndex = Math.min(cleanDigits.length - 1, 3);

      document.getElementById(`otp_box_${focusIndex}`)?.focus();
    }
  }
}