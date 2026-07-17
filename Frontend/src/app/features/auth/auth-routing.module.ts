import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';

import { RegisterComponent } from './register/register.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: 'register-customer',
    component: RegisterComponent,
    data: { role: 'CUSTOMER' },
  },

  {
    path: 'register-agent',
    component: RegisterComponent,
    data: { role: 'AGENT' },
  },

  { path: '', redirectTo: 'login', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],

  exports: [RouterModule],
})
export class AuthRoutingModule {}