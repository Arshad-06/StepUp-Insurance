import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';

import { PolicyListComponent } from './policy-list/policy-list.component';

import { PurchasePolicyComponent } from './purchase-policy/purchase-policy.component';

const routes: Routes = [
  { path: '', component: PolicyListComponent },

  { path: 'purchase', component: PurchasePolicyComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],

  exports: [RouterModule],
})
export class CustomerDashboardRoutingModule {}