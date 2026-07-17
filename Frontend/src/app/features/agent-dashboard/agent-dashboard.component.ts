import { KeyValue } from '@angular/common';

import { Component, OnInit } from '@angular/core';

import { Store } from '@ngrx/store';

import { ChartData, ChartType } from 'chart.js';

import jsPDF from 'jspdf';

import autoTable from 'jspdf-autotable';

import { delay, Observable } from 'rxjs';

import { ApiService } from 'src/app/core/services/api.service';

import { ToastService } from 'src/app/shared/services/toast.service';

import { AuthState } from 'src/app/store/auth.reducer';

interface UIFormattedPolicy {
  customerName: string;

  customerContact: string;

  policyId: string;

  policyType: string;

  premiumAmount: number;

  lastPaymentDate: string;

  endDate: string;

  status: string;
}

interface UIDashboardPolicy {
  policyId: string;

  policyType: string;

  premiumAmount: number;

  lastPaymentDate: string;

  endDate: string;

  status: string;
}

interface UICustomerGroup {
  customerName: string;

  customerContact: string;

  policies: UIDashboardPolicy[];
}

@Component({
  selector: 'app-agent-dashboard',

  templateUrl: './agent-dashboard.component.html',
})
export class AgentDashboardComponent implements OnInit {
  // To fetch agent name from ngRx store

  agentName$: Observable<string | null>;

  agentId$: Observable<number | null>;

  // KPI Metrics

  totalCustomers: number = 0;

  totalPoliciesCurrentYear: number = 0;

  annualProfit: number = 0;

  // Table Data

  allCustomerPolicies: UIFormattedPolicy[] = [];

  groupedPolicies: { [key: string]: UICustomerGroup } = {};

  searchTerm: string = '';

  isLoading: boolean = true;

  // Chart.js configuration

  public doughnutChartLabels: string[] = ['Health', 'Life', 'Home', 'Vehicle'];

  public doughnutChartData: ChartData<'doughnut'> = {
    labels: this.doughnutChartLabels,

    datasets: [
      {
        data: [0, 0, 0, 0],

        backgroundColor: ['#10B981', '#3B82F6', '#F59E0B', '#8B5CF6'],

        hoverBackgroundColor: ['#059669', '#2563EB', '#D97706', '#7C3AED'],

        borderWidth: 0,

        hoverOffset: 4,
      },
    ],
  };

  public doughnutChartType: ChartType = 'doughnut';

  public barChartData: ChartData<'bar'> = {
    labels: [],

    datasets: [
      { label: 'Active', data: [], backgroundColor: '#10B981' },

      { label: 'Lapsed', data: [], backgroundColor: '#F43F5E' },
    ],
  };

  constructor(
    private readonly store: Store<{ auth: AuthState }>,

    private readonly api: ApiService,

    private readonly toastService: ToastService,
  ) {
    this.agentName$ = this.store.select((state) => state.auth.name);

    this.agentId$ = this.store.select((state) => state.auth.userId);
  }

  ngOnInit() {
    this.fetchDashboardData();
  }

  getPolicyStatus(policy: any): string {
    if (new Date(policy.endDate).getTime() < Date.now()) {
      return 'EXPIRED';
    }

    return policy.status;
  }

  updateAnnualProfit(policies: any[]): void {
    const currentYear = new Date().getFullYear();

    for (let policy of policies) {
      let monthlyPremium = policy.premiumAmount;

      const lastPayment = new Date(policy.lastPaymentDate);

      if (
        (lastPayment.getFullYear() === currentYear &&
          this.getPolicyStatus(policy) === 'LAPSED') ||
        this.getPolicyStatus(policy) === 'EXPIRED'
      ) {
        const lastPaymentMonth = lastPayment.getMonth();

        const monthsPaid = lastPaymentMonth + 1;

        const monthsRemaining = 12 - monthsPaid;

        this.annualProfit -= monthlyPremium * 0.05 * monthsRemaining;

        console.log(
          'Policy ID : ' +
            policy.policyId +
            '---' +
            'Months Remaining : ' +
            monthsRemaining,
        );

        console.log('Subtracting...' + monthlyPremium * 0.05 * monthsRemaining);
      }
    }
  }

  fetchDashboardData() {
    this.isLoading = true;

    this.api
      .getAgentDashboard()
      .pipe(delay(300))
      .subscribe({
        next: (dashboardDto: any) => this.handleDashboardSuccess(dashboardDto),

        error: (err) => {
          this.isLoading = false;

          console.error('Failed To Load Dashboard: ', err);
        },
      });
  }

