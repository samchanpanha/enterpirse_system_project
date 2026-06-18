package com.reportsystem.finance.infrastructure.config;

import com.reportsystem.finance.domain.port.outbound.*;
import com.reportsystem.finance.domain.service.*;
import com.reportsystem.finance.infrastructure.persistence.repository.*;
import com.reportsystem.finance.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinanceConfig {

    private final JpaAccountRepository accountRepo;
    private final JpaJournalEntryRepository jeRepo;
    private final JpaJournalEntryLineRepository jelRepo;
    private final JpaInvoiceRepository invRepo;
    private final JpaTaxRecordRepository trRepo;
    private final JpaTaxFilingReportRepository tfrRepo;
    private final JpaEmployeeRepository empRepo;
    private final JpaPayrollPeriodRepository ppRepo;
    private final JpaPayrollItemRepository piRepo;

    public FinanceConfig(JpaAccountRepository ar, JpaJournalEntryRepository jer, JpaJournalEntryLineRepository jelr,
                         JpaInvoiceRepository ir, JpaTaxRecordRepository trr, JpaTaxFilingReportRepository tfrr,
                         JpaEmployeeRepository er, JpaPayrollPeriodRepository ppr, JpaPayrollItemRepository pir) {
        accountRepo = ar; jeRepo = jer; jelRepo = jelr; invRepo = ir; trRepo = trr; tfrRepo = tfrr; empRepo = er; ppRepo = ppr; piRepo = pir;
    }

    @Bean public AccountRepository accountRepository() { return new JpaAccountAdapter(accountRepo); }
    @Bean public JournalEntryRepository journalEntryRepository() { return new JpaJournalEntryAdapter(jeRepo); }
    @Bean public JournalEntryLineRepository journalEntryLineRepository() { return new JpaJournalEntryLineAdapter(jelRepo); }
    @Bean public InvoiceRepository invoiceRepository() { return new JpaInvoiceAdapter(invRepo); }
    @Bean public TaxRecordRepository taxRecordRepository() { return new JpaTaxRecordAdapter(trRepo); }
    @Bean public TaxFilingReportRepository taxFilingReportRepository() { return new JpaTaxFilingReportAdapter(tfrRepo); }
    @Bean public EmployeeRepository employeeRepository() { return new JpaEmployeeAdapter(empRepo); }
    @Bean public PayrollPeriodRepository payrollPeriodRepository() { return new JpaPayrollPeriodAdapter(ppRepo); }
    @Bean public PayrollItemRepository payrollItemRepository() { return new JpaPayrollItemAdapter(piRepo); }

    @Bean public AccountingServiceImpl accountingService(AccountRepository ar, JournalEntryRepository jer, JournalEntryLineRepository jelr) { return new AccountingServiceImpl(ar, jer, jelr); }
    @Bean public InvoiceServiceImpl invoiceService(InvoiceRepository ir) { return new InvoiceServiceImpl(ir); }
    @Bean public TaxServiceImpl taxService(TaxRecordRepository trr, TaxFilingReportRepository tfrr) { return new TaxServiceImpl(trr, tfrr); }
    @Bean public PayrollServiceImpl payrollService(EmployeeRepository er, PayrollPeriodRepository ppr, PayrollItemRepository pir) { return new PayrollServiceImpl(er, ppr, pir); }
}
