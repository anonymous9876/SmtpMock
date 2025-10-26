import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf, NgFor } from '@angular/common';
import { Email, EmailAddressGroup, EmailAttachment } from './email.model';
import { EmailService } from './email.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, DatePipe],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  emails: Email[] = [];
  selectedEmail: Email | null = null;
  loading = false;
  errorMessage: string | null = null;
  filterValue = '';
  private newEmailSubscription?: Subscription;

  constructor(private readonly emailService: EmailService) {}

  ngOnInit(): void {
    this.refresh();
    this.newEmailSubscription = this.emailService.onNewEmail().subscribe({
      next: email => this.handleIncomingEmail(email),
      error: () => {
        // Connection errors are logged in the service; keep the UI functional even if streaming fails.
      }
    });
  }

  ngOnDestroy(): void {
    this.newEmailSubscription?.unsubscribe();
  }

  refresh(): void {
    this.loading = true;
    this.errorMessage = null;
    this.emailService.getEmails().subscribe({
      next: emails => {
        this.emails = emails;
        this.updateSelectionAfterFilter();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de récupérer les messages. Vérifiez que le serveur est démarré.';
      }
    });
  }

  selectEmail(email: Email): void {
    this.selectedEmail = email;
  }

  removeEmail(email: Email, event: MouseEvent): void {
    event.stopPropagation();
    this.emailService.removeEmail(email.id).subscribe({
      next: () => {
        this.emails = this.emails.filter(e => e.id !== email.id);
        if (this.selectedEmail?.id === email.id) {
          this.selectedEmail = null;
        }
        this.updateSelectionAfterFilter();
      }
    });
  }

  clearAll(): void {
    if (this.emails.length === 0) {
      return;
    }
    this.emailService.clearEmails().subscribe({
      next: () => {
        this.emails = [];
        this.selectedEmail = null;
      }
    });
  }

  getRecipientGroups(email: Email | null): EmailAddressGroup[] {
    if (!email) {
      return [];
    }
    const groups: EmailAddressGroup[] = [];
    if (email.to?.length) {
      groups.push({ label: 'À', values: email.to });
    }
    if (email.cc?.length) {
      groups.push({ label: 'Cc', values: email.cc });
    }
    if (email.bcc?.length) {
      groups.push({ label: 'Cci', values: email.bcc });
    }
    return groups;
  }

  trackById(_: number, email: Email): string {
    return email.id;
  }

  getRecipientList(email: Email): string {
    const recipients = email.to?.filter(Boolean) ?? [];
    return recipients.length > 0 ? recipients.join(', ') : 'Destinataire inconnu';
  }

  getAttachmentUrl(email: Email, attachment: EmailAttachment): string {
    return this.emailService.getAttachmentUrl(email.id, attachment.id);
  }

  formatFileSize(size: number): string {
    if (size === null || size === undefined) {
      return '';
    }
    const units = ['octets', 'Ko', 'Mo', 'Go', 'To'];
    let value = size;
    let unitIndex = 0;
    while (value >= 1024 && unitIndex < units.length - 1) {
      value /= 1024;
      unitIndex++;
    }
    const formatted = unitIndex === 0 ? value.toString() : value.toFixed(1);
    return `${formatted} ${units[unitIndex]}`;
  }

  get filteredEmails(): Email[] {
    const filter = this.filterValue.trim().toLowerCase();
    if (!filter) {
      return this.emails;
    }
    return this.emails.filter(email => this.getAllRecipients(email).some(recipient => recipient.toLowerCase() === filter));
  }

  onFilterChange(value: string): void {
    this.filterValue = value;
    this.updateSelectionAfterFilter();
  }

  private handleIncomingEmail(email: Email): void {
    this.emails = [email, ...this.emails.filter(existing => existing.id !== email.id)];
    this.updateSelectionAfterFilter();
  }

  private getAllRecipients(email: Email): string[] {
    const recipients: (string | null | undefined)[] = [
      ...(email.to ?? []),
      ...(email.cc ?? []),
      ...(email.bcc ?? [])
    ];
    return recipients.filter((recipient): recipient is string => !!recipient);
  }

  private updateSelectionAfterFilter(): void {
    const filtered = this.filteredEmails;
    if (filtered.length === 0) {
      this.selectedEmail = null;
      return;
    }
    if (this.selectedEmail && filtered.some(email => email.id === this.selectedEmail!.id)) {
      return;
    }
    this.selectedEmail = filtered[0];
  }
}
