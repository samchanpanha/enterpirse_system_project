import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DividerModule } from 'primeng/divider';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../core/services/auth.service';
import { BranchService } from '../../../core/services/branch.service';

type Locale = 'en' | 'kh';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    CheckboxModule,
    DividerModule,
    SelectModule,
    ToastModule,
  ],
  providers: [MessageService],
  template: `
    <div class="login-root" [class.dark]="isDark">
      <div class="login-container">
        <!-- Left: Hero Branding -->
        <div class="login-hero">
          <div class="hero-bg"></div>
          <div class="hero-content">
            <div class="hero-badge">{{ t('enterprise_suite') }}</div>
            <h1 class="hero-title">{{ t('hero_title') }}</h1>
            <p class="hero-subtitle">{{ t('hero_subtitle') }}</p>
            <div class="hero-features">
              @for (f of features; track f.key) {
                <div class="hero-feature">
                  <i [class]="f.icon" class="feature-icon"></i>
                  <span>{{ t(f.key) }}</span>
                </div>
              }
            </div>
            <div class="hero-footer">
              <span class="hero-version">v2.0.0</span>
              <span class="hero-dot">•</span>
              <span>{{ t('multi_tenant') }}</span>
            </div>
          </div>
        </div>

        <!-- Right: Login Card -->
        <div class="login-card">
          <!-- Top bar: locale + theme -->
          <div class="login-topbar">
            <button class="top-btn" (click)="toggleDark()" [title]="t('toggle_theme')">
              <i [class]="isDark ? 'pi pi-sun' : 'pi pi-moon'"></i>
            </button>
            <p-select
              [options]="locales"
              [(ngModel)]="currentLocale"
              (onChange)="setLocale($event.value)"
              optionLabel="label"
              optionValue="value"
              styleClass="locale-select"
            />
          </div>

          <!-- Form -->
          <div class="login-form-area">
            <div class="login-brand">
              <div class="brand-icon">
                <i class="pi pi-chart-bar"></i>
              </div>
              <h2 class="brand-name">Report System</h2>
              <p class="brand-desc">{{ t('sign_in_prompt') }}</p>
            </div>

            <form (ngSubmit)="onSubmit()" class="login-form">
              <div class="field">
                <label class="field-label" for="email">{{ t('email_label') }}</label>
                <input
                  id="email"
                  pInputText
                  type="email"
                  [(ngModel)]="email"
                  name="email"
                  [placeholder]="t('email_placeholder')"
                  [class.ng-invalid]="submitted && !email"
                  class="field-input"
                  autocomplete="email"
                />
              </div>

              <div class="field">
                <div class="field-label-row">
                  <label class="field-label" for="password">{{ t('password_label') }}</label>
                  <a class="forgot-link" (click)="forgotPassword()">{{ t('forgot_password') }}</a>
                </div>
                <p-password
                  id="password"
                  [(ngModel)]="password"
                  name="password"
                  [feedback]="false"
                  [toggleMask]="true"
                  [placeholder]="t('password_placeholder')"
                  [class.ng-invalid]="submitted && !password"
                  styleClass="w-full"
                  inputStyleClass="w-full field-input"
                  autocomplete="current-password"
                />
              </div>

              <div class="field-row">
                <p-checkbox
                  [(ngModel)]="rememberMe"
                  name="rememberMe"
                  [binary]="true"
                  inputId="remember"
                />
                <label for="remember" class="remember-label">{{ t('remember_me') }}</label>
              </div>

              @if (error) {
                <div class="error-box">
                  <i class="pi pi-exclamation-circle"></i>
                  <span>{{ error }}</span>
                </div>
              }

              <p-button
                type="submit"
                [label]="t('sign_in')"
                styleClass="w-full submit-btn"
                [loading]="loading"
              />

              <p-divider align="center">
                <span class="divider-text">{{ t('or_continue') }}</span>
              </p-divider>

              <button type="button" class="sso-btn" (click)="ssoLogin()">
                <i class="pi pi-shield"></i>
                <span>{{ t('sso_login') }}</span>
              </button>
            </form>

            <div class="login-footer">
              <div class="demo-hint">
                <i class="pi pi-info-circle"></i>
                <span>{{ t('demo_hint') }}</span>
              </div>
              <div class="trust-footer">
                <i class="pi pi-lock"></i>
                <span>{{ t('encrypted') }}</span>
              </div>
              <div class="copyright">
                &copy; 2026 Report System. {{ t('rights') }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-root {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8fafc;
      font-family: 'Inter', sans-serif;
    }
    .login-root.dark {
      background: #0f172a;
    }
    .login-container {
      display: flex;
      width: 100%;
      max-width: 1100px;
      min-height: 600px;
      border-radius: 20px;
      overflow: hidden;
      box-shadow: 0 20px 60px rgba(0,0,0,0.08), 0 4px 16px rgba(0,0,0,0.04);
      margin: 20px;
    }
    .dark .login-container {
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }

    /* ===== LEFT HERO ===== */
    .login-hero {
      flex: 1;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 48px;
      overflow: hidden;
    }
    .hero-bg {
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, #6366f1 0%, #4f46e5 40%, #7c3aed 100%);
    }
    .dark .hero-bg {
      background: linear-gradient(135deg, #4338ca 0%, #3730a3 40%, #5b21b6 100%);
    }
    .hero-bg::before {
      content: '';
      position: absolute;
      inset: 0;
      background:
        radial-gradient(circle at 20% 80%, rgba(255,255,255,0.12) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
    }
    .hero-bg::after {
      content: '';
      position: absolute;
      inset: -50%;
      background-image:
        radial-gradient(2px 2px at 20px 30px, rgba(255,255,255,0.15) 0%, transparent 0%),
        radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.1) 0%, transparent 0%),
        radial-gradient(2px 2px at 50px 160px, rgba(255,255,255,0.12) 0%, transparent 0%),
        radial-gradient(2px 2px at 90px 40px, rgba(255,255,255,0.08) 0%, transparent 0%),
        radial-gradient(2px 2px at 130px 80px, rgba(255,255,255,0.14) 0%, transparent 0%),
        radial-gradient(2px 2px at 160px 30px, rgba(255,255,255,0.1) 0%, transparent 0%);
      background-size: 200px 200px;
      animation: drift 20s linear infinite;
    }
    @keyframes drift {
      from { transform: translateY(0); }
      to { transform: translateY(-100px); }
    }
    .hero-content {
      position: relative;
      z-index: 1;
      color: white;
      max-width: 400px;
    }
    .hero-badge {
      display: inline-block;
      padding: 6px 14px;
      background: rgba(255,255,255,0.15);
      backdrop-filter: blur(4px);
      border-radius: 20px;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      margin-bottom: 24px;
    }
    .hero-title {
      font-size: 2.25rem;
      font-weight: 700;
      line-height: 1.2;
      margin: 0 0 12px;
    }
    .hero-subtitle {
      font-size: 1rem;
      line-height: 1.6;
      opacity: 0.85;
      margin: 0 0 32px;
    }
    .hero-features {
      display: flex;
      flex-direction: column;
      gap: 14px;
      margin-bottom: 40px;
    }
    .hero-feature {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 0.9rem;
    }
    .feature-icon {
      width: 20px;
      text-align: center;
      font-size: 0.9rem;
      opacity: 0.8;
    }
    .hero-footer {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.8rem;
      opacity: 0.6;
    }
    .hero-dot {
      opacity: 0.4;
    }

    /* ===== RIGHT LOGIN CARD ===== */
    .login-card {
      flex: 1;
      background: #ffffff;
      display: flex;
      flex-direction: column;
      padding: 40px;
      max-width: 480px;
    }
    .dark .login-card {
      background: #1e293b;
    }
    .login-topbar {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 12px;
      margin-bottom: 32px;
    }
    .top-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
      color: #64748b;
      cursor: pointer;
      transition: all 0.15s ease;
    }
    .dark .top-btn {
      border-color: #334155;
      background: #0f172a;
      color: #94a3b8;
    }
    .top-btn:hover {
      background: #e2e8f0;
      color: #334155;
    }
    .dark .top-btn:hover {
      background: #334155;
      color: #e2e8f0;
    }
    :deep(.locale-select) {
      width: 80px;
    }
    :deep(.locale-select .p-select-label) {
      font-size: 0.825rem;
    }
    .login-form-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
    .login-brand {
      text-align: center;
      margin-bottom: 32px;
    }
    .brand-icon {
      width: 48px;
      height: 48px;
      margin: 0 auto 12px;
      background: linear-gradient(135deg, #6366f1, #4f46e5);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1.5rem;
    }
    .brand-name {
      font-size: 1.5rem;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 6px;
    }
    .dark .brand-name {
      color: #f1f5f9;
    }
    .brand-desc {
      font-size: 0.875rem;
      color: #94a3b8;
      margin: 0;
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .field {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .field-label {
      font-size: 0.8125rem;
      font-weight: 600;
      color: #334155;
    }
    .dark .field-label {
      color: #cbd5e1;
    }
    .field-label-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .field-input {
      width: 100%;
    }
    .forgot-link {
      font-size: 0.8rem;
      color: #6366f1;
      cursor: pointer;
      text-decoration: none;
      font-weight: 500;
    }
    .forgot-link:hover {
      text-decoration: underline;
    }
    .field-row {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .remember-label {
      font-size: 0.825rem;
      color: #64748b;
      cursor: pointer;
    }
    .dark .remember-label {
      color: #94a3b8;
    }
    .error-box {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 14px;
      background: #fef2f2;
      border-radius: 10px;
      font-size: 0.825rem;
      color: #dc2626;
      border: 1px solid #fecaca;
    }
    .dark .error-box {
      background: #450a0a;
      border-color: #7f1d1d;
      color: #fca5a5;
    }
    .submit-btn {
      margin-top: 4px;
    }
    :deep(.submit-btn .p-button) {
      height: 44px;
      font-size: 0.9rem;
      font-weight: 600;
      border-radius: 10px;
    }
    .divider-text {
      font-size: 0.8rem;
      color: #94a3b8;
      padding: 0 8px;
    }
    .sso-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      width: 100%;
      height: 44px;
      border-radius: 10px;
      border: 1px solid #e2e8f0;
      background: transparent;
      color: #475569;
      font-size: 0.875rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.15s ease;
      font-family: inherit;
    }
    .dark .sso-btn {
      border-color: #334155;
      color: #94a3b8;
    }
    .sso-btn:hover {
      background: #f8fafc;
      border-color: #cbd5e1;
    }
    .dark .sso-btn:hover {
      background: #0f172a;
      border-color: #475569;
    }

    .login-footer {
      margin-top: 32px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
    }
    .demo-hint {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .trust-footer {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 0.725rem;
      color: #94a3b8;
    }
    .copyright {
      font-size: 0.7rem;
      color: #cbd5e1;
    }
    .dark .copyright {
      color: #475569;
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 800px) {
      .login-container {
        flex-direction: column;
        max-width: 460px;
        margin: 16px;
      }
      .login-hero {
        padding: 32px 24px;
        min-height: 240px;
      }
      .hero-title {
        font-size: 1.5rem;
      }
      .hero-features {
        display: none;
      }
      .login-card {
        max-width: 100%;
        padding: 32px 24px;
      }
    }
  `],
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';
  submitted = false;
  rememberMe = false;
  isDark = false;
  currentLocale: Locale = 'en';

  readonly locales = [
    { label: '🇬🇧 EN', value: 'en' },
    { label: '🇰🇭 KH', value: 'kh' },
  ];

  readonly features = [
    { icon: 'pi pi-building', key: 'f_property' },
    { icon: 'pi pi-shop', key: 'f_pos' },
    { icon: 'pi pi-box', key: 'f_inventory' },
    { icon: 'pi pi-wallet', key: 'f_finance' },
    { icon: 'pi pi-truck', key: 'f_delivery' },
    { icon: 'pi pi-chart-line', key: 'f_reports' },
  ];

  private readonly translations: Record<Locale, Record<string, string>> = {
    en: {
      enterprise_suite: 'Enterprise SaaS Suite',
      hero_title: 'All-in-One Business OS',
      hero_subtitle: 'POS, Real Estate, Delivery, Property, Inventory, Finance & Reporting — one platform for your entire enterprise.',
      f_property: 'Property & Housing Management',
      f_pos: 'POS & Restaurant Management',
      f_inventory: 'Inventory & Supply Chain',
      f_finance: 'Finance, Payroll & Accounting',
      f_delivery: 'Delivery & Logistics',
      f_reports: 'Reports & Analytics',
      multi_tenant: 'Multi-tenant • White-label',
      toggle_theme: 'Toggle theme',
      sign_in_prompt: 'Sign in to your enterprise account',
      email_label: 'Email',
      email_placeholder: 'admin@demo.com',
      password_label: 'Password',
      password_placeholder: 'Enter your password',
      forgot_password: 'Forgot password?',
      remember_me: 'Remember me',
      sign_in: 'Sign In',
      or_continue: 'Or continue with',
      sso_login: 'Sign in with Keycloak SSO',
      demo_hint: 'Demo: admin@demo.com / Demo123!',
      encrypted: 'Secured with 256-bit encryption',
      rights: 'All rights reserved.',
    },
    kh: {
      enterprise_suite: 'ឈុតកម្មវិធីសហគ្រាស',
      hero_title: 'ប្រព័ន្ធគ្រប់គ្រងអាជីវកម្មគ្រប់លក្ខណៈ',
      hero_subtitle: 'POS, អចលនទ្រព្យ, ដឹកជញ្ជូន, ស្តុក, ហិរញ្ញវត្ថុ និងរបាយការណ៍ — វេទិកាតែមួយសម្រាប់សហគ្រាសរបស់អ្នក។',
      f_property: 'គ្រប់គ្រងអចលនទ្រព្យ និងលំនៅដ្ឋាន',
      f_pos: 'ប្រព័ន្ធ POS និងភោជនីយដ្ឋាន',
      f_inventory: 'គ្រប់គ្រងស្តុក និងសង្វាក់ផ្គត់ផ្គង់',
      f_finance: 'ហិរញ្ញវត្ថុ ប្រាក់ខែ និងគណនេយ្យ',
      f_delivery: 'ដឹកជញ្ជូន និងភស្តុភារ',
      f_reports: 'របាយការណ៍ និងវិភាគទិន្នន័យ',
      multi_tenant: 'ពហុអតិថិជន • ស្លាកសញ្ញាផ្ទាល់ខ្លួន',
      toggle_theme: 'ប្តូរពណ៌',
      sign_in_prompt: 'ចូលគណនីសហគ្រាសរបស់អ្នក',
      email_label: 'អ៊ីមែល',
      email_placeholder: 'admin@demo.com',
      password_label: 'ពាក្យសម្ងាត់',
      password_placeholder: 'បញ្ចូលពាក្យសម្ងាត់',
      forgot_password: 'ភ្លេចពាក្យសម្ងាត់?',
      remember_me: 'ចងចាំខ្ញុំ',
      sign_in: 'ចូលប្រព័ន្ធ',
      or_continue: 'ឬបន្តជាមួយ',
      sso_login: 'ចូលតាមរយៈ Keycloak SSO',
      demo_hint: 'សាកល្បង: admin@demo.com / Demo123!',
      encrypted: 'ការពារដោយការអ៊ិនគ្រីប 256-bit',
      rights: 'រក្សាសិទ្ធគ្រប់យ៉ាង។',
    },
  };

  constructor(
    private auth: AuthService,
    private branch: BranchService,
    private router: Router,
    private toast: MessageService,
  ) {
    this.isDark = typeof document !== 'undefined' && document.documentElement.classList.contains('dark');
  }

  t(key: string): string {
    return this.translations[this.currentLocale]?.[key] ?? this.translations['en'][key] ?? key;
  }

  setLocale(locale: Locale) {
    this.currentLocale = locale;
    this.toast.add({
      severity: 'info',
      summary: locale === 'kh' ? 'ភាសាខ្មែរ' : 'English',
      detail: locale === 'kh'
        ? 'បានប្តូរទៅជាភាសាខ្មែរ'
        : 'Switched to English',
      life: 2000,
    });
  }

  toggleDark() {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';
    if (!this.email || !this.password) return;

    this.loading = true;
    this.auth.login({
      email: this.email,
      password: this.password,
      tenantId: this.branch.tenantId,
    }).subscribe({
      next: () => {
        this.toast.add({
          severity: 'success',
          summary: this.t('sign_in'),
          detail: this.currentLocale === 'kh' ? 'សូមស្វាគមន៍មកកាន់ប្រព័ន្ធ' : 'Welcome back!',
          life: 3000,
        });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err.error?.message
          || (this.currentLocale === 'kh'
            ? 'អ៊ីមែល ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវ'
            : 'Invalid email or password. Please try again.');
        this.loading = false;
      },
    });
  }

  ssoLogin() {
    const keycloakToken = localStorage.getItem('kc_token');
    if (keycloakToken) {
      this.loading = true;
      this.auth.ssoLogin(keycloakToken).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: () => {
          window.location.href = 'http://localhost:8180/realms/demo-corp/protocol/openid-connect/auth?client_id=report-system-web&redirect_uri=http://localhost:4200/auth/login&response_mode=query&response_type=code&scope=openid';
        },
      });
    } else {
      window.location.href = 'http://localhost:8180/realms/demo-corp/protocol/openid-connect/auth?client_id=report-system-web&redirect_uri=http://localhost:4200/auth/login&response_mode=query&response_type=code&scope=openid';
    }
  }

  forgotPassword() {
    this.toast.add({
      severity: 'info',
      summary: this.t('forgot_password'),
      detail: this.currentLocale === 'kh'
        ? 'សូមទាក់ទងអ្នកគ្រប់គ្រងប្រព័ន្ធ ឬទំនាក់ទំនងមកយើងខ្ញុំ'
        : 'Please contact your system administrator or reach out to our support team.',
      life: 5000,
    });
  }
}
