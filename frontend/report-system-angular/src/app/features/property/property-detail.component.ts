import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { PropertyApiService } from './services/property-api.service';
import { Property } from './models/property.model';
import { UnitListComponent } from './unit-list.component';
import { LeaseListComponent } from './lease-list.component';

@Component({
  selector: 'app-property-detail',
  standalone: true,
  imports: [
    DatePipe,
    TabViewModule,
    CardModule,
    TagModule,
    ButtonModule,
    UnitListComponent,
    LeaseListComponent,
  ],
  template: `
    <div>
      <div class="flex align-items-center gap-2 mb-3">
        <button
          class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="router.navigate(['/properties'])"
        >
          <i class="pi pi-arrow-left text-xl"></i>
        </button>
        @if (property) {
          <div class="flex-1">
            <h2 class="text-xl font-bold m-0">{{ property.name }}</h2>
            <p class="text-sm text-gray-500 m-0 mt-1">
              {{ property.type }} &middot; {{ property.city }}
              @if (property.district) {
                &middot; {{ property.district }}
              }
            </p>
          </div>
          <p-button
            label="Edit"
            icon="pi pi-pencil"
            severity="info"
            (onClick)="router.navigate(['properties', property.id, 'edit'])"
          />
        }
      </div>

      @if (property) {
        <div class="grid mb-3">
          <div class="col-12 md:col-3">
            <p-card styleClass="shadow-1">
              <ng-template pTemplate="content">
                <p class="text-2xl font-bold m-0 text-indigo-600">{{ property.totalUnits }}</p>
                <p class="text-sm text-gray-500 m-0">Total Units</p>
              </ng-template>
            </p-card>
          </div>
          <div class="col-12 md:col-3">
            <p-card styleClass="shadow-1">
              <ng-template pTemplate="content">
                <p class="text-2xl font-bold m-0 text-green-600">{{ property.type }}</p>
                <p class="text-sm text-gray-500 m-0">Type</p>
              </ng-template>
            </p-card>
          </div>
          <div class="col-12 md:col-3">
            <p-card styleClass="shadow-1">
              <ng-template pTemplate="content">
                <p-tag
                  [value]="property.status"
                  [severity]="property.status === 'ACTIVE' ? 'success' : 'danger'"
                />
                <p class="text-sm text-gray-500 m-0 mt-2">Status</p>
              </ng-template>
            </p-card>
          </div>
          <div class="col-12 md:col-3">
            <p-card styleClass="shadow-1">
              <ng-template pTemplate="content">
                <p class="text-sm font-medium m-0">{{ property.ownerName || '—' }}</p>
                <p class="text-sm text-gray-500 m-0">Owner</p>
              </ng-template>
            </p-card>
          </div>
        </div>

        <p-tabView>
          <p-tabPanel header="Details">
            <div class="grid">
              <div class="col-12 md:col-6">
                <div class="flex flex-column gap-2">
                  <div><span class="font-medium">Address:</span> {{ property.address || '—' }}</div>
                  <div><span class="font-medium">City:</span> {{ property.city }}</div>
                  <div><span class="font-medium">District:</span> {{ property.district || '—' }}</div>
                  <div><span class="font-medium">Owner:</span> {{ property.ownerName || '—' }}</div>
                </div>
              </div>
              <div class="col-12 md:col-6">
                <div class="flex flex-column gap-2">
                  <div><span class="font-medium">Owner Phone:</span> {{ property.ownerPhone || '—' }}</div>
                  <div><span class="font-medium">Created:</span> {{ property.createdAt | date:'medium' }}</div>
                  <div><span class="font-medium">Updated:</span> {{ property.updatedAt | date:'medium' }}</div>
                </div>
              </div>
              @if (property.notes) {
                <div class="col-12">
                  <div><span class="font-medium">Notes:</span></div>
                  <p class="text-gray-600 mt-1">{{ property.notes }}</p>
                </div>
              }
            </div>
          </p-tabPanel>
          <p-tabPanel header="Units">
            <app-unit-list [propertyId]="propertyId" />
          </p-tabPanel>
          <p-tabPanel header="Leases">
            <app-lease-list [propertyId]="propertyId" />
          </p-tabPanel>
        </p-tabView>
      } @else {
        <div class="text-center text-gray-500 py-6">Loading...</div>
      }
    </div>
  `,
})
export class PropertyDetailComponent implements OnInit {
  property?: Property;
  propertyId = '';

  constructor(
    protected router: Router,
    private route: ActivatedRoute,
    private api: PropertyApiService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.propertyId = id;
      this.api.getProperty(id).subscribe({
        next: (p) => (this.property = p),
      });
    }
  }
}
