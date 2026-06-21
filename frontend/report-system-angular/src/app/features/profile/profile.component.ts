import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputSwitchModule } from 'primeng/inputswitch';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    FormsModule,
    InputTextModule,
    ButtonModule,
    SelectModule,
    InputSwitchModule,
    CardModule,
    DividerModule,
    ToastModule,
  ],
  providers: [MessageService],
  template: `
    <p-toast />

    <div class="mb-4">
      <h2 class="text-xl font-bold m-0">My Profile</h2>
      <p class="text-gray-500 text-sm mt-1 m-0">
        Manage your account details and preferences.
      </p>
    </div>

    <div class="grid">
      <div class="col-12 lg:col-6">
        <p-card header="Account Information">
          <div class="flex flex-column gap-4">
            <div class="field">
              <label for="name" class="block font-semibold text-sm mb-1">Full Name</label>
              <input
                id="name"
                pInputText
                type="text"
                [(ngModel)]="profile.name"
                class="w-full"
              />
            </div>
            <div class="field">
              <label for="email" class="block font-semibold text-sm mb-1">Email</label>
              <input
                id="email"
                pInputText
                type="email"
                [(ngModel)]="profile.email"
                class="w-full"
              />
            </div>
            <div class="field">
              <label for="phone" class="block font-semibold text-sm mb-1">Phone</label>
              <input
                id="phone"
                pInputText
                type="tel"
                [(ngModel)]="profile.phone"
                class="w-full"
              />
            </div>
          </div>
        </p-card>
      </div>

      <div class="col-12 lg:col-6">
        <p-card header="Preferences">
          <div class="flex flex-column gap-4">
            <div class="field">
              <label for="language" class="block font-semibold text-sm mb-1">Language</label>
              <p-select
                id="language"
                [options]="languages"
                [(ngModel)]="profile.language"
                optionLabel="label"
                optionValue="value"
                styleClass="w-full"
              />
            </div>
            <div class="field">
              <label for="timezone" class="block font-semibold text-sm mb-1">Timezone</label>
              <p-select
                id="timezone"
                [options]="timezones"
                [(ngModel)]="profile.timezone"
                optionLabel="label"
                optionValue="value"
                styleClass="w-full"
              />
            </div>
            <p-divider />
            <div class="field">
              <label class="block font-semibold text-sm mb-1">Dark Mode</label>
              <div class="flex align-items-center gap-2">
                <p-inputSwitch [(ngModel)]="isDark" (onChange)="toggleDark()" />
                <span class="text-sm text-gray-500">
                  {{ isDark ? 'Dark' : 'Light' }} mode
                </span>
              </div>
            </div>
          </div>
        </p-card>
      </div>

      <div class="col-12">
        <div class="flex justify-content-end">
          <p-button
            label="Save Changes"
            icon="pi pi-check"
            severity="primary"
            (onClick)="save()"
          />
        </div>
      </div>
    </div>
  `,
})
export class ProfileComponent implements OnInit {
  isDark = false;

  profile = {
    name: 'Admin User',
    email: 'admin@demo.com',
    phone: '+855 12 345 678',
    language: 'en',
    timezone: 'Asia/Phnom_Penh',
  };

  readonly languages = [
    { label: 'English', value: 'en' },
    { label: 'ភាសាខ្មែរ (Khmer)', value: 'kh' },
  ];

  readonly timezones = [
    { label: 'Asia/Phnom_Penh (UTC+7)', value: 'Asia/Phnom_Penh' },
    { label: 'Asia/Bangkok (UTC+7)', value: 'Asia/Bangkok' },
    { label: 'Asia/Ho_Chi_Minh (UTC+7)', value: 'Asia/Ho_Chi_Minh' },
    { label: 'Asia/Singapore (UTC+8)', value: 'Asia/Singapore' },
    { label: 'America/New_York (UTC-5)', value: 'America/New_York' },
    { label: 'Europe/London (UTC+0)', value: 'Europe/London' },
  ];

  constructor(private message: MessageService) {}

  ngOnInit() {
    this.isDark =
      typeof document !== 'undefined' &&
      document.documentElement.classList.contains('dark');
  }

  toggleDark() {
    document.documentElement.classList.toggle('dark', this.isDark);
  }

  save() {
    this.message.add({
      severity: 'success',
      summary: 'Saved',
      detail: 'Profile updated successfully.',
      life: 3000,
    });
  }
}
