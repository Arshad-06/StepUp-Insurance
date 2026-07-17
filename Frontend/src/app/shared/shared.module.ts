import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OtpModalComponent } from './components/otp-modal/otp-modal.component';

@NgModule({
  declarations: [OtpModalComponent],

  imports: [CommonModule, FormsModule],

  exports: [OtpModalComponent],
})
export class SharedModule {}