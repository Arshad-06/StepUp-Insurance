import { HttpClient } from '@angular/common/http';

import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

export interface UserProfile {
  name: string;

  email: string;

  contact: string;

  role: string;

  customerId?: number;

  agentId?: number;

  token: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly baseUrl = 'https://stepup-insurance.onrender.com';

  constructor(private readonly http: HttpClient) {}

  // --- OTP API Endpoint Mappings --

  generateOtp(email: string): Observable<string> {
    return this.http.get<string>(`${this.baseUrl}/otp/generate-otp/${email}`, {
      responseType: 'text' as 'json',
    });
  }

  verifyOtp(payload: { email: string; otp: string }): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/otp/verify-otp`, payload, {
      responseType: 'text' as 'json',
    });
  }

  checkEmailExists(email: string, type: 'a' | 'c'): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.baseUrl}/otp/check-email?email=${email}&type=${type}`,
    );
  }

  // -- Agent API Mappings --

  registerAgent(agentData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/agent/register`, agentData);
  }

  loginAgent(loginData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/agent/login`, loginData);
  }

  getAgentDashboard(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/agent/dashboard`);
  }

  checkAgentExists(agentId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/agent/check/${agentId}`);
  }

  sendPaymentReminder(policyId: string): Observable<string> {
    return this.http.get(`${this.baseUrl}/agent/remind/${policyId}`, {
      responseType: 'text',
    });
  }

  // -- Customer API Mappings --

  registerCustomer(customerData: any): Observable<any> {
    return this.http.post<any>(
      `${this.baseUrl}/customer/register`,
      customerData,
    );
  }

  loginCustomer(loginData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/customer/login`, loginData);
  }

  getCustomerPolicies(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/customer/policies`);
  }

  //-- Policy API Mappings --

  purchasePolicy(purchaseDto: any): Observable<string> {
    return this.http.post<string>(
      `${this.baseUrl}/policies/purchase`,
      purchaseDto,
      { responseType: 'text' as 'json' },
    );
  }

  renewPolicy(renewDto: any): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/policies/renew`, renewDto, {
      responseType: 'text' as 'json',
    });
  }

  // -- Profile API Mappings --

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/profile/fetch`);
  }

  updateProfile(deltaPayload: any): Observable<UserProfile> {
    return this.http.patch<UserProfile>(
      `${this.baseUrl}/profile/update`,
      deltaPayload,
    );
  }

  resetPassword(payload: {
    email: string;
    resetToken: string;
    newPassword: string;
    role: string;
  }): Observable<any> {
    return this.http.patch(`${this.baseUrl}/profile/reset-password`, payload, {
      responseType: 'text',
    });
  }
}