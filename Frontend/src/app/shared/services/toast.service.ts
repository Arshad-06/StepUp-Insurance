import { Injectable } from '@angular/core';

import { Subject } from 'rxjs';

export interface ToastMessage {
  message: string;

  type: 'success' | 'error';
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toastSubject = new Subject<ToastMessage | null>();

  public toastState$ = this.toastSubject.asObservable();

  showSuccess(message: string) {
    this.toastSubject.next({ message, type: 'success' });
  }

  showError(message: string) {
    this.toastSubject.next({ message, type: 'error' });
  }

  clear() {
    this.toastSubject.next(null);
  }
}