import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AgentDashboardRoutingModule } from './agent-dashboard-routing.module';
import { NgChartsModule } from 'ng2-charts';
import { AgentDashboardComponent } from './agent-dashboard.component';

@NgModule({
  declarations: [AgentDashboardComponent],

  imports: [CommonModule, NgChartsModule, AgentDashboardRoutingModule],
})
export class AgentDashboardModule {}