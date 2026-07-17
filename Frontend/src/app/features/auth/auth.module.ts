import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing.module';

import { LoginComponent } from './login/login.component';

import {
  FormsModule,
  ReactiveFormsModule,
  ɵInternalFormsSharedModule,
} from '@angular/forms';

import { SharedModule } from 'src/app/shared/shared.module';

import { RegisterComponent } from './register/register.component';

@NgModule({
  declarations: [LoginComponent, RegisterComponent],

  imports: [
    CommonModule,

    AuthRoutingModule,

    ReactiveFormsModule,

    ɵInternalFormsSharedModule,

    FormsModule,

    SharedModule,
  ],
})
export class AuthModule {}