import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, NgIf, NgFor } from '@angular/common';
import { Email, EmailAddressGroup, EmailAttachment } from './email.model';
import { EmailService } from './email.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, NgFor, NgIf, DatePipe],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  emails: Email[] = [];
  selectedEmail: Email | null = null;
  loading = false;
  errorMessage: string | null = null;

  constructor(private readonly emailService: EmailService) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.errorMessage = null;
    this.emailService.getEmails().subscribe({
      next: emails => {
        this.emails = emails;
        this.selectedEmail = emails.length > 0 ? emails[0] : null;
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
          this.selectedEmail = this.emails.length > 0 ? this.emails[0] : null;
        }
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
}
