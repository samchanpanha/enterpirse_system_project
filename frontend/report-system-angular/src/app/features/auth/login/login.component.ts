import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../../core/services/auth.service';
import { BranchService } from '../../../core/services/branch.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    CardModule,
    MessageModule,
  ],
  template: `
    <div
      class="flex align-items-center justify-content-center min-h-screen bg-gray-50"
    >
      <p-card styleClass="w-full max-w-24rem shadow-3">
        <ng-template pTemplate="title">
          <div class="text-center mb-3">
            <h2 class="text-2xl font-bold text-gray-800 m-0">Report System</h2>
            <p class="text-sm text-gray-500 mt-1">Sign in to your account</p>
          </div>
        </ng-template>
        <ng-template pTemplate="content">
          <form #loginForm="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
            <div class="flex flex-column gap-1">
              <label for="email" class="text-sm font-medium">Email</label>
              <input
                id="email"
                pInputText
                type="email"
                [(ngModel)]="email"
                name="email"
                required
                [class.ng-invalid]="submitted && !email"
                placeholder="admin@demo.com"
              />
            </div>
            <div class="flex flex-column gap-1">
              <label for="password" class="text-sm font-medium">Password</label>
              <p-password
                id="password"
                [(ngModel)]="password"
                name="password"
                [feedback]="false"
                [toggleMask]="true"
                required
                [class.ng-invalid]="submitted && !password"
                placeholder="Password"
                styleClass="w-full"
                inputStyleClass="w-full"
              />
            </div>
            @if (error) {
              <p-message severity="error" [text]="error" />
            }
            <p-button
              type="submit"
              label="Sign In"
              styleClass="w-full"
              [loading]="loading"
            />
          </form>
        </ng-template>
      </p-card>
    </div>
  `,
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';
  submitted = false;

  constructor(
    private auth: AuthService,
    private branch: BranchService,
    private router: Router,
  ) {}

  onSubmit() {
    this.submitted = true;
    this.error = '';
    if (!this.email || !this.password) return;

    this.loading = true;
    this.auth
      .login({
        email: this.email,
        password: this.password,
        tenantId: this.branch.tenantId,
      })
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.error =
            err.error?.message || 'Invalid email or password. Please try again.';
          this.loading = false;
        },
      });
  }
}
