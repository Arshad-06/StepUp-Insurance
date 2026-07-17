import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';

import { CustomerDashboardRoutingModule } from './customer-dashboard-routing.module';

import { PolicyListComponent } from './policy-list/policy-list.component';

import { PurchasePolicyComponent } from './purchase-policy/purchase-policy.component';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';

@NgModule({
  declarations: [PolicyListComponent, PurchasePolicyComponent],

  imports: [
    CommonModule,

    ReactiveFormsModule,

    CustomerDashboardRoutingModule,

    FormsModule,
  ],
})
export class CustomerDashboardModule {}