  private handleDashboardSuccess(dashboardDto: any): void {
    this.totalCustomers = dashboardDto.totalCustomers;

    this.totalPoliciesCurrentYear = dashboardDto.totalPoliciesCurrentYear;

    this.annualProfit = dashboardDto.annualProfit * 12;

    const customers =
      dashboardDto.customers && Array.isArray(dashboardDto.customers)
        ? dashboardDto.customers
        : [];

    this.groupedPolicies = this.buildGroupedPolicies(customers);

    const extractedData = this.extractPoliciesAndCounts(customers);

    this.allCustomerPolicies = extractedData.flattenedPolicies;

    this.updateAnnualProfit(this.allCustomerPolicies);

    this.updateChart(
      extractedData.healthCount,
      extractedData.lifeCount,
      extractedData.homeCount,
      extractedData.vehicleCount,
    );

    this.updateBarChart();

    this.isLoading = false;
  }

  private buildGroupedPolicies(customers: any[]): {
    [key: string]: UICustomerGroup;
  } {
    return customers.reduce(
      (acc: { [key: string]: UICustomerGroup }, customer: any) => {
        acc[customer.name] = {
          customerName: customer.name,

          customerContact: customer.contact,

          policies:
            customer.policies && Array.isArray(customer.policies)
              ? customer.policies.map((policy: any) =>
                  this.mapToDashboardPolicy(policy),
                )
              : [],
        };

        return acc;
      },
      {},
    );
  }

  private mapToDashboardPolicy(policy: any): UIDashboardPolicy {
    return {
      policyId: policy.policyId,

      policyType: policy.policyType,

      premiumAmount: policy.premiumAmount,

      lastPaymentDate: policy.lastPremiumPaymentDate,

      endDate: policy.policyEndDate,

      status: policy.policyStatus,
    };
  }

  private extractPoliciesAndCounts(customers: any[]) {
    const flattenedPolicies: UIFormattedPolicy[] = [];

    let healthCount = 0,
      lifeCount = 0,
      homeCount = 0,
      vehicleCount = 0;

    for (const customer of customers) {
      if (!customer.policies || !Array.isArray(customer.policies)) {
        continue;
      }

      for (const policy of customer.policies) {
        flattenedPolicies.push({
          customerName: customer.name,

          customerContact: customer.contact,

          ...this.mapToDashboardPolicy(policy),
        });

        const typeStr = (policy.policyType || '').toUpperCase();

        if (typeStr.includes('HEALTH')) healthCount++;
        else if (typeStr.includes('LIFE')) lifeCount++;
        else if (typeStr.includes('HOME')) homeCount++;
        else vehicleCount++;
      }
    }

    return {
      flattenedPolicies,
      healthCount,
      lifeCount,
      homeCount,
      vehicleCount,
    };
  }

  private updateBarChart(): void {
    const customerStats = Object.keys(this.groupedPolicies).map((name) => {
      const policies = this.groupedPolicies[name].policies;

      return {
        name: name,

        totalCount: policies.length,

        activeCount: policies.filter((p) => p.status === 'ACTIVE').length,

        lapsedCount: policies.filter((p) => p.status === 'LAPSED').length,
      };
    });

    customerStats.sort((a, b) => b.totalCount - a.totalCount);

    const top5Customers = customerStats.slice(0, 5);

    this.barChartData = {
      labels: top5Customers.map((c) => c.name),

      datasets: [
        {
          label: 'Active',
          data: top5Customers.map((c) => c.activeCount),
          backgroundColor: '#10B981',
          borderRadius: 4,
        },

        {
          label: 'Lapsed',
          data: top5Customers.map((c) => c.lapsedCount),
          backgroundColor: '#F43F5E',
          borderRadius: 4,
        },
      ],
    };
  }

  // Custom sort by policy count for keyvalue pipe

  sortCustomersByPolicyCount = (
    a: KeyValue<string, UICustomerGroup>,
    b: KeyValue<string, UICustomerGroup>,
  ): number => {
    return b.value.policies.length - a.value.policies.length;
  };

  updateSearch(event: Event): void {
    this.searchTerm = (event.target as HTMLInputElement).value.toLowerCase();
  }

  matchesSearch(name: string, contact: string): boolean {
    if (!this.searchTerm) return true;

    const nameMatch = name.toLowerCase().includes(this.searchTerm);

    const contactMatch = contact ? contact.includes(this.searchTerm) : false;

    return nameMatch || contactMatch;
  }

