#!/usr/bin/env python3
"""Fix Java files split from grouped files.
Handles: strip trailing next-type content, add missing annotations."""
import os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# Entity annotations per service
ENTITY_ANNOTATIONS = {
    # inventory-service entities
    "ProductCategoryEntity": "@Entity\n@Table(name = \"product_categories\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "ProductEntity": "@Entity\n@Table(name = \"products\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "StockEntryEntity": "@Entity\n@Table(name = \"stock_entries\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "StockExitEntity": "@Entity\n@Table(name = \"stock_exits\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "SupplierEntity": "@Entity\n@Table(name = \"suppliers\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "WarehouseEntity": "@Entity\n@Table(name = \"warehouses\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "PurchaseOrderEntity": "@Entity\n@Table(name = \"purchase_orders\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "PurchaseOrderItemEntity": "@Entity\n@Table(name = \"purchase_order_items\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    # finance entities
    "AccountEntity": "@Entity\n@Table(name = \"accounts\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "JournalEntryEntity": "@Entity\n@Table(name = \"journal_entries\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "JournalEntryLineEntity": "@Entity\n@Table(name = \"journal_entry_lines\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "InvoiceEntity": "@Entity\n@Table(name = \"invoices\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "InvoiceItemEntity": "@Entity\n@Table(name = \"invoice_items\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "TaxRecordEntity": "@Entity\n@Table(name = \"tax_records\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "TaxFilingReportEntity": "@Entity\n@Table(name = \"tax_filing_reports\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "EmployeeEntity": "@Entity\n@Table(name = \"employees\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "AttendanceRecordEntity": "@Entity\n@Table(name = \"attendance_records\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "PayrollPeriodEntity": "@Entity\n@Table(name = \"payroll_periods\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "PayrollItemEntity": "@Entity\n@Table(name = \"payroll_items\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    # payment entities
    "TransactionEntity": "@Entity\n@Table(name = \"transactions\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "GatewayLogEntity": "@Entity\n@Table(name = \"gateway_logs\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "RefundEntity": "@Entity\n@Table(name = \"refunds\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "ReconciliationRecordEntity": "@Entity\n@Table(name = \"reconciliation_records\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    # reporting entities
    "ReportDefinitionEntity": "@Entity\n@Table(name = \"report_definitions\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "ScheduledReportEntity": "@Entity\n@Table(name = \"scheduled_reports\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "ReportExecutionEntity": "@Entity\n@Table(name = \"report_executions\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "DashboardConfigEntity": "@Entity\n@Table(name = \"dashboard_configs\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
    "AggregatedSnapshotEntity": "@Entity\n@Table(name = \"aggregated_snapshots\")\n@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor\n",
}

CONTROLLER_ANNOTATIONS = {
    # inventory controllers
    "ProductController": "@RestController\n@RequestMapping(\"/products\")\n",
    "StockController": "@RestController\n@RequestMapping(\"/stock\")\n",
    "SupplierController": "@RestController\n@RequestMapping(\"/suppliers\")\n",
    "PurchaseOrderController": "@RestController\n@RequestMapping(\"/purchase-orders\")\n",
    # finance controllers
    "AccountController": "@RestController\n@RequestMapping(\"/accounts\")\n",
    "JournalEntryController": "@RestController\n@RequestMapping(\"/journal-entries\")\n",
    "InvoiceController": "@RestController\n@RequestMapping(\"/invoices\")\n",
    "TaxController": "@RestController\n@RequestMapping(\"/tax\")\n",
    "EmployeeController": "@RestController\n@RequestMapping(\"/employees\")\n",
    "PayrollController": "@RestController\n@RequestMapping(\"/payroll\")\n",
    # reporting controllers
    "ReportController": "@RestController\n@RequestMapping(\"/reports\")\n",
    "DashboardController": "@RestController\n@RequestMapping(\"/dashboards\")\n",
}

SERVICE_ANNOTATIONS = {
    # inventory services
    "ProductServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "StockServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "SupplierServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "PurchaseOrderServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    # finance services
    "AccountingServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "InvoiceServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "TaxServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    "PayrollServiceImpl": "@Service\n@RequiredArgsConstructor\n",
    # payment services
    "PaymentServiceImpl": "@Service\n",
    "PaymentGatewayRouter": "@Component\n",
    "ReconciliationServiceImpl": "@Service\n",
    # reporting services
    "ReportServiceImpl": "@Service\n",
    "DashboardServiceImpl": "@Service\n",
}

ADAPTER_ANNOTATIONS = {
    # inventory adapters
    "JpaSupplierAdapter": "@Component\n",
    "JpaWarehouseAdapter": "@Component\n",
    "JpaProductCategoryAdapter": "@Component\n",
    "JpaProductAdapter": "@Component\n",
    "JpaStockEntryAdapter": "@Component\n",
    "JpaStockExitAdapter": "@Component\n",
    "JpaPurchaseOrderAdapter": "@Component\n",
    "JpaPurchaseOrderItemAdapter": "@Component\n",
    # finance adapters
    "JpaAccountAdapter": "@Component\n",
    "JpaJournalEntryAdapter": "@Component\n",
    "JpaJournalEntryLineAdapter": "@Component\n",
    "JpaInvoiceAdapter": "@Component\n",
    "JpaInvoiceItemAdapter": "@Component\n",
    "JpaTaxRecordAdapter": "@Component\n",
    "JpaTaxFilingReportAdapter": "@Component\n",
    "JpaEmployeeAdapter": "@Component\n",
    "JpaAttendanceRecordAdapter": "@Component\n",
    "JpaPayrollPeriodAdapter": "@Component\n",
    "JpaPayrollItemAdapter": "@Component\n",
    # payment adapters
    "JpaTransactionAdapter": "@Component\n",
    "JpaGatewayLogAdapter": "@Component\n",
    "JpaRefundAdapter": "@Component\n",
    "JpaReconciliationRecordAdapter": "@Component\n",
    # reporting adapters
    "JpaReportDefinitionAdapter": "@Component\n",
    "JpaReportExecutionAdapter": "@Component\n",
    "JpaDashboardConfigAdapter": "@Component\n",
    # gateway adapters
    "AbaPayWayAdapter": "",
    "WingAdapter": "",
    "PiPayAdapter": "",
    "CashAdapter": "",
}

ALL_ANNOTATIONS = {}
ALL_ANNOTATIONS.update(ENTITY_ANNOTATIONS)
ALL_ANNOTATIONS.update(CONTROLLER_ANNOTATIONS)
ALL_ANNOTATIONS.update(SERVICE_ANNOTATIONS)
ALL_ANNOTATIONS.update(ADAPTER_ANNOTATIONS)

def fix_file(path):
    with open(path) as f:
        content = f.read()
    
    filename = os.path.basename(path)
    class_name = filename.replace(".java", "")
    
    # Find the class/interface/enum/record name from content
    type_match = re.search(r'public\s+(?:abstract\s+)?(?:class|interface|enum|record)\s+(\w+)', content)
    if not type_match:
        return False
    
    decl_name = type_match.group(1)
    
    # 1. Strip trailing content after the last top-level closing brace
    # Count braces to find the last brace at depth 0
    depth = 0
    last_toplevel_brace = -1
    for i, ch in enumerate(content):
        if ch == '{':
            depth += 1
        elif ch == '}':
            depth -= 1
            if depth == 0:
                last_toplevel_brace = i
    
    if last_toplevel_brace < 0:
        return False
    
    # Check if there's content after the last top-level brace
    after_brace = content[last_toplevel_brace + 1:]
    has_trailing = after_brace.strip() != ""
    
    # Strip trailing content
    body = content[:last_toplevel_brace + 1]
    
    # 2. Check if annotations exist before type declaration
    has_annotations = False
    before_type = body[:type_match.start()]
    if '@' in before_type:
        has_annotations = True
    
    # 3. Add missing annotations
    annotations = ALL_ANNOTATIONS.get(decl_name, "")
    
    if has_trailing or (annotations and not has_annotations):
        # Reconstruct: package + imports + annotations + type
        pkg_match = re.search(r'^package\s+[\w.]+;', body, re.MULTILINE)
        if pkg_match:
            pkg_end = pkg_match.end()
            imports_end = pkg_end
            for m in re.finditer(r'^import\s+.*?;\s*$', body[imports_end:], re.MULTILINE):
                imports_end = pkg_end + m.end()
            
            header = body[:imports_end]
            type_decl_and_body = body[imports_end:].lstrip('\n')
            
            # Remove leading newlines/whitespace from type section
            type_start = 0
            for i, ch in enumerate(type_decl_and_body):
                if not ch.isspace():
                    type_start = i
                    break
            
            rest = type_decl_and_body[type_start:]
            
            # If the type already has annotations, don't duplicate
            if rest.startswith('@') if annotations else False:
                new_content = header + "\n\n" + annotations + rest
            else:
                new_content = header + "\n\n" + annotations + rest
            
            with open(path, 'w') as f:
                f.write(new_content)
            return True
    
    return False

def main():
    services_dir = os.path.join(BASE, "services")
    fixed = 0
    checked = 0
    for root, dirs, files in os.walk(services_dir):
        for fname in files:
            if not fname.endswith(".java"):
                continue
            # Check if this is a file that could have been split
            # (not from the original auth or property service which were fine)
            if not any(s in root for s in ["inventory", "finance", "payment", "reporting", "restaurant"]):
                continue
            # Skip Restaurant files that were correctly split (only JpaRepositories was split)
            if "restaurant" in root and "JpaRepositories" not in fname and "Jpa" not in fname:
                continue
            path = os.path.join(root, fname)
            checked += 1
            if fix_file(path):
                fixed += 1
                print(f"  Fixed: {os.path.relpath(path, BASE)}")
    
    print(f"Checked {checked} files, fixed {fixed}")

if __name__ == '__main__':
    main()
