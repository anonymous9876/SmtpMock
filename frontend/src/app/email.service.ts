import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Email } from './email.model';

@Injectable({ providedIn: 'root' })
export class EmailService {
  private readonly baseUrl = '/api/emails';

  constructor(private readonly http: HttpClient) {}

  getEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(this.baseUrl);
  }

  getAttachmentUrl(emailId: string, attachmentId: string): string {
    return `${this.baseUrl}/${emailId}/attachments/${attachmentId}`;
  }

  removeEmail(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  clearEmails(): Observable<void> {
    return this.http.delete<void>(this.baseUrl);
  }
}
