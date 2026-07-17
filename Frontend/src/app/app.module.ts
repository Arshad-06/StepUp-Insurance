import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NgChartsModule } from 'ng2-charts';
import { StoreModule } from '@ngrx/store';
import { ReactiveFormsModule } from '@angular/forms';
import { NavbarComponent } from './core/components/navbar/navbar.component';
import { ProfileDrawerComponent } from './features/profile-drawer/profile-drawer.component';
import { SharedModule } from './shared/shared.module';
import { authReducer } from './store/auth.reducer';
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';
import { LandingPageComponent } from './features/landing-page/landing-page.component';


@NgModule({
  declarations: [
    AppComponent,

    NavbarComponent,

    LandingPageComponent,

    ProfileDrawerComponent,
  ],

  imports: [
    BrowserModule,

    AppRoutingModule,

    HttpClientModule,

    NgChartsModule,

    ReactiveFormsModule,

    SharedModule,

    StoreModule.forRoot({ auth: authReducer }),
  ],

  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
  ],

  bootstrap: [AppComponent],
})
export class AppModule {}