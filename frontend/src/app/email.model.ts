export interface EmailAddressGroup {
  label: string;
  values: string[];
}

export interface EmailAttachment {
  id: string;
  fileName: string;
  contentType: string;
  size: number;
}

export interface Email {
  id: string;
  from: string | null;
  to: string[];
  cc: string[];
  bcc: string[];
  subject: string | null;
  body: string | null;
  receivedAt: string;
  rawMessage: string;
  attachments: EmailAttachment[];
}
