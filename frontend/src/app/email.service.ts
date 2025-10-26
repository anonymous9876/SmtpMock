import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Email } from './email.model';

@Injectable({ providedIn: 'root' })
export class EmailService {
  private readonly baseUrl = '/api/emails';
  private readonly newEmailSubject = new Subject<Email>();
  private socket: WebSocket | null = null;
  private reconnectTimeoutId: number | null = null;

  constructor(private readonly http: HttpClient) {}

  getEmails(): Observable<Email[]> {
    return this.http.get<Email[]>(this.baseUrl);
  }

  onNewEmail(): Observable<Email> {
    this.ensureWebSocketConnection();
    return this.newEmailSubject.asObservable();
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

  private ensureWebSocketConnection(): void {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    if (this.reconnectTimeoutId !== null) {
      window.clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = null;
    }

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${protocol}://${window.location.host}/ws/emails`;
    this.socket = new WebSocket(url);

    this.socket.onmessage = event => {
      try {
        const email = JSON.parse(event.data) as Email;
        this.newEmailSubject.next(email);
      } catch (error) {
        console.error('Impossible de décoder le message WebSocket entrant.', error);
      }
    };

    this.socket.onerror = event => {
      console.error('Erreur WebSocket détectée.', event);
    };

    this.socket.onclose = () => {
      this.socket = null;
      this.scheduleReconnect();
    };
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimeoutId !== null) {
      return;
    }
    this.reconnectTimeoutId = window.setTimeout(() => {
      this.reconnectTimeoutId = null;
      this.ensureWebSocketConnection();
    }, 5000);
  }
}