  sendReminder(policyId: string, customerName: string): void {
    this.api.sendPaymentReminder(policyId).subscribe({
      next: () => {
        this.toastService.showSuccess(
          `Reminder Email Successfully Sent To ${customerName}`,
        );
      },

      error: () => {
        this.toastService.showError(
          'Failed To Send Reminder. Please Try Again!',
        );
      },
    });
  }

  private updateChart(
    health: number,
    life: number,
    home: number,
    vehicle: number,
  ) {
    this.doughnutChartData = {
      labels: this.doughnutChartLabels,

      datasets: [
        {
          data: [health, life, home, vehicle],

          backgroundColor: ['#10B981', '#3B82F6', '#F59E0B', '#8B5CF6'],

          borderWidth: 0,

          hoverOffset: 4,
        },
      ],
    };
  }

  exportToPDF(): void {
    if (!this.allCustomerPolicies || this.allCustomerPolicies.length === 0) {
      this.toastService.showError('No Data To Export!');

      return;
    }

    const doc = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4',
    });

    doc.setFontSize(22);

    doc.setFont('helvetica', 'bold');

    doc.setTextColor(15, 23, 42);

    doc.text('Step', 15, 22);

    doc.setTextColor(79, 70, 229);

    doc.text('Up', 32, 22);

    doc.setFontSize(9);

    doc.setFont('helvetica', 'bold');

    doc.setTextColor(100, 116, 139);

    doc.text('ENTERPRISE INSURANCE DISPATCH', 15, 27);

    doc.setFontSize(12);

    doc.setFont('helvetica', 'bold');

    doc.setTextColor(79, 70, 229);

    doc.text('AGENT PORTFOLIO REPORT', 195, 22, { align: 'right' });

    doc.setFontSize(9);

    doc.setFont('helvetica', 'normal');

    doc.setTextColor(100, 116, 139);

    doc.text(`Generated: ${new Date().toLocaleDateString()}`, 195, 27, {
      align: 'right',
    });

    const totalPolicies = this.allCustomerPolicies.length;

    const activeCount = this.allCustomerPolicies.filter(
      (p) => p.status === 'ACTIVE',
    ).length;

    const lapsedCount = this.allCustomerPolicies.filter(
      (p) => p.status === 'LAPSED',
    ).length;

    doc.setDrawColor(226, 232, 246);

    doc.setFillColor(255, 255, 255);

    doc.roundedRect(15, 35, 180, 22, 3, 3, 'FD');

    doc.setFontSize(9);

    doc.setFont('helvetica', 'bold');

    doc.text(`TOTAL ASSIGNED: ${totalPolicies}`, 35, 47, { align: 'center' });

    doc.setTextColor(6, 95, 70);

    doc.text(`ACTIVE POLICIES: ${activeCount}`, 105, 47, { align: 'center' });

    doc.setTextColor(159, 18, 57);

    doc.text(`LAPSED POLICIES: ${lapsedCount}`, 175, 47, { align: 'center' });

    const tableRows = this.allCustomerPolicies.map((policy) => [
      policy.customerName,

      policy.policyId,

      policy.policyType.replace('_INSURANCE', ''),

      `INR ${policy.premiumAmount}`,

      policy.lastPaymentDate || 'Pending',

      policy.status,
    ]);

    autoTable(doc, {
      startY: 65,

      head: [
        [
          'CUSTOMER NAME',
          'POLICY ID',
          'POLICY TYPE',
          'PREMIUM',
          'LAST PAYMENT',
          'STATUS',
        ],
      ],

      body: tableRows,

      theme: 'grid',

      styles: {
        font: 'helvetica',

        fontSize: 9,

        textColor: [51, 69, 85],

        lineColor: [226, 232, 240],

        lineWidth: 0.5,

        cellPadding: 4,
      },

      headStyles: {
        fillColor: [79, 70, 229],

        textColor: [255, 255, 255],

        fontStyle: 'bold',
      },

      alternateRowStyles: {
        fillColor: [248, 250, 252],
      },

      didParseCell: (data) => {
        if (data.section === 'body' && data.column.index === 5) {
          if (data.cell.raw === 'ACTIVE') {
            data.cell.styles.textColor = [16, 185, 129];

            data.cell.styles.fontStyle = 'bold';
          } else if (data.cell.raw === 'LAPSED') {
            data.cell.styles.textColor = [244, 63, 24];

            data.cell.styles.fontStyle = 'bold';
          }
        }
      },
    });

    doc.save('StepUp_Insurance_Agent_Report.pdf');
  }
}