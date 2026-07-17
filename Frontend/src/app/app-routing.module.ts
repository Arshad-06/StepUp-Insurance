import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';

import { LandingPageComponent } from './features/landing-page/landing-page.component';

import { authGuard } from './core/guards/auth.guard';

import { guestGuard } from './core/guards/guest.guard';

const routes: Routes = [
  // Default Route (Landing Page)

  {
    path: '',

    component: LandingPageComponent,

    pathMatch: 'full',
  },

  // Auth Module (Lazy Loaded - No Guards)

  {
    path: 'auth',

    loadChildren: () =>
      import('./features/auth/auth.module').then((m) => m.AuthModule),

    canActivate: [guestGuard],
  },

  // Agent Dashboard (Lazy Loaded & Guarded)

  {
    path: 'agent-dashboard',

    loadChildren: () =>
      import('./features/agent-dashboard/agent-dashboard.module').then(
        (m) => m.AgentDashboardModule,
      ),

    canActivate: [authGuard],

    data: { role: 'AGENT' }, // Check state
  },

  // Customer Dashboard (Lazy Loaded & Guarded)

  {
    path: 'customer-dashboard',

    loadChildren: () =>
      import('./features/customer-dashboard/customer-dashboard.module').then(
        (m) => m.CustomerDashboardModule,
      ),

    canActivate: [authGuard],

    data: { role: 'CUSTOMER' }, // Check state
  },

  // Wildcard Fallback

  {
    path: '**',

    redirectTo: '',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],

  exports: [RouterModule],
})
export class AppRoutingModule {}
