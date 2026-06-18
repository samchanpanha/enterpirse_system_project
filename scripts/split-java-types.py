#!/usr/bin/env python3
"""Split Java files with multiple public types into individual files.
Correctly handles annotations before each type declaration."""
import os, re

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

files = [
    "services/restaurant-service/src/main/java/com/reportsystem/restaurant/infrastructure/persistence/repository/JpaRepositories.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/domain/port/outbound/InventoryRepositories.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/domain/port/inbound/InventoryService.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/domain/service/InventoryServices.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/infrastructure/persistence/entity/InventoryEntities.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/infrastructure/persistence/repository/JpaRepositories.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/infrastructure/persistence/adapter/JpaAdapters.java",
    "services/inventory-service/src/main/java/com/reportsystem/inventory/infrastructure/web/InventoryControllers.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/domain/port/outbound/FinanceRepositories.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/domain/port/inbound/FinanceServices.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/domain/service/FinanceServices.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/domain/model/FinanceModels.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/infrastructure/persistence/entity/FinanceEntities.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/infrastructure/persistence/repository/JpaRepositories.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/infrastructure/persistence/adapter/JpaAdapters.java",
    "services/finance-service/src/main/java/com/reportsystem/finance/infrastructure/web/FinanceControllers.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/domain/port/outbound/PaymentRepositories.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/domain/port/inbound/PaymentPorts.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/domain/model/PaymentModels.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/domain/service/PaymentServices.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/infrastructure/persistence/entity/PaymentEntities.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/infrastructure/persistence/repository/JpaRepositories.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/infrastructure/persistence/adapter/JpaAdapters.java",
    "services/payment-service/src/main/java/com/reportsystem/payment/infrastructure/gateway/GatewayAdapters.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/domain/port/outbound/ReportingRepositories.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/domain/port/inbound/ReportingPorts.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/domain/model/ReportingModels.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/domain/service/ReportingServices.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/infrastructure/persistence/entity/ReportingEntities.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/infrastructure/persistence/repository/JpaRepositories.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/infrastructure/persistence/adapter/JpaAdapters.java",
    "services/reporting-service/src/main/java/com/reportsystem/reporting/infrastructure/web/ReportingControllers.java",
]

def split_file(relpath):
    path = os.path.join(BASE, relpath)
    print(f"  {relpath}")
    
    with open(path) as f:
        content = f.read()
    
    pkg_match = re.search(r'^package\s+([\w.]+);', content, re.MULTILINE)
    if not pkg_match:
        print(f"    No package found")
        return
    pkg_end = pkg_match.end()
    
    # Find end of imports
    imports_end = pkg_end
    for m in re.finditer(r'^import\s+.*?;\s*$', content[imports_end:], re.MULTILINE):
        imports_end = pkg_end + m.end()
    
    header = content[:imports_end]
    body = content[imports_end:]
    
    # Find all public type declarations
    type_re = re.compile(r'public\s+(?:abstract\s+)?(?:class|interface|enum|record)\s+(\w+)')
    matches = list(type_re.finditer(body))
    
    if len(matches) <= 1:
        print(f"    Only {len(matches)} type(s), no split needed")
        return
    
    # For each type, extract its content and find annotations that belong to the next type
    type_data = []
    for i, m in enumerate(matches):
        name = m.group(1)
        start = m.start()
        if i + 1 < len(matches):
            next_start = matches[i + 1].start()
            segment = body[start:next_start]
            # Find the last closing brace to find where this type ends
            last_brace = segment.rfind('}')
            if last_brace >= 0:
                type_body = segment[:last_brace + 1]
                after_brace = segment[last_brace + 1:]
            else:
                type_body = segment
                after_brace = ""
        else:
            type_body = body[start:]
            after_brace = ""
        type_data.append((name, type_body, after_brace))
    
    # Get annotations before first type declaration (between imports and first public)
    first_type_start = matches[0].start()
    first_annotations = body[:first_type_start]
    
    # Build final content for each type, passing annotations from previous type
    dir_path = os.path.dirname(path)
    created = []
    prev_annotations = first_annotations
    for name, type_body, next_annotations in type_data:
        full = header + "\n" + prev_annotations + type_body.rstrip() + "\n"
        out_path = os.path.join(dir_path, f"{name}.java")
        with open(out_path, 'w') as f:
            f.write(full)
        created.append(name)
        prev_annotations = next_annotations
    
    os.remove(path)
    print(f"    -> {len(created)} files: {', '.join(created)}")

def main():
    print(f"Splitting {len(files)} files (v2 - with annotation support)...")
    for f in files:
        split_file(f)
    print("Done!")

if __name__ == '__main__':
    main()